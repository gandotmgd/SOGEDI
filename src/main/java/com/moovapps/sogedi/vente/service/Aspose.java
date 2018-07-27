package com.moovapps.sogedi.vente.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.ModuleException;
import com.axemble.vdoc.sdk.exceptions.SDKException;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IConfiguration;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IFile;
import com.axemble.vdoc.sdk.interfaces.ILibrary;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IResourceController;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.ui.framework.foundation.Navigator;

public class Aspose extends BaseDocumentExtension
{
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(Aspose.class);
	
	public void GenerationDuDocumentPDF (IWorkflowModule workflowModule , IWorkflowInstance workflowInstance ,IResourceController resourceController,
			String cheminModeleWord , String champPJName, String titrePJOutput)
	{
		ILibraryModule libraryModule = Modules.getLibraryModule();
		IDirectoryModule directoryModule = Modules.getDirectoryModule();
		try
		{
			IContext context = libraryModule.getContextByLogin("sysadmin");
			IOrganization organization = DirectoryService.getOrganization(directoryModule, workflowModule);
			IConfiguration configuration = workflowModule.getConfiguration();
			//----------------------------------------- Paramètres ------------------------------------
			
			String libraryName = configuration.getStringProperty("com.moovapps.sogedi.libray.name").trim();
			String cheminTempFolder = configuration.getStringProperty("com.moovapps.sogedi.temp.folder").trim();
			
			ILibrary library = libraryModule.getLibrary(context, organization, libraryName);
			if(library == null)
			{
				resourceController.alert("Le chemin du fichier de modele n'existe pas dans l'espace documentaire...Vieullez contacter votre administrateur");
			}
								
			IFile file = libraryModule.getFileByPath(context, library, cheminModeleWord);
			if (file == null)
			{
				getResourceController().alert("Le fichier de modele n'existe pas dans l'espace documentaire...Vieullez contacter votre administrateur");
			}
			
			GenerateWordFile generateWordFile = new GenerateWordFile();
			IAttachment attachement = libraryModule.getAttachment(file, file.getName());
			InputStream inputStream = generateWordFile.valorization(workflowModule, workflowInstance, attachement);
			
			
			Document document = new Document(inputStream);
			document.save(cheminTempFolder+ "\\" + titrePJOutput + ".pdf", SaveFormat.PDF);
			workflowInstance.setValue(champPJName, null);
			workflowModule.addAttachment(workflowInstance, champPJName, new File(cheminTempFolder+ "\\" + titrePJOutput + ".pdf"));
			workflowInstance.save(champPJName);
			
			File MyFile = new File(cheminTempFolder+ "\\" + titrePJOutput + ".pdf");
			MyFile.delete();
		}
		catch (IOException | ModuleException | SDKException e)
		{
			if (libraryModule.isTransactionActive())
			{
				libraryModule.rollbackTransaction();
			}

			String message = "Error in Aspose GenerationDuDocument method : " + e.getClass() + " - " + e.getMessage();
			Navigator.getNavigator().getRootNavigator().showAlertBox(message);
		}
		catch (Exception e)
		{
			String message = e.getMessage();
			if (message == null)
			{
				message = "";
			}
			e.printStackTrace();
			log.error("Error in Aspose GenerationDuDocument method : " + e.getClass() + " - " + message);
		}
		finally
		{
			if (!libraryModule.isTransactionActive())
			{
				Modules.releaseModule(libraryModule);
			}

			Modules.releaseModule(directoryModule);
		}
		
	} 
	
}