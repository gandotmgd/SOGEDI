package com.moovapps.sogedi.approvisionnemntDepot.document;

import java.util.ArrayList;
import java.util.Collection;

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

public class AccuserReception extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
protected static final Logger log = Logger.getLogger(Approvisionnement.class);
	
	
	
	@Override
	public boolean onAfterLoad() {
		try {
			//Alimmentation de la liste des bouteilles rébus
			Collection<String> bouteillesApprovisonnees = (Collection<String>) getWorkflowInstance().getValue("BouteillesApprovisionnees");
			if(bouteillesApprovisonnees != null && bouteillesApprovisonnees.size() > 0)
			{
				ArrayList<IOption> liste = new ArrayList<IOption>();
				for(String bouteille : bouteillesApprovisonnees)
				{
					liste.add(getWorkflowModule().createListOption(bouteille, bouteille));
				}
				getWorkflowInstance().setList("BouteillesRebus", liste);
			}
			else
			{
				getWorkflowInstance().setValue("BouteillesRebus", null);
				getWorkflowInstance().setList("BouteillesRebus", null);
			}
			
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.approvisionnemntDepot.document.AccuserReception méthode onAfterLoad: "+e.getClass()+" - "+e.getMessage());
		}
		return super.onAfterLoad();
	}

}

