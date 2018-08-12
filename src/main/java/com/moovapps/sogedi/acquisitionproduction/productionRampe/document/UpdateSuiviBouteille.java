package com.moovapps.sogedi.acquisitionproduction.productionRampe.document;

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

public class UpdateSuiviBouteille extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(DeclarationRampe.class);
	
	protected IContext context = null;
	protected IOrganization organization = null;
	protected IProject project = null;
	protected ICatalog catalogRef = null;
	protected IResourceDefinition resourceDefinitionSuiviBouteille = null, refBout = null;
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		try {
			if(action.getName().equals("AccuserReception"))
			{
				context = getWorkflowModule().getSysadminContext();
				project = getWorkflowInstance().getCatalog().getProject();
				catalogRef = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
			
				//MAJ DES BOUTEILLES DE LA RAMPE DANS LES REFERENTIELS SUIVI DES BOUTEILLES ET NOUVELLE BOUTEILLE
				Collection<String> bouteillesSurRampe = (Collection<String>) getWorkflowInstance().getValue("NBouteillesProduites");
				Collection<String> bouteillesEnRebus = (Collection<String>) getWorkflowInstance().getValue("NDesBouteillesEnRebus");
				if(bouteillesSurRampe != null && bouteillesSurRampe.size()> 0)
				{
					resourceDefinitionSuiviBouteille = getWorkflowModule().getResourceDefinition(context, catalogRef, "SuiviBouteille");
					refBout = getWorkflowModule().getResourceDefinition(context, catalogRef, "Bouteilles");
					
					//getWorkflowModule().beginTransaction();
					
					for(String bouteille : bouteillesSurRampe)
					{
						if(bouteillesEnRebus != null)
						{
								if(!bouteillesEnRebus.contains(bouteille))
								{
									/***********************************************************FAIRE LA MAJ Remplie de l'USINE à l'AGENCE***************************************************/
									//SUIVI DES BOUTEILLES
									IStorageResource suivi = getWorkflowModule().createStorageResource(context, resourceDefinitionSuiviBouteille, null);
									if(suivi != null)
									{
										//Date suivi
										suivi.setValue("DateDeSuivi", new Date());
										//Numero bouteille
										suivi.setValue("CodeEmballage", bouteille);
										//Type de bouteille
											suivi.setValue("TypeGazSuivi", (String) getWorkflowInstance().getValue("TypeDeBouteilleSurLaRampe"));
										//Etat de la bouteille
										suivi.setValue("EtatBoutSuivi", "Remplie");
										//Origine
										suivi.setValue("CodeTiersOrig", "USINE");
										//Destination bouteille
										suivi.setValue("CodeTiersMouv", "AGENCE");
										//Entree/Sortie
										suivi.setValue("EntreeSortie", "Mouvement interne");
										//Date mouvement
										suivi.setValue("DateMouv", new Date());
											
										suivi.save(context);
										
										//BOUTEILLES
										IStorageResource storageResource = Helper.getBouteilleByCode(context, getWorkflowModule(), refBout, bouteille);
										if(storageResource != null)
										{
											storageResource.setValue("EtatBouteille", "Remplie");
											storageResource.setValue("CodeTiersOrig", "USINE");
											storageResource.setValue("CodeTiersMouvement", "AGENCE");
											storageResource.setValue("EntreeSortie", "Mouvement interne");
											storageResource.setValue("DateMouv", new Date());
											
											storageResource.save(context);
										}	
									}
									
									/*************************************************************************************************************************/
								}
								else
								{
									/***********************************************************FAIRE LA MAJ de Vide non traitée de l'usine à AEMD***************************************************/
									//SUIVI DES BOUTEILLES
									IStorageResource suivi = getWorkflowModule().createStorageResource(context, resourceDefinitionSuiviBouteille, null);
									if(suivi != null)
									{
										//Date suivi
										suivi.setValue("DateDeSuivi", new Date());
										//Numero bouteille
										suivi.setValue("CodeEmballage", bouteille);
										//Type de bouteille
											suivi.setValue("TypeGazSuivi", (String) getWorkflowInstance().getValue("TypeDeBouteilleSurLaRampe"));
										//Etat de la bouteille
										suivi.setValue("EtatBoutSuivi", "Vide non traitée");
										//Origine
										suivi.setValue("CodeTiersOrig", "USINE");
										//Destination bouteille
										suivi.setValue("CodeTiersMouv", "AEMD");
										//Entree/Sortie
										suivi.setValue("EntreeSortie", "Mouvement interne");
										//Date mouvement
										suivi.setValue("DateMouv", new Date());
											
										suivi.save(context);
										
										//BOUTEILLES
										IStorageResource storageResource = Helper.getBouteilleByCode(context, getWorkflowModule(), refBout, bouteille);
										if(storageResource != null)
										{
											storageResource.setValue("EtatBouteille", "Vide non traitée");
											storageResource.setValue("CodeTiersOrig", "USINE");
											storageResource.setValue("CodeTiersMouvement", "AEMD");
											storageResource.setValue("EntreeSortie", "Mouvement interne");
											storageResource.setValue("DateMouv", new Date());
											
											storageResource.save(context);
										}
			
									}
									/*************************************************************************************************************************/
								}
						}
						else
						{
							/***********************************************************FAIRE LA MAJ de Vide non traitée de l'usine à AEMD***************************************************/
							//SUIVI DES BOUTEILLES
							IStorageResource suivi = getWorkflowModule().createStorageResource(context, resourceDefinitionSuiviBouteille, null);
							if(suivi != null)
							{
								//Date suivi
								suivi.setValue("DateDeSuivi", new Date());
								//Numero bouteille
								suivi.setValue("CodeEmballage", bouteille);
								//Type de bouteille
									suivi.setValue("TypeGazSuivi", (String) getWorkflowInstance().getValue("TypeDeBouteilleSurLaRampe"));
								//Etat de la bouteille
								suivi.setValue("EtatBoutSuivi", "Remplie");
								//Origine
								suivi.setValue("CodeTiersOrig", "USINE");
								//Destination bouteille
								suivi.setValue("CodeTiersMouv", "AGENCE");
								//Entree/Sortie
								suivi.setValue("EntreeSortie", "Mouvement interne");
								//Date mouvement
								suivi.setValue("DateMouv", new Date());
									
								suivi.save(context);
								
								//BOUTEILLES
								IStorageResource storageResource = Helper.getBouteilleByCode(context, getWorkflowModule(), refBout, bouteille);
								if(storageResource != null)
								{
									storageResource.setValue("EtatBouteille", "Remplie");
									storageResource.setValue("CodeTiersOrig", "USINE");
									storageResource.setValue("CodeTiersMouvement", "AGENCE");
									storageResource.setValue("EntreeSortie", "Mouvement interne");
									storageResource.setValue("DateMouv", new Date());
									
									storageResource.save(context);
								}
	
							}
							/*************************************************************************************************************************/
						}
						
					}
					getWorkflowModule().commitTransaction();
				}
			}
			
		} catch (Exception e) {
			//getWorkflowModule().rollbackTransaction();
			//getResourceController().alert("Impossible de mettre à jour le référentiel de suivi des bouteilles.Merci de contacter votre administrateur.");
			//return false;
			log.error("Erreur dans la classe com.moovapps.sogedi.acquisitionproduction.productionRampe.document.UpdateSuiviBouteille methode onBeforeSubmit : " + e.getClass() + " - " + e.getMessage());
		}
		return super.onBeforeSubmit(action);
	}

}
