package com.moovapps.sogedi.vente.document;

import java.util.ArrayList;
import java.util.Collection;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IOptionList.IOption;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.axemble.vdoc.sdk.interfaces.IWorkflow;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.utils.Logger;
import com.moovapps.sogedi.Helper.Helper;

public class EnregistrementCommande extends BaseDocumentExtension{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(EnregistrementCommande.class);
	
	protected IContext context = null;
	protected IOrganization organization = null;
	protected IProject project = null;
	protected IWorkflow workflow = null;
	protected ICatalog catalogRef = null;
	protected IResourceDefinition resourceDefinitionClient = null, resDefinitionDepot = null, refBouteille = null;
		
	@Override
	public boolean onAfterLoad() {
		try {
			context = getWorkflowModule().getSysadminContext();
			organization = getDirectoryModule().getOrganization(context, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.organization.name"));
			project = getWorkflowInstance().getCatalog().getProject();
			catalogRef = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
			resourceDefinitionClient = getWorkflowModule().getResourceDefinition(context, catalogRef, "Clients");
			refBouteille = getWorkflowModule().getResourceDefinition(context, catalogRef, "Bouteilles");
			
			//Recherche du dépôt du créateur
			IUser creator = getWorkflowInstance().getCreatedBy();
			if(creator != null)
			{
				resDefinitionDepot = getWorkflowModule().getResourceDefinition(context, catalogRef, "Depot");
				getWorkflowInstance().setValue("Depot", Helper.getDepot(context,getWorkflowModule(),resDefinitionDepot,creator));
			}
			//Alimentation de la liste des clients
			IViewController viewController = getWorkflowModule().getViewController(context, IResource.class);
			
			Collection<? extends IStorageResource> storageResources = viewController.evaluate(resourceDefinitionClient);
			if(storageResources != null && storageResources.size() > 0)
			{
				ArrayList<IOption> options = new ArrayList<>();
				for(IStorageResource storageResource : storageResources)
				{
					options.add(getWorkflowModule().createListOption((String) storageResource.getValue("NomOuRaisonSociale") , (String) storageResource.getValue("NomOuRaisonSociale"))); 
				}
				options.add(getWorkflowModule().createListOption("Autres","Autres"));
				getWorkflowInstance().setList("Client", options);
			}
			
			//Alimentation de la liste des bouteilles à vendre
			ArrayList<IOption> options = Helper.getBouteilles(context, getWorkflowModule(), getWorkflowInstance(), refBouteille, null, Helper.getDepot(context,getWorkflowModule(), resDefinitionDepot,getWorkflowInstance().getCreatedBy()), "Remplie",true);
			if(options != null && options.size() > 0)
			{
				getWorkflowInstance().setList("ListeDesBouteillesVendues", options);
			}
			else
			{
				getWorkflowInstance().setValue("ListeDesBouteillesVendues", null);
				getWorkflowInstance().setList("ListeDesBouteillesVendues", null);
				getResourceController().inform("ListeDesBouteillesVendues", "Aucune bouteille de type  n'est prête pour vente dans votre dépôt.");
				
			}
			//Alimentation de la liste des bouteilles à rendre
			String nomClient = (String) getWorkflowInstance().getValue("Client");
			String boutARendre = (String) getWorkflowInstance().getValue("LeClientATIlRenduDesBouteilles");
			if(nomClient != null && boutARendre != null && boutARendre.equals("Oui"))
			{
				ArrayList<IOption> optionsRendues = Helper.getBouteilles(context, getWorkflowModule(), getWorkflowInstance(), refBouteille, null, nomClient, "Livrée",false);
				if(options != null && options.size() > 0)
				{
					getWorkflowInstance().setList("ListeDesBouteillesRendues", optionsRendues);
				}
				else
				{
					getWorkflowInstance().setValue("ListeDesBouteillesRendues", null);
					getWorkflowInstance().setList("ListeDesBouteillesRendues", null);
					getResourceController().inform("ListeDesBouteillesRendues", "Aucune bouteille n'est livrée chez "+nomClient+".");
					
				}
			}
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.vente.document.EnregistrementCommande méthode onAfterLoad: "+e.getClass()+" - "+e.getMessage());
		}
		return super.onAfterLoad();
	}
	
	@Override
	public boolean isOnChangeSubscriptionOn(IProperty property) {
		if(property.getName().equals("Client") || property.getName().equals("LeClientATIlRenduDesBouteilles") || property.getName().equals("ListeDesBouteillesVendues") || property.getName().equals("ListeDesBouteillesRendues"))
		{
			return true;
		}
		return super.isOnChangeSubscriptionOn(property);
	}
	
	@Override
	public void onPropertyChanged(IProperty property) {
		try 
		{
			if(property.getName().equals("Client"))
			{
				String nomClient = (String) getWorkflowInstance().getValue("Client");
				if(nomClient != null)
				{
					if(! nomClient.equals("Autres"))
					{
						getResourceController().setEditable("NClient", false);
						getResourceController().setEditable("TypeClient", false);
						getResourceController().setEditable("Adresse", false);
						getResourceController().setEditable("Telephone", false);
						getResourceController().setEditable("Email", false);
						getInfosCLIENT(nomClient);
					}
					else
					{
						getWorkflowInstance().setValue("NClient", null);
						getResourceController().setEditable("NClient", true);
						getResourceController().setMandatory("NClient", true);
						
						getWorkflowInstance().setValue("TypeClient", null);
						getResourceController().setEditable("TypeClient", true);
						getResourceController().setMandatory("TypeClient", true);
						
						getWorkflowInstance().setValue("Adresse", null);
						getResourceController().setEditable("Adresse", true);
						getResourceController().setMandatory("Adresse", true);
						
						getWorkflowInstance().setValue("Telephone", null);
						getResourceController().setEditable("Telephone", true);
						getResourceController().setMandatory("Telephone", true);
						
						getWorkflowInstance().setValue("Email", null);
						getResourceController().setEditable("Email", true);
						getResourceController().setMandatory("Email", true);
						
						
						
						
					}
				}
				else
				{
					getWorkflowInstance().setValue("NClient", null);
					getWorkflowInstance().setValue("TypeClient", null);
					getWorkflowInstance().setValue("Adresse", null);
					getWorkflowInstance().setValue("Telephone", null);
					getWorkflowInstance().setValue("Email", null);
				}
			}
			
			if(property.getName().equals("LeClientATIlRenduDesBouteilles") || property.getName().equals("Client"))
			{
				String nomClient = (String) getWorkflowInstance().getValue("Client");
				String boutARendre = (String) getWorkflowInstance().getValue("LeClientATIlRenduDesBouteilles");
				if(nomClient != null && boutARendre != null && boutARendre.equals("Oui"))
				{
					ArrayList<IOption> options = Helper.getBouteilles(context, getWorkflowModule(), getWorkflowInstance(), refBouteille, null, nomClient, "Livrée",false);
					if(options != null && options.size() > 0)
					{
						getWorkflowInstance().setList("ListeDesBouteillesRendues", options);
					}
					else
					{
						getWorkflowInstance().setValue("ListeDesBouteillesRendues", null);
						getWorkflowInstance().setList("ListeDesBouteillesRendues", null);
						getResourceController().inform("ListeDesBouteillesRendues", "Aucune bouteille n'est livrée chez "+nomClient+".");
						
					}
				}
				
			}
			
			if(property.getName().equals("ListeDesBouteillesVendues"))
			{
				getWorkflowInstance().setValue("BouteillesVendues", null);
				getWorkflowInstance().setValue("BonDeMouvementDeBouteille", null);
				Collection<String> boutVendues = (Collection<String>) getWorkflowInstance().getValue("ListeDesBouteillesVendues");
				if(boutVendues != null && boutVendues.size() > 0)
				{
					ILinkedResource linkedResource = null;
					for(String bout : boutVendues)
					{
						linkedResource = getWorkflowInstance().createLinkedResource("BouteillesVendues");
								linkedResource.setValue("Num", bout);
								linkedResource.setValue("Cap", (Float)Helper.getBouteilleByCode(context, getWorkflowModule(), refBouteille, bout).getValue("Capacite"));
										
								linkedResource.save(context);
						getWorkflowInstance().addLinkedResource(linkedResource);
					}
					
				}
				else
				{
					getWorkflowInstance().setValue("BouteillesVendues", null);
				}
			}
			
			if(property.getName().equals("ListeDesBouteillesRendues"))
			{
				getWorkflowInstance().setValue("BouteillesRendues", null);
				getWorkflowInstance().setValue("BonDeMouvementDeBouteille", null);
				Collection<String> boutVendues = (Collection<String>) getWorkflowInstance().getValue("ListeDesBouteillesRendues");
				if(boutVendues != null && boutVendues.size() > 0)
				{
					ILinkedResource linkedResource = null;
					for(String bout : boutVendues)
					{
						linkedResource = getWorkflowInstance().createLinkedResource("BouteillesRendues");
								linkedResource.setValue("Num", bout);
								linkedResource.setValue("Cap", (Float)Helper.getBouteilleByCode(context, getWorkflowModule(), refBouteille, bout).getValue("Capacite"));
										
								linkedResource.save(context);
						getWorkflowInstance().addLinkedResource(linkedResource);
					}
					
				}
				else
				{
					getWorkflowInstance().setValue("BouteillesRendues", null);
				}
			}
			
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.vente.document.EnregistrementCommande méthode onPropertyChanged: "+e.getClass()+" - "+e.getMessage());
		}
		super.onPropertyChanged(property);
	}
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		try {
			if(action.getName().equals("Envoyer2"))
			{
				Collection<? extends IAttachment> attachments = (Collection<? extends IAttachment>) getWorkflowModule().getAttachments(getWorkflowInstance(), "BonDeMouvementDeBouteille");
				if(attachments.size() == 0)
				{
					getResourceController().alert("Vous devez obligatoirement générer le BMB");
					return false;
				}
			}
			
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.vente.document.EnregistrementCommande methode onBeforeSubmit : " + e.getClass() + " - " + e.getMessage());
		}
		return super.onBeforeSubmit(action);
	}

