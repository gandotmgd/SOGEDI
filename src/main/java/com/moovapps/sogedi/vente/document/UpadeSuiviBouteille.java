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
			if(action.getName().equals("Archiver"))
			{
				context = getWorkflowModule().getSysadminContext();
				project = getWorkflowInstance().getCatalog().getProject();
				catalogReservoir = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
				resourceDefinitionSuivi = getWorkflowModule().getResourceDefinition(context, catalogReservoir, "SuiviBouteille");
				String depot = (String) getWorkflowInstance().getValue("Depot");
				String client = (String) getWorkflowInstance().getValue("NomOuRaisonSociale");
				if(depot != null && client != null)
				{
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
								resourceDefinitionBouteille = getWorkflowModule().getResourceDefinition(context, catalogReservoir, "Bouteilles");
								IStorageResource bout = Helper.getBouteilleByCode(context,getWorkflowModule(),resourceDefinitionBouteille,bouteille);
								if(bout != null)
								{
									//Type de bouteille
									suivi.setValue("TypeGazSuivi",(String) bout.getValue("TypeGazBout"));
								}
								/********************************************************************************/
								
								//Etat de la bouteille
								suivi.setValue("EtatBoutSuivi", "Location");
								//Depot
								suivi.setValue("Depot", null);
								//Client
								suivi.setValue("Client", client);
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
									resourceDefinitionBouteille = getWorkflowModule().getResourceDefinition(context, catalogReservoir, "Bouteilles");
									IStorageResource bout = Helper.getBouteilleByCode(context,getWorkflowModule(),resourceDefinitionBouteille,bouteille);
									if(bout != null)
									{
										//Type de bouteille
										suivi.setValue("TypeGazSuivi",(String) bout.getValue("TypeGazBout"));
									}
									/********************************************************************************/
									
									//Etat de la bouteille
									suivi.setValue("EtatBoutSuivi", "Vide non traitée");
									//Depot
									suivi.setValue("Depot", depot);
									suivi.setValue("Client", null);
									//Origine de la bouteille
									suivi.setValue("CodeTiersOrig", client);
									//Destination bouteille
									suivi.setValue("CodeTiersMouv",depot );
									//Entree/Sortie
									suivi.setValue("EntreeSortie", "E");
									//Date mouvement
									suivi.setValue("DateMouv", new Date());
									
									suivi.save(context);
									
								}
							}
						}
					}
				}
				else
				{
					getResourceController().alert("Le dépôt et le client ne peuvent pas être vide.");
					return false;
				}
			}
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.vente.document.UpadeSuiviBouteille methode onBeforeSubmit : " + e.getClass() + " - " + e.getMessage());
		}
		return super.onBeforeSubmit(action);
	}

}
