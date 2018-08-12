package com.moovapps.sogedi.vente.document;

import java.util.Collection;
import java.util.Date;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
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
	protected IProject project = null;
	protected ICatalog catalogReservoir = null;
	protected IResourceDefinition resourceDefinitionSuivi = null,resourceDefinitionBouteille = null;
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		try {
			if(action.getName().equals("Envoyer"))
			{
				context = getWorkflowModule().getSysadminContext();
				project = getWorkflowInstance().getCatalog().getProject();
				catalogReservoir = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
				resourceDefinitionSuivi = getWorkflowModule().getResourceDefinition(context, catalogReservoir, "SuiviBouteille");
				resourceDefinitionBouteille = getWorkflowModule().getResourceDefinition(context, catalogReservoir, "Bouteilles");
				
				String depot = (String) getWorkflowInstance().getValue("Depot");
				String client = (String) getWorkflowInstance().getValue("NomOuRaisonSociale");
				
				if(depot != null && client != null)
				{
					//getWorkflowModule().beginTransaction();
					// Bouteilles vendues
					Collection<String> bouteillesVendues = (Collection<String>) getWorkflowInstance().getValue("ListeDesBouteillesVendues");
					if(bouteillesVendues != null && bouteillesVendues.size()> 0)
					{
						for(String bouteille : bouteillesVendues)
						{
							IStorageResource suivi = getWorkflowModule().createStorageResource(context, resourceDefinitionSuivi, null);
							if(suivi != null)
							{
								//Date suivi
								suivi.setValue("DateDeSuivi", new Date());
								//Numero bouteille
								suivi.setValue("CodeEmballage", bouteille);
								/********************************************************************************/
								
								IStorageResource bout = Helper.getBouteilleByCode(context, getWorkflowModule(), resourceDefinitionBouteille, bouteille);
								if(bout != null)
								{
									//Type de bouteille
									suivi.setValue("TypeGazSuivi",(String) bout.getValue("TypeGazBout"));
									
									bout.setValue("EtatBouteille", "Livrée");
									bout.setValue("CodeTiersOrig", depot);
									bout.setValue("CodeTiersMouvement", client);
									bout.setValue("EntreeSortie", "S");
									bout.setValue("DateMouv", new Date());
									
									bout.save(context);
								}
								/********************************************************************************/
								
								//Etat de la bouteille
								suivi.setValue("EtatBoutSuivi", "Livrée");

								//Origine de la bouteille
								suivi.setValue("CodeTiersOrig", depot);
								//Destination bouteille
								suivi.setValue("CodeTiersMouv", client);
								//Entree/Sortie
								suivi.setValue("EntreeSortie", "S");
								//Date mouvement
								suivi.setValue("DateMouv", new Date());
								
								suivi.save(context);
								
							}
						}
					}
					//Bouteilles rendues par le client
					String bouteilleRendue = (String) getWorkflowInstance().getValue("LeClientATIlRenduDesBouteilles");
					if(bouteilleRendue != null && bouteilleRendue.equals("Oui"))
					{
						Collection<String> bouteillesRendues = (Collection<String>) getWorkflowInstance().getValue("ListeDesBouteillesRendues");
						if(bouteillesRendues != null && bouteillesRendues.size()> 0)
						{
							for(String bouteille : bouteillesRendues)
							{
								IStorageResource suivi = getWorkflowModule().createStorageResource(context, resourceDefinitionSuivi, null);
								if(suivi != null)
								{
									//Date suivi
									suivi.setValue("DateDeSuivi", new Date());
									//Numero bouteille
									suivi.setValue("CodeEmballage", bouteille);
									/********************************************************************************/

									IStorageResource bout = Helper.getBouteilleByCode(context,getWorkflowModule(),resourceDefinitionBouteille,bouteille);
									if(bout != null)
									{
										//Type de bouteille
										suivi.setValue("TypeGazSuivi",(String) bout.getValue("TypeGazBout"));
										
										bout.setValue("EtatBouteille", "Vide non traitée");
										bout.setValue("CodeTiersOrig", client);
										if(depot.equals("AGENCE"))
										{
											bout.setValue("CodeTiersMouvement", "AEMD");
										}
										else
										{
											bout.setValue("CodeTiersMouvement", depot);
										}
										
										bout.setValue("EntreeSortie", "E");
										bout.setValue("DateMouv", new Date());
										
										bout.save(context);
									}
									/********************************************************************************/
									
									//Etat de la bouteille
									suivi.setValue("EtatBoutSuivi", "Vide non traitée");

									//Origine de la bouteille
									suivi.setValue("CodeTiersOrig", client);
									//Destination bouteille
									if(depot.equals("AGENCE"))
									{
										suivi.setValue("CodeTiersMouv","AEMD");
									}
									else
									{
										suivi.setValue("CodeTiersMouv",depot);
									}
									//Entree/Sortie
									suivi.setValue("EntreeSortie", "E");
									//Date mouvement
									suivi.setValue("DateMouv", new Date());
									
									suivi.save(context);
									
								}
							}
						}
					}
					//getWorkflowModule().commitTransaction();
				}
				else
				{
					getResourceController().alert("Le dépôt et le client ne peuvent pas être vide.");
					return false;
				}
			}
		} catch (Exception e) {
//			getWorkflowModule().rollbackTransaction();
//			getResourceController().alert("Impossible de mettre à jour le référentiel de suivi des bouteilles.Merci de contacter votre administrateur.");
//			return false;
			log.error("Erreur dans la classe com.moovapps.sogedi.vente.document.UpadeSuiviBouteille methode onBeforeSubmit : " + e.getClass() + " - " + e.getMessage());
		}
		return super.onBeforeSubmit(action);
	}

}