	private void getInfosCLIENT(String nomClient)
	{
		try
		{
			IViewController viewController = getWorkflowModule().getViewController(context, IResource.class);
			viewController.addEqualsConstraint("NomOuRaisonSociale", nomClient);
			
			Collection<IStorageResource> storageResources = viewController.evaluate(resourceDefinitionClient);
			if(storageResources != null && storageResources.size() > 0)
			{
				IStorageResource storageResource = storageResources.iterator().next();
				
				getWorkflowInstance().setValue("NClient", storageResource.getValue("NClient"));
				getResourceController().setThrowEvents("NClient", true);
				
				getWorkflowInstance().setValue("TypeClient", storageResource.getValue("TypeClient"));
				getResourceController().setThrowEvents("TypeClient", true);
				
				getWorkflowInstance().setValue("Adresse", storageResource.getValue("Adresse"));
				getResourceController().setThrowEvents("Adresse", true);
				
				getWorkflowInstance().setValue("Telephone", storageResource.getValue("Telephone"));
				getResourceController().setThrowEvents("Telephone", true);
				
				getWorkflowInstance().setValue("Email", storageResource.getValue("EmailClient"));
				getResourceController().setThrowEvents("Email", true);
				
			}
			else
			{
				getWorkflowInstance().setValue("NClient", null);
				getWorkflowInstance().setValue("TypeClient", null);
				getWorkflowInstance().setValue("Adresse", null);
				getWorkflowInstance().setValue("Telephone", null);
				getWorkflowInstance().setValue("Email", null);
			}
		}
		catch (Exception e)
		{
			log.error("Erreur dans la classe com.moovapps.sogedi.vente.document.EnregistrementCommande methode getInfosCLIENT "+e.getClass()+"-"+e.getMessage());
		}
	}
	
	
	
	
}
