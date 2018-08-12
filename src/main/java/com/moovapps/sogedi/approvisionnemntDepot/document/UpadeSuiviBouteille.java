package com.moovapps.sogedi.approvisionnemntDepot.document;

import java.util.Collection;
import java.util.Date;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.utils.Logger;
import com.moovapps.sogedi.Helper.Helper;

public class UpadeSuiviBouteille extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(UpadeSuiviBouteille.class);
	
	protected IContext context = null;
	protected IOrganization organization = null;
	protected IProject project = null;
	protected ICatalog catalogRef = null;
	protected IResourceDefinition resourceDefinitionSuiviBouteille = null, refBout = null;
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		try {
			if(action.getName().equals("Cloturer"))
			{
				context = getWorkflowModule().getSysadminContext();
				project = getWorkflowInstance().getCatalog().getProject();
				catalogRef = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
				resourceDefinitionSuiviBouteille = getWorkflowModule().getResourceDefinition(context, catalogRef, "SuiviBouteille");
				refBout = getWorkflowModule().getResourceDefinition(context, catalogRef, "Bouteilles");
				
				String depot = (String) getWorkflowInstance().getValue("Depot");
				if(depot != null)
				{
					//BOUTEILLES RENDUES
					Collection<String> bouteillesRendues = (Collection<String>) getWorkflowInstance().getValue("BouteillesARendre");
					if(bouteillesRendues != null && bouteillesRendues.size() > 0)
					{
						for(String bouteilleRendue : bouteillesRendues)
						{
							IStorageResource suivi = getWorkflowModule().createStorageResource(context, resourceDefinitionSuiviBouteille, null);
							if(suivi != null)
							{
								//Date suivi
								suivi.setValue("DateDeSuivi", new Date());
								//Numero bouteille
								suivi.setValue("CodeEmballage", bouteilleRendue);
								/********************************************************************************/
								IStorageResource bout = Helper.getBouteilleByCode(context, getWorkflowModule(), refBout, bouteilleRendue);
								if(bout != null)
								{
									//Type de bouteille
									suivi.setValue("TypeGazSuivi",(String) bout.getValue("TypeGazBout"));
									
									bout.setValue("EtatBouteille", "Vide non traitée");
									bout.setValue("CodeTiersOrig", depot);
									bout.setValue("CodeTiersMouvement", "AEMD");
									bout.setValue("EntreeSortie", "E");
									bout.setValue("DateMouv", new Date());
									
									bout.save(context);
								}
								/********************************************************************************/
								//Etat de la bouteille
								suivi.setValue("EtatBoutSuivi", "Vide non traitée");
								//Origine
								suivi.setValue("CodeTiersOrig", depot);
								//Destination bouteille
								suivi.setValue("CodeTiersMouv", "AEMD");
								//Entree/Sortie
								suivi.setValue("EntreeSortie", "E");
								//Date mouvement
								suivi.setValue("DateMouv", new Date());
									
								suivi.save(context);
							}
						}
					}
					//BOUTEILLES APPROVISIONNNER
					Collection<String> bouteillesApprovisionnees = (Collection<String>) getWorkflowInstance().getValue("BouteillesApprovisionnees");
					Collection<String> bouteillesRebus = (Collection<String>) getWorkflowInstance().getValue("BouteillesRebus");
					if(bouteillesApprovisionnees != null && bouteillesApprovisionnees.size() > 0)
					{
						for(String bouteilleApprovisionnee : bouteillesApprovisionnees)
						{
							if(bouteillesRebus != null && !bouteillesApprovisionnees.contains(bouteilleApprovisionnee))
							{
								//Mettre la bouteille à l'état Remplie dans ce dépôt dont l'origine est AGENCE
								IStorageResource suivi = getWorkflowModule().createStorageResource(context, resourceDefinitionSuiviBouteille, null);
								if(suivi != null)
								{
									//Date suivi
									suivi.setValue("DateDeSuivi", new Date());
									//Numero bouteille
									suivi.setValue("CodeEmballage", bouteilleApprovisionnee);
									/********************************************************************************/
									IStorageResource bout = Helper.getBouteilleByCode(context, getWorkflowModule(), refBout, bouteilleApprovisionnee);
									if(bout != null)
									{
										//Type de bouteille
										suivi.setValue("TypeGazSuivi",(String) bout.getValue("TypeGazBout"));
										
										bout.setValue("EtatBouteille", "Remplie");
										bout.setValue("CodeTiersOrig", "AGENCE");
										bout.setValue("CodeTiersMouvement", depot);
										bout.setValue("EntreeSortie", "S");
										bout.setValue("DateMouv", new Date());
										
										bout.save(context);
									}
									/********************************************************************************/
									//Etat de la bouteille
									suivi.setValue("EtatBoutSuivi", "Remplie");
									//Origine
									suivi.setValue("CodeTiersOrig", "AGENCE");
									//Destination bouteille
									suivi.setValue("CodeTiersMouv", depot);
									//Entree/Sortie
									suivi.setValue("EntreeSortie", "S");
									//Date mouvement
									suivi.setValue("DateMouv", new Date());
										
									suivi.save(context);
								}
								
							}
							else
							{
								//Mettre la bouteille à l'état Vide non traitée dans ce dépôt dont l'origine est AGENCE
								IStorageResource suivi = getWorkflowModule().createStorageResource(context, resourceDefinitionSuiviBouteille, null);
								if(suivi != null)
								{
									//Date suivi
									suivi.setValue("DateDeSuivi", new Date());
									//Numero bouteille
									suivi.setValue("CodeEmballage", bouteilleApprovisionnee);
									/********************************************************************************/
									IStorageResource bout = Helper.getBouteilleByCode(context, getWorkflowModule(), refBout, bouteilleApprovisionnee);
									if(bout != null)
									{
										//Type de bouteille
										suivi.setValue("TypeGazSuivi",(String) bout.getValue("TypeGazBout"));
										
										bout.setValue("EtatBouteille", "Vide non traitée");
										bout.setValue("CodeTiersOrig", "AGENCE");
										bout.setValue("CodeTiersMouvement", depot);
										bout.setValue("EntreeSortie", "S");
										bout.setValue("DateMouv", new Date());
										
										bout.save(context);
									}
									/********************************************************************************/
									//Etat de la bouteille
									suivi.setValue("EtatBoutSuivi", "Vide non traitée");
									//Origine
									suivi.setValue("CodeTiersOrig", "AGENCE");
									//Destination bouteille
									suivi.setValue("CodeTiersMouv", depot);
									//Entree/Sortie
									suivi.setValue("EntreeSortie", "S");
									//Date mouvement
									suivi.setValue("DateMouv", new Date());
										
									suivi.save(context);
								}
							}
							
						}
					}
				}			
			}
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.approvisionnemntDepot.document.UpadeSuiviBouteille methode onBeforeSubmit : " + e.getClass() + " - " + e.getMessage());
		}
		return super.onBeforeSubmit(action);
	}

}
