package com.moovapps.sogedi.acquisitionproduction.agents;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.ITaskInstance;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.axemble.vdoc.sdk.interfaces.IWorkflow;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.utils.Logger;
import com.moovapps.sogedi.acquisitionproduction.campagne.document.SyntheseCampagne;

public class AgentDeCreationSyntheseJournalière extends BaseAgent {
	
	protected static final Logger log = Logger.getLogger(AgentDeCreationSyntheseJournalière.class);
	
	protected IContext context = null;
	protected IOrganization organization = null;
	protected IProject project = null;
	protected ICatalog catalog = null, catalogRef = null;
	protected IWorkflow workflow = null, workflowCible = null;
	protected IResourceDefinition refBout = null;
	
	protected ITaskInstance taskInstance = null;
	protected IAction action = null;

	@Override
	protected void execute() {
		try {
			Date now = new Date();
			
			context = getWorkflowModule().getSysadminContext();
			organization = getDirectoryModule().getOrganization(context, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.organization.name"));
			project = getProjectModule().getProject(context, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.project.name"), organization);
			catalog = getWorkflowModule().getCatalog(context, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.production.catalog.name"), project);
			catalogRef = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
			workflow = getWorkflowModule().getWorkflow(context, catalog, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.rampe.workflow.name"));
			workflowCible = getWorkflowModule().getWorkflow(context, catalog, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.syntheseJournaliere.workflow.name"));
			refBout = getWorkflowModule().getResourceDefinition(context, catalogRef, "Bouteilles");
	
			IViewController viewController = getWorkflowModule().getViewController(context);
			
							//viewController.addEqualsConstraint("sys_CreationDate", new Date());
			Collection<IWorkflowInstance> rampes = viewController.evaluate(workflow);
			if(rampes != null && rampes.size() > 0)
			{
				for(IWorkflowInstance rampe : rampes)
				{
					Date dateCreation = (Date) rampe.getValue("sys_CreationDate");
					
					if(dateCreation.compareTo(now) != 0)
					{
						rampes.remove(rampe);
					}
				}
				if(rampes != null && rampes.size() > 0)
				{
					Collection<String> allBouteilles = SyntheseCampagne.getAllbouteilles(rampes);
					Map<String, Float> nombreParType = new HashMap<>();
					if(allBouteilles != null && allBouteilles.size() > 0)
					{
						nombreParType = SyntheseCampagne.getNombreParType(context, getWorkflowModule(), refBout, allBouteilles);
						if(nombreParType != null && nombreParType.size() > 0)
						{
							IWorkflowInstance syntheseJournaliere = getWorkflowModule().createWorkflowInstance(context, workflowCible, null);
							if(syntheseJournaliere != null)
							{
								syntheseJournaliere.setValue("SyntheseJour", null);
								SyntheseCampagne.getSynthese(context,getWorkflowModule(),syntheseJournaliere, refBout, nombreParType, allBouteilles,"SyntheseJour");
								
								taskInstance = syntheseJournaliere.getCurrentTaskInstance(context);
								if(taskInstance != null)
								{
									action = taskInstance.getTask().getAction("Envoyer");
									if(action != null)
									{
										getWorkflowModule().end(context, taskInstance, action, "Génération automatique de la synthèse journalière");
									}
								}
								syntheseJournaliere.save(context);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.acquisitionproduction.agents.AgentDeCreationSyntheseJournalière methode execute : " + e.getClass() + " - " + e.getMessage());
		}
		
	}

	
}
