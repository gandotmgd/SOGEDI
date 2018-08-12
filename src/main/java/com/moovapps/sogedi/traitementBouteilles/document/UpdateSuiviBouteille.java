package com.moovapps.sogedi.traitementBouteilles.document;

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

public class UpdateSuiviBouteille extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(UpdateSuiviBouteille.class);
	
	protected IContext context = null;
	protected IProject project = null;
	protected ICatalog catalogReservoir = null;
	protected IResourceDefinition resourceDefinitionSuivi = null,resourceDefinitionBouteille = null;
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		try {
			if(action.getName().equals("Valider"))
			{
				context = getWorkflowModule().getSysadminContext();
				project = getWorkflowInstance().getCatalog().getProject();
				catalogReservoir = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
				resourceDefinitionSuivi = getWorkflowModule().getResourceDefinition(context, catalogReservoir, "SuiviBouteille");
				resourceDefinitionBouteille = getWorkflowModule().getResourceDefinition(context, catalogReservoir, "Bouteilles");
				
				Collection<? extends ILinkedResource> linkedResources = getWorkflowInstance().getLinkedResources("BouteillesTraitees");
				if(linkedResources != null && linkedResources.size() > 0)
				{
					String actionEffectuee = null;
					for(ILinkedResource linkedResource : linkedResources)
					{
						IStorageResource suivi = getWorkflowModule().createStorageResource(context, resourceDefinitionSuivi, null);
						if(suivi != null)
						{
							//Date suivi
							suivi.setValue("DateDeSuivi", new Date());
							//Numero bouteille
							suivi.setValue("CodeEmballage", (String) linkedResource.getValue("Numero"));
							//Date mouvement
							suivi.setValue("DateMouv", new Date());
							/*********************************************************************/
							IStorageResource bout = Helper.getBouteilleByCode(context, getWorkflowModule(), resourceDefinitionBouteille, (String) linkedResource.getValue("Numero"));
							if(bout != null)
							{
								//Type de bouteille
								suivi.setValue("TypeGazSuivi",(String)linkedResource.getValue("Type"));
								//Bout
								bout.setValue("TypeGazBout",(String)linkedResource.getValue("Type"));
								bout.setValue("CodeGazEmballage",(String)linkedResource.getValue("CodeGazEmballage"));
								bout.setValue("Proprietaire",(String)linkedResource.getValue("Proprietaire"));
								bout.setValue("DateAchat",(Date)linkedResource.getValue("DateAchat"));
								bout.setValue("CodeFournisseur",(String)linkedResource.getValue("CodeFournisseur"));
								bout.setValue("NomFournisseur",(String)linkedResource.getValue("NomFournisseur"));
								bout.setValue("Statut",(String)linkedResource.getValue("Statut"));
								
								bout.setValue("Capacite", (Float) linkedResource.getValue("Capacite"));
								bout.setValue("PeriodeControle", (Float) linkedResource.getValue("PeriodeControle"));
								bout.setValue("Tare", (Float) linkedResource.getValue("Tare"));
								bout.setValue("NumeroDeFabrication", (String) linkedResource.getValue("NumeroDeFabrication"));
								bout.setValue("AnneeDeFabrication", (String) linkedResource.getValue("AnneeDeFabrication"));
								bout.setValue("PressionEpreuve", (Float) linkedResource.getValue("PressionEpreuve"));
								bout.setValue("CapaciteEau", (Float) linkedResource.getValue("CapaciteEau"));
								bout.setValue("PoidsVide", (Float) linkedResource.getValue("PoidsAVide"));
								bout.setValue("PressionService", (Float) linkedResource.getValue("PressionService"));
								
								bout.setValue("DateDernierControle",(Date)linkedResource.getValue("DateDernierControle"));
								bout.setValue("DateProchainControle",(Date)linkedResource.getValue("DateProchainControle"));
								
								bout.setValue("EtatBouteille", "Vide traitée");
								bout.setValue("CodeTiersOrig", "AEMD");
								bout.setValue("CodeTiersMouvement", "USINE");
								bout.setValue("EntreeSortie", "Mouvement interne");
								bout.setValue("DateMouv", new Date());
								
								//Etat de la bouteille
								suivi.setValue("EtatBoutSuivi", "Vide traitée");

								//Origine de la bouteille
								suivi.setValue("CodeTiersOrig", "AEMD");
								//Destination bouteille
								suivi.setValue("CodeTiersMouv", "USINE");
								//Entree/Sortie
								suivi.setValue("EntreeSortie", "Mouvement interne");
								//Date mouvement
								suivi.setValue("DateMouv", new Date());
								
								actionEffectuee = (String) linkedResource.getValue("ActionEffectuee");
								if(actionEffectuee != null && actionEffectuee.equals("Mutation"))
								{
									bout.setValue("DateMutation", new Date());
								}
								bout.save(context);
							}
							/*********************************************************************/
							
							suivi.save(context);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.traitementBouteilles.document.UpdateSuiviBouteille méthode onPropertyChanged: "+e.getClass()+" - "+e.getMessage());
		}
		return super.onBeforeSubmit(action);
	}

}
