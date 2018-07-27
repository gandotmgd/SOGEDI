package com.moovapps.sogedi.vente.service;

import com.axemble.vdoc.sdk.interfaces.IConfiguration;
import com.axemble.vdoc.sdk.interfaces.IResourceController;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;


public class HelperAspose
{
	// GENERATION DE LA FACTURE
	public void genererFacture (IWorkflowModule workflowModule, IWorkflowInstance workflowInstance, IResourceController resourceController)
	{
		try
		{
			workflowInstance.save(workflowModule.getLoggedOnUserContext());
			IConfiguration configuration = workflowModule.getConfiguration();
			
			String cheminModeleWord = configuration.getStringProperty("com.moovapps.sogedi.vente.libray.modele.facture.path").trim();
			String champPJName = configuration.getStringProperty("com.moovapps.sogedi.vente.facture.champ.name").trim();
			String titrePJOutput = configuration.getStringProperty("com.moovapps.sogedi.vente.facture.titre").trim();
			
			Aspose aspose = new Aspose();
			aspose.GenerationDuDocumentPDF(workflowModule, workflowInstance, resourceController, cheminModeleWord, champPJName, titrePJOutput);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	// GENERATION DU BORDEREAU DE MOUVEMENT DE BOUTEILLE
	public void genererBMB (IWorkflowModule workflowModule, IWorkflowInstance workflowInstance, IResourceController resourceController)
	{
		try
		{
			workflowInstance.save(workflowModule.getLoggedOnUserContext());
			IConfiguration configuration = workflowModule.getConfiguration();
			
			String cheminModeleWord = configuration.getStringProperty("com.moovapps.sogedi.vente.libray.modele.bmb.path").trim();
			String champPJName = configuration.getStringProperty("com.moovapps.sogedi.vente.bmb.champ.name").trim();
			String titrePJOutput = configuration.getStringProperty("com.moovapps.sogedi.vente.bmb.titre").trim();
			
			Aspose aspose = new Aspose();
			aspose.GenerationDuDocumentPDF(workflowModule, workflowInstance, resourceController, cheminModeleWord, champPJName, titrePJOutput);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
		
}
