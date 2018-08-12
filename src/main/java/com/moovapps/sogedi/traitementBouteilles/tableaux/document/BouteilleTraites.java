package com.moovapps.sogedi.traitementBouteilles.tableaux.document;

import java.util.ArrayList;
import java.util.Date;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IOptionList.IOption;
import com.axemble.vdoc.sdk.utils.Logger;
import com.moovapps.sogedi.Helper.Helper;

public class BouteilleTraites extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(BouteilleTraites.class);
	
	protected IContext context = null;
	protected IOrganization organization = null;
	protected IProject project = null;
	protected ICatalog catalogRef = null;
	protected IResourceDefinition refBout = null;
	
	@Override
	public boolean onAfterLoad() {
		try {
			context = getWorkflowModule().getSysadminContext();
			organization = getDirectoryModule().getOrganization(context, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.organization.name"));
			project = getProjectModule().getProject(context, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.project.name"), organization);
			catalogRef = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
			refBout = getWorkflowModule().getResourceDefinition(context, catalogRef, "Bouteilles");
			
			ArrayList<IOption> options = Helper.getBouteilles(context, getWorkflowModule(), getWorkflowInstance(), refBout, null, "AEMD", "Vide non traitée", false);
			if(options != null && options.size() > 0)
			{
				getWorkflowInstance().setList("Numero", options);
			}
			else
			{
				getWorkflowInstance().setList("Numero", null);
				getWorkflowInstance().setValue("Numero", null);
				getResourceController().inform("Numero", "Aucune bouteille n'est disponible à l'AEMD");
			}
			
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.traitementBouteilles.tableaux.document.BouteilleTraites méthode onAfterLoad: "+e.getClass()+" - "+e.getMessage());
		}
		return super.onAfterLoad();
	}
	
	@Override
	public void onPropertyChanged(IProperty property) {
		try {
			
			if(property.getName().equals("Numero"))
			{
				String numero = (String) getWorkflowInstance().getValue("Numero");
				if(numero != null)
				{
					IStorageResource storageResource = Helper.getBouteilleByCode(context, getWorkflowModule(), refBout, numero);
					if(storageResource != null)
					{
						getWorkflowInstance().setValue("Type", (String)storageResource.getValue("TypeGazBout"));
						getWorkflowInstance().setValue("CodeGazEmballage", (String)storageResource.getValue("CodeGazEmballage"));
						getWorkflowInstance().setValue("Proprietaire", (String)storageResource.getValue("Proprietaire"));
						getWorkflowInstance().setValue("DateAchat", (Date)storageResource.getValue("DateAchat"));
						getWorkflowInstance().setValue("CodeFournisseur", (String)storageResource.getValue("CodeFournisseur"));
						getWorkflowInstance().setValue("NomFournisseur", (String)storageResource.getValue("NomFournisseur"));
						getWorkflowInstance().setValue("Statut", (String)storageResource.getValue("Statut"));
						
						getWorkflowInstance().setValue("Capacite", (Float)storageResource.getValue("Capacite"));
						getWorkflowInstance().setValue("PeriodeControle", (Float)storageResource.getValue("PeriodeControle"));
						getWorkflowInstance().setValue("Tare", (Float)storageResource.getValue("Tare"));
						getWorkflowInstance().setValue("NumeroDeFabrication", (String)storageResource.getValue("NumeroDeFabrication"));
						getWorkflowInstance().setValue("AnneeDeFabrication", (String)storageResource.getValue("AnneeDeFabrication"));
						getWorkflowInstance().setValue("PressionEpreuve", (Float)storageResource.getValue("PressionEpreuve"));
						getWorkflowInstance().setValue("CapaciteEau", (Float)storageResource.getValue("CapaciteEau"));
						getWorkflowInstance().setValue("PoidsAVide", (Float)storageResource.getValue("PoidsVide"));
						getWorkflowInstance().setValue("PressionService", (Float)storageResource.getValue("PressionService"));
						
						getWorkflowInstance().setValue("DateDernierControle", (Date)storageResource.getValue("DateDernierControle"));
						getWorkflowInstance().setValue("DateProchainControle", (Date)storageResource.getValue("DateProchainControle"));
					}
					else
					{
						getWorkflowInstance().setValue("Type", null);
						getWorkflowInstance().setValue("CodeGazEmballage", null);
						getWorkflowInstance().setValue("Proprietaire", null);
						getWorkflowInstance().setValue("DateAchat", null);
						getWorkflowInstance().setValue("CodeFournisseur", null);
						getWorkflowInstance().setValue("NomFournisseur", null);
						getWorkflowInstance().setValue("Statut", null);
						getWorkflowInstance().setValue("Capacite", null);
						getWorkflowInstance().setValue("PeriodeControle", null);
						getWorkflowInstance().setValue("Tare", null);
						getWorkflowInstance().setValue("NumeroDeFabrication", null);
						getWorkflowInstance().setValue("AnneeDeFabrication", null);
						getWorkflowInstance().setValue("PressionEpreuve", null);
						getWorkflowInstance().setValue("CapaciteEau", null);
						getWorkflowInstance().setValue("PoidsAVide", null);
						getWorkflowInstance().setValue("PressionService", null);
						
						getWorkflowInstance().setValue("DateDernierControle", null);
						getWorkflowInstance().setValue("DateProchainControle", null);
					}
				}
				else
				{
					getWorkflowInstance().setValue("Type", null);
					getWorkflowInstance().setValue("CodeGazEmballage", null);
					getWorkflowInstance().setValue("Proprietaire", null);
					getWorkflowInstance().setValue("DateAchat", null);
					getWorkflowInstance().setValue("CodeFournisseur", null);
					getWorkflowInstance().setValue("NomFournisseur", null);
					getWorkflowInstance().setValue("Statut", null);
					getWorkflowInstance().setValue("Capacite", null);
					getWorkflowInstance().setValue("PeriodeControle", null);
					getWorkflowInstance().setValue("Tare", null);
					getWorkflowInstance().setValue("NumeroDeFabrication", null);
					getWorkflowInstance().setValue("AnneeDeFabrication", null);
					getWorkflowInstance().setValue("PressionEpreuve", null);
					getWorkflowInstance().setValue("CapaciteEau", null);
					getWorkflowInstance().setValue("PoidsAVide", null);
					getWorkflowInstance().setValue("PressionService", null);
					
					getWorkflowInstance().setValue("DateDernierControle", null);
					getWorkflowInstance().setValue("DateProchainControle", null);
				}
			}
			
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.traitementBouteilles.tableaux.document.BouteilleTraites méthode onPropertyChanged: "+e.getClass()+" - "+e.getMessage());
		}
		super.onPropertyChanged(property);
	}

}
