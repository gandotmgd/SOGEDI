package com.moovapps.sogedi.referentiels;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.axemble.vdoc.sdk.document.extensions.BaseStorageResourceExtension;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IResourceController;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.INamedWidget;
import com.moovapps.sogedi.vente.document.EnregistrementCommande;

public class Bouteille extends BaseStorageResourceExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2248034968332991446L;
	protected static final Logger log = Logger.getLogger(EnregistrementCommande.class);
	
	protected IContext context = null;
	protected IOrganization organization = null;
	protected IProject project = null;
	protected ICatalog catalogRef = null;
	protected IResourceDefinition resourceDefinitionBouteilleSuivi = null, resourceDefinitionBouteille = null;
	
	@Override
	public void onPropertyChanged(IProperty property) {
		try {
			if(property.getName().equals("CodeEmballage"))
			{
			context = getWorkflowModule().getSysadminContext();
			organization = getDirectoryModule().getOrganization(context, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.organization.name"));
			project = getProjectModule().getProject(context, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.project.name"), organization);
			catalogRef = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
			resourceDefinitionBouteille = getWorkflowModule().getResourceDefinition(context, catalogRef, "Bouteilles");
			
			String codeBouteille = (String) getStorageResource().getValue("CodeEmballage");
			IViewController viewController = getWorkflowModule().getViewController(context, IResource.class);
			viewController.addEqualsConstraint("CodeEmballage", codeBouteille);
			@SuppressWarnings("unchecked")
			Collection<IStorageResource> storageResources = viewController.evaluate(resourceDefinitionBouteille);
			NamedContainer Bottomcontainer = getResourceController().getButtonContainer(IResourceController.BOTTOM_CONTAINER);
			List<IWidget> buttonList1 = Bottomcontainer.getWidgets();
			if(!storageResources.isEmpty())
			{
				
				for (IWidget iWidget1 : buttonList1)
				{
					INamedWidget iNamedWidget = (INamedWidget)iWidget1;
					if (iNamedWidget.getName().contains("save") )
					{
						iNamedWidget.setHidden(true);
					}
				}
				getResourceController().alert("La bouteille de code : "+codeBouteille +" existe déjà dans le référentiel");
				getStorageResource().setValue("CodeEmballage", null);
			}
			else
			{
				for (IWidget iWidget1 : buttonList1)
				{
					INamedWidget iNamedWidget = (INamedWidget)iWidget1;
					if (iNamedWidget.getName().contains("save") )
					{
						iNamedWidget.setHidden(false);
					}
				}
			}
		}
		}catch(Exception e)
		{
			log.error("Erreur dans la classe com.moovapps.sogedi.referentiels.Bouteille méthode onPropertyChanged: "+e.getClass()+" - "+e.getMessage());	
		}
		super.onPropertyChanged(property);
	}
	
	@Override
	public boolean onBeforeSave() {
		try 
		{
			context = getWorkflowModule().getSysadminContext();
			organization = getDirectoryModule().getOrganization(context, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.organization.name"));
			project = getProjectModule().getProject(context, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.project.name"), organization);
			catalogRef = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
			
			IStorageResource storageResource = getStorageResource();
			String suivi = (String) storageResource.getValue("Suivi");
			if(suivi != null && suivi.equals("Non"))
			{
				resourceDefinitionBouteilleSuivi = getWorkflowModule().getResourceDefinition(context, catalogRef, "SuiviBouteille");
				IStorageResource suiviBouteille = getWorkflowModule().createStorageResource(context, resourceDefinitionBouteilleSuivi,null);
				
				if(suiviBouteille != null)
				{
					suiviBouteille.setValue("DateDeSuivi", new Date());
					suiviBouteille.setValue("CodeEmballage", (String) storageResource.getValue("CodeEmballage"));
					suiviBouteille.setValue("TypeGazSuivi", (String) storageResource.getValue("TypeGazBout"));
					suiviBouteille.setValue("EtatBoutSuivi", (String) storageResource.getValue("EtatBouteille"));
					suiviBouteille.setValue("CodeTiersOrig", (String) storageResource.getValue("CodeTiersOrig"));
					suiviBouteille.setValue("CodeTiersMouv", (String) storageResource.getValue("CodeTiersMouvement"));
					suiviBouteille.setValue("EntreeSortie", (String) storageResource.getValue("EntreeSortie"));
					suiviBouteille.setValue("DateMouv", (Date) storageResource.getValue("DateMouv"));
					
					storageResource.setValue("Suivi", "Oui");
					suiviBouteille.save(context);
				}
				else
				{
					getResourceController().alert("Impossible de créer cette ligne dans le référentiel de suivi de bouteille."+"\n"+"Contacter votre administrateur");
					return false;
				}
				
			}			
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.referentiels.Bouteille méthode onBeforeSave: "+e.getClass()+" - "+e.getMessage());
		}
		return super.onBeforeSave();
	}
}
