package com.moovapps.sogedi.approvisionnemntDepot.document;

import java.util.ArrayList;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.IWorkflow;
import com.axemble.vdoc.sdk.interfaces.IOptionList.IOption;
import com.axemble.vdoc.sdk.utils.Logger;
import com.moovapps.sogedi.Helper.Helper;

public class Approvisionnement extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(Approvisionnement.class);
	
	protected IContext context = null;
	protected IOrganization organization = null;
	protected IProject project = null;
	protected IWorkflow workflow = null;
	protected ICatalog catalogRef = null;
	protected IResourceDefinition resDefinitionDepot = null, refBouteille = null;
	
	@Override
	public boolean onAfterLoad() {
		try {
			context = getWorkflowModule().getSysadminContext();
			organization = getDirectoryModule().getOrganization(context, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.organization.name"));
			project = getWorkflowInstance().getCatalog().getProject();
			catalogRef = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
			
			//Alimmentation de la liste des bouteilles à approvisionner
			refBouteille = getWorkflowModule().getResourceDefinition(context, catalogRef, "Bouteilles");
			ArrayList<IOption> options = Helper.getBouteilles(context, getWorkflowModule(), getWorkflowInstance(), refBouteille, null, "AGENCE", "Remplie",true);
			if(options != null && options.size() > 0)
			{
				getWorkflowInstance().setList("BouteillesApprovisionnees", options);
			}
			else
			{
				getWorkflowInstance().setValue("BouteillesApprovisionnees", null);
				getWorkflowInstance().setList("BouteillesApprovisionnees", null);
				getResourceController().inform("BouteillesApprovisionnees", "Aucune bouteille n'est à être livrée dans votre dépôt");
				
			}
			
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.approvisionnemntDepot.document.Approvisionnement méthode onAfterLoad: "+e.getClass()+" - "+e.getMessage());
		}
		return super.onAfterLoad();
	}

}

