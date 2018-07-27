package com.moovapps.sogedi.vente.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
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
	protected IResourceDefinition resourceDefinitionClient = null, resDefinitionDepot = null, resourceDefinitionSuiviBouteille = null;
		
	@Override
	public boolean onAfterLoad() {
		try {
			context = getWorkflowModule().getSysadminContext();
			organization = getDirectoryModule().getOrganization(context, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.organization.name"));
			project = getWorkflowInstance().getCatalog().getProject();
			catalogRef = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
			resourceDefinitionClient = getWorkflowModule().getResourceDefinition(context, catalogRef, "Clients");
			
			//Recherche du dépôt du créateur
			IUser creator = getWorkflowInstance().getCreatedBy();
			if(creator != null)
			{
				resDefinitionDepot = getWorkflowModule().getResourceDefinition(context, catalogRef, "Depot");
				getWorkflowInstance().setValue("Depot", getDepot(creator));
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
			resourceDefinitionSuiviBouteille = getWorkflowModule().getResourceDefinition(context, catalogRef, "SuiviBouteille");
			ArrayList<IOption> options = Helper.getBouteillesVente(context, getWorkflowModule(), getWorkflowInstance(), resourceDefinitionSuiviBouteille, getDepot(getWorkflowInstance().getCreatedBy()), "Remplie");
			if(options != null && options.size() > 0)
			{
				getWorkflowInstance().setList("ListeDesBouteillesVendues", options);
			}
			else
			{
				getWorkflowInstance().setList("ListeDesBouteillesVendues", null);
				getResourceController().inform("ListeDesBouteillesVendues", "Aucune bouteille de type  n'est prête pour vente dans votre dépôt.");
				
			}
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.vente.document.EnregistrementCommande méthode onAfterLoad: "+e.getClass()+" - "+e.getMessage());
		}
		return super.onAfterLoad();
	}
	
	@Override
	public boolean isOnChangeSubscriptionOn(IProperty property) {
		if(property.getName().equals("Client") || property.getName().equals("LeClientATIlRenduDesBouteilles"))
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
						
						
						
						
					}
				}
				else
				{
					getWorkflowInstance().setValue("NClient", null);
					getWorkflowInstance().setValue("TypeClient", null);
					getWorkflowInstance().setValue("Adresse", null);
					getWorkflowInstance().setValue("Telephone", null);
				}
			}
			
			if(property.getName().equals("LeClientATIlRenduDesBouteilles") || property.getName().equals("Client"))
			{
				String nomClient = (String) getWorkflowInstance().getValue("Client");
				String boutARendre = (String) getWorkflowInstance().getValue("LeClientATIlRenduDesBouteilles");
				if(nomClient != null && boutARendre != null && boutARendre.equals("Oui"))
				{
					ArrayList<IOption> options = Helper.getBouteillesARendre(context, getWorkflowModule(), getWorkflowInstance(), resourceDefinitionSuiviBouteille, nomClient, "Location");
					if(options != null && options.size() > 0)
					{
						getWorkflowInstance().setList("ListeDesBouteillesRendues", options);
					}
					else
					{
						getWorkflowInstance().setList("ListeDesBouteillesRendues", null);
						getResourceController().inform("ListeDesBouteillesRendues", "Aucune bouteille n'est prête pour une vente dans votre dépôt.");
						
					}
				}
				
			}
			
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.vente.document.EnregistrementCommande méthode onPropertyChanged: "+e.getClass()+" - "+e.getMessage());
		}
		super.onPropertyChanged(property);
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
				
			}
			else
			{
				getWorkflowInstance().setValue("NClient", null);
				getWorkflowInstance().setValue("TypeClient", null);
				getWorkflowInstance().setValue("Adresse", null);
				getWorkflowInstance().setValue("Telephone", null);
			}
		}
		catch (Exception e)
		{
			log.error("Erreur dans la classe com.moovapps.sogedi.vente.document.EnregistrementCommande methode getInfosCLIENT "+e.getClass()+"-"+e.getMessage());
		}
	}
	
	private String getDepot(IUser user)
	{
		try {
			IViewController viewController = getWorkflowModule().getViewController(context, IResource.class);
			viewController.addEqualsConstraint("ResponsableDepot", user);
			
			Collection<IStorageResource> storageResources = viewController.evaluate(resDefinitionDepot);
			if(storageResources != null && storageResources.size() > 0)
			{
				IStorageResource storageResource = storageResources.iterator().next();
				if(storageResource != null)
				{
					return (String) storageResource.getValue("NomDepot");
				}
			}
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.vente.document.EnregistrementCommande methode getDepot "+e.getClass()+"-"+e.getMessage());
		}
		return null;
	}
}
