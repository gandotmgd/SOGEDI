package com.moovapps.sogedi.vente.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.utils.Logger;

public class Facturation extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(Facturation.class);
	
	protected IContext context = null;
	protected IProject project = null;
	protected ICatalog catalogRef = null;
	protected IResourceDefinition refClient = null;
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		try {
			if(action.getName().equals("Envoyer"))
			{
				context = getWorkflowModule().getSysadminContext();
				project = getWorkflowInstance().getCatalog().getProject();
				catalogRef = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
				refClient = getWorkflowModule().getResourceDefinition(context, catalogRef, "Clients");
				
				String enregistrer = (String) getWorkflowInstance().getValue("VoulezVousEnregistrerCeClient");
				if(enregistrer != null && enregistrer.equals("Oui"))
				{
					IStorageResource storageResource = getWorkflowModule().createStorageResource(context, refClient, null);
					if(storageResource != null)
					{
						storageResource.setValue("NClient", (String) getWorkflowInstance().getValue("NClient"));
						storageResource.setValue("NomOuRaisonSociale", (String) getWorkflowInstance().getValue("NomOuRaisonSociale"));
						storageResource.setValue("TypeClient", (String) getWorkflowInstance().getValue("TypeClient"));
						storageResource.setValue("Adresse", (String) getWorkflowInstance().getValue("Adresse"));
						storageResource.setValue("Telephone", (String) getWorkflowInstance().getValue("Telephone"));
						storageResource.setValue("EmailClient", (String) getWorkflowInstance().getValue("Email"));
						
						storageResource.save(context);
					}
				}
			}
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.vente.document.Facturation methode onBeforeSubmit "+e.getClass()+"-"+e.getMessage());
		}
		return super.onBeforeSubmit(action);
	}

}
