package com.moovapps.sogedi.vente.agents;

import java.util.Collection;

import com.axemble.easysite.io.storage.beans.Storage;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.ISecurityController;
import com.axemble.vdoc.sdk.interfaces.ISecurityController.IPermissionFlags;
import com.axemble.vdoc.sdk.interfaces.ISecurityController.IPermissionLevels;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.axemble.vdoc.sdk.interfaces.IWorkflow;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.workflow.domain.ProcessWorkflowInstance;

public class AttributionDroitDeCreation extends BaseAgent {
	
	protected static final Logger log = Logger.getLogger(AttributionDroitDeCreation.class);
	
	protected IContext iContext = null;
	IOrganization iOrganization = null;
	IProject iProject = null;
	ICatalog iCatalogVente = null, iCatalogRef = null, iCatalogAppro = null;
	IWorkflow iWorkflowVente = null, iWorkflowAppro = null;
	

	@Override
	protected void execute() {
		try {
			iContext = getWorkflowModule().getSysadminContext();
			// We load SDK objects, coming from what we get input configuration
			iOrganization = getDirectoryModule().getOrganization(iContext, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.organization.name"));
		    iProject = getProjectModule().getProject(iContext, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.project.name"), iOrganization);
		    iCatalogVente = getWorkflowModule().getCatalog(iContext, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.vente.catalog.name"), iProject);
		    iCatalogAppro = getWorkflowModule().getCatalog(iContext, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.appro.catalog.name"), iProject);
		    iCatalogRef = getWorkflowModule().getCatalog(iContext, "Referentiels", ICatalog.IType.STORAGE, iProject);
		    iWorkflowVente = getWorkflowModule().getWorkflow(iContext, iCatalogVente, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.vente.workflow.name"));
		    iWorkflowAppro = getWorkflowModule().getWorkflow(iContext, iCatalogAppro, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.appro.workflow.name"));
		    																										
		    // We get security controllers (for workflow and for catalog)
		    ISecurityController iSecurityControllerCatalogVente = getWorkflowModule().getSecurityController(iCatalogVente);
		    ISecurityController iSecurityControllerWorkflowVente = getWorkflowModule().getSecurityController(iWorkflowVente);
		    ISecurityController iSecurityControllerCatalogAppro = getWorkflowModule().getSecurityController(iCatalogAppro);
		    ISecurityController iSecurityControllerWorkflowAppro = getWorkflowModule().getSecurityController(iWorkflowAppro);
		    
		    getWorkflowModule().beginTransaction();
		    
		    IViewController viewController = getWorkflowModule().getViewController(iContext, IResource.class);
		    Collection<? extends IStorageResource> depots = viewController.evaluate(getWorkflowModule().getResourceDefinition(iContext, iCatalogRef, "Depot"));
		    if(depots != null && depots.size() > 0)
		    {
		    	for(IStorageResource depot : depots)
		    	{
		    		IUser responsableDepot = (IUser) depot.getValue("ResponsableDepot");
		    		if(responsableDepot != null)
		    		{
		    			/*****************************PROCESSUS DE VENTE**********************************************************/
		    			 // Right to access to process group
		    	        // IPermissionFlags.WorkflowCatalogPermissionFlags.ALLOW_ACCESS = 0
		    			boolean hasAccessToProcessGroup = iSecurityControllerCatalogVente.hasPermission(responsableDepot, IPermissionLevels.NO, IPermissionFlags.WorkflowCatalogPermissionFlags.ALLOW_ACCESS);
		    			if (!hasAccessToProcessGroup)
		    	        {
		    				iSecurityControllerCatalogVente.addPermission(responsableDepot, IPermissionLevels.NO, IPermissionFlags.WorkflowCatalogPermissionFlags.ALLOW_ACCESS);
		    	         }
		    			// Rights to create documents on workflow
		    	          // IPermissionFlags.WorkflowPermissionFlags.CREATE = 9
		    	          // We use ProcessWorkflowInstance.class instead of IWorkflowInstance.class to test security (bug)
		    	        boolean canCreateDocumentsOnWorkflow = iSecurityControllerWorkflowVente.hasPermission(responsableDepot, ProcessWorkflowInstance.class, IPermissionLevels.NO, IPermissionFlags.WorkflowPermissionFlags.CREATE);
		    	        if (!canCreateDocumentsOnWorkflow)
		    	        {
		    	        	iSecurityControllerWorkflowVente.addPermission(responsableDepot, ProcessWorkflowInstance.class, IPermissionLevels.NO, IPermissionFlags.WorkflowPermissionFlags.CREATE);
		    	        }
		    	        /*****************************PROCESSUS D'APPROVISIONNEMENT**********************************************************/
		    	        // Right to access to process group
		    	        // IPermissionFlags.WorkflowCatalogPermissionFlags.ALLOW_ACCESS = 0
		    			boolean hasAccessToProcessGroupAppro = iSecurityControllerCatalogAppro.hasPermission(responsableDepot, IPermissionLevels.NO, IPermissionFlags.WorkflowCatalogPermissionFlags.ALLOW_ACCESS);
		    			if (!hasAccessToProcessGroupAppro)
		    	        {
		    				iSecurityControllerCatalogAppro.addPermission(responsableDepot, IPermissionLevels.NO, IPermissionFlags.WorkflowCatalogPermissionFlags.ALLOW_ACCESS);
		    	         }
		    			// Rights to create documents on workflow
		    	          // IPermissionFlags.WorkflowPermissionFlags.CREATE = 9
		    	          // We use ProcessWorkflowInstance.class instead of IWorkflowInstance.class to test security (bug)
		    	        boolean canCreateDocumentsOnWorkflowAppro = iSecurityControllerWorkflowAppro.hasPermission(responsableDepot, ProcessWorkflowInstance.class, IPermissionLevels.NO, IPermissionFlags.WorkflowPermissionFlags.CREATE);
		    	        if (!canCreateDocumentsOnWorkflowAppro)
		    	        {
		    	        	iSecurityControllerWorkflowAppro.addPermission(responsableDepot, ProcessWorkflowInstance.class, IPermissionLevels.NO, IPermissionFlags.WorkflowPermissionFlags.CREATE);
		    	        }
		    		}
		    	}
		    }
		    
		    getWorkflowModule().commitTransaction();
		    
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.vente.agents.AttributionDroitDeCreation methode execute : " + e.getClass() + " - " + e.getMessage());
			getWorkflowModule().rollbackTransaction();
		}

	}

}
