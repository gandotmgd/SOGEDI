package com.moovapps.sogedi.acquisitionproduction.campagne;

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

public class EnregistrementDeLaProduction extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(EnregistrementDeLaProduction.class);
	
	protected IContext context = null;
	protected IProject project = null;
	protected ICatalog catalogReservoir = null;
	protected IResourceDefinition resourceDefinitionSuivi = null;
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		try {
			context = getWorkflowModule().getSysadminContext();
			project = getWorkflowInstance().getCatalog().getProject();
			catalogReservoir = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
			resourceDefinitionSuivi = getWorkflowModule().getResourceDefinition(context, catalogReservoir, "SuiviBouteille");
			
			Collection<? extends ILinkedResource> gazAir = getWorkflowInstance().getLinkedResources("Oxygene");
			if(gazAir != null && gazAir.size() > 0)
			{
				for(ILinkedResource linkedResource : gazAir)
				{
					IStorageResource storageResource = getWorkflowModule().createStorageResource(context, resourceDefinitionSuivi, null);
					                 storageResource.setValue("DateSuivi", new Date());
									 storageResource.setValue("NumeroBoutSuivi", (String) linkedResource.getValue("Numero"));
									 storageResource.setValue("TypeBoutSuivi", "Gaz de l'air");
									 storageResource.setValue("TypeGazSuivi", (String) linkedResource.getValue("TypeDeGaz_GA"));
									 storageResource.setValue("CapaciteBoutSuivi", (float) linkedResource.getValue("CapaciteBoutOG"));
									 storageResource.setValue("EtatBoutSuivi", "Remplie");
									 storageResource.setValue("Depot", "SIEGE");
									 storageResource.setValue("DateDEpreuveSuivi", (Date) linkedResource.getValue("DateDEpreuve"));
									 storageResource.setValue("PoidsAVideSuivi", (float) linkedResource.getValue("PoidsAVide"));
									 storageResource.setValue("PoidsSuivi", (float) linkedResource.getValue("Poids"));
									 
									 storageResource.save(context);
				}
			}
			else
			{
				Collection<? extends ILinkedResource> acetylene = getWorkflowInstance().getLinkedResources("Acetylene");
				if(acetylene != null && acetylene.size() > 0)
				{
					String necessiteAutreCharge = null; Integer niveauCharge = null;
					for(ILinkedResource linkedResource : acetylene)
					{
						
						IStorageResource storageResource = getWorkflowModule().createStorageResource(context, resourceDefinitionSuivi, null);
		                 storageResource.setValue("DateSuivi", new Date());
						 storageResource.setValue("NumeroBoutSuivi", (String) linkedResource.getValue("NumeroDesBouteilles"));
						 storageResource.setValue("TypeBoutSuivi", "Acétylène");
						 storageResource.setValue("CapaciteBoutSuivi", (float) linkedResource.getValue("CapaciteEnM3"));
						 
						 necessiteAutreCharge = (String) linkedResource.getValue("NecessiteTElleUneAutreCharge");
						 if(necessiteAutreCharge != null && necessiteAutreCharge.equals("Oui"))
						 {
							 niveauCharge = Integer.parseInt((String)linkedResource.getValue("NiveauCharge"));
							 if(niveauCharge != null)
							 {
								 if(niveauCharge + 1 == 3)
								 {
									 storageResource.setValue("EtatBoutSuivi", "Vide non traitée");
								 }
								 else
								 {
									 storageResource.setValue("EtatBoutSuivi", "Vide traitée");
									 storageResource.setValue("NiveauChargeBoutSuivi", String.valueOf(niveauCharge + 1));
								 }
							 }
							
						 }
						 else
						 {
							 storageResource.setValue("EtatBoutSuivi", "Remplie");

							 niveauCharge = Integer.parseInt((String)linkedResource.getValue("NiveauCharge"));
							 
							 storageResource.setValue("NiveauChargeBoutSuivi", String.valueOf(niveauCharge + 1));
						 }
						 storageResource.setValue("Depot", "SIEGE");
						 storageResource.setValue("DateDEpreuveSuivi", (Date) linkedResource.getValue("DateDEpreuve"));
						 storageResource.setValue("PoidsAVideSuivi", (float) linkedResource.getValue("Tare"));
						 storageResource.setValue("PoidsSuivi", (float) linkedResource.getValue("Poids"));
						 
						 storageResource.save(context);
					}
				}
			}
			
		} catch (Exception e) {
			log.error("Erreur dans la classe EnregistrementDeLaProduction méthode onBeforeSubmit "+e.getClass()+" - "+e.getMessage());
		}
		return super.onBeforeSubmit(action);
	}

}
