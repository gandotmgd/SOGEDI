package com.moovapps.sogedi.vente.document;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IFile;
import com.axemble.vdoc.sdk.interfaces.IFolder;
import com.axemble.vdoc.sdk.interfaces.ILibrary;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.axemble.vdoc.sdk.utils.Logger;

public class FileCenter extends BaseDocumentExtension {

/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static final Logger log = Logger.getLogger(FileCenter.class);
	
	protected IContext context = null;
	protected IOrganization organization = null;
	protected ILibrary library = null;
	
	@Override
	public boolean onBeforeSubmit(IAction action) 
	{
		ILibraryModule libraryModule = Modules.getLibraryModule();
		try
		{
			if(action.getName().equals("Envoyer"))
			{
				//Type de document
				String typeDoc = "vente";
					
				//Date
				Date date = new Date();
				Calendar c = Calendar.getInstance();
				c.setTime(date);
					//Annee
					String annee = String.valueOf(c.get(Calendar.YEAR));
				//Client
				String client = (String) getWorkflowInstance().getValue("NomOuRaisonSociale");
				//Reference VDoc
				String referenceVDoc = (String) getWorkflowInstance().getValue("sys_Reference");
				//Numéro facture
				String numFacture = (String) getWorkflowInstance().getValue("NFacture");
				
				/*********************************************************************************************************************************************************/
				context = libraryModule.getSysadminContext();
				organization = getDirectoryModule().getOrganization(context, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.organization.name"));
				library = libraryModule.getLibrary(context, organization, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.libray.name"));
				if(library != null)
				{
					IFolder iFolderN1 = null, iFolderN2 = null, iFolderN3 = null, iFolderN4 = null;
						
					iFolderN1 = libraryModule.getFolder(context, library, "Vente de bouteilles");
					if (iFolderN1 == null)
					{
						iFolderN1 = libraryModule.createFolder(context, library, "Vente de bouteilles");
					}
						
					iFolderN2 = libraryModule.getFolder(context, iFolderN1, annee);
					if (iFolderN2 == null)
					{
						iFolderN2 = libraryModule.createFolder(context, iFolderN1, annee);
					}
								
					iFolderN3 = libraryModule.getFolder(context, iFolderN2, client);
					if (iFolderN3 == null)
					{
						iFolderN3 = libraryModule.createFolder(context, iFolderN2, client);
					}
						
					iFolderN4 = libraryModule.getFolder(context, iFolderN3, referenceVDoc);
					if (iFolderN4 == null)
					{
						iFolderN4 = libraryModule.createFolder(context, iFolderN3, referenceVDoc);
					}
						
					Collection <? extends IProperty> properties = getWorkflowInstance().getDefinition().getProperties();
					Collection <? extends IAttachment> attachments = null;

					for(IProperty property : properties)
					{
						if(property.getDisplaySettings().getType().equals(IProperty.IDisplaySettings.IType.FILE_MULTIPLE))
						{
							attachments = getWorkflowModule().getAttachments(getWorkflowInstance(), property.getName());
							if(attachments != null && attachments.size() > 0)
							{
								int i = 0;
								for(IAttachment attachment : attachments)
								{
									if(i == 0)
									{
										creationFichiers(libraryModule,library,iFolderN4,typeDoc,annee,client,referenceVDoc,numFacture,renommer(attachment, property.getLabel()));
									}
									else 
									{
										creationFichiers(libraryModule,library,iFolderN4,typeDoc,annee,client,referenceVDoc,numFacture,renommer(attachment, property.getLabel()+i));
									}
									i++;
								}
							}
						}
						}
				}else
				{
					getResourceController().alert("Impossible de trouver l'espace documentaire."+"/n"+"Merci de contacter l'administrateur");
					return false;
				}
			}
			
		}
		catch(Exception e)
		{
			log.error("Erreur dans la classe com.moovapps.sogedi.vente.document.FileCenter methode onBeforeSubmit : " + e.getClass() + " - " + e.getMessage());
		}
		finally {
			Modules.releaseModule(libraryModule);
		}
		return super.onBeforeSubmit(action);
	}
	
	public static IAttachment renommer(IAttachment attachment, String nom)
	{
			attachment.setName(nom+".pdf");
			return attachment;
	}
	
	public static void creationFichiers(ILibraryModule libraryModule, ILibrary library, IFolder iFolderN4, String typeDoc, String annee, String client, String referenceVDoc, String numFacture, IAttachment attachment)
	{
		IResourceDefinition iResourceDefinition = null;
		
		IFile iFile = null;
		try {
			IContext iContext = libraryModule.getSysadminContext();
			iResourceDefinition = libraryModule.getResourceDefinition(iContext, library, typeDoc);

			libraryModule.beginTransaction();

			iFile = libraryModule.createFile(iContext, iFolderN4, attachment.getName(), attachment);

			if (iFile != null) {
				iFile.setDefinition(iResourceDefinition);
				iFile.setName(attachment.getName());
				iFile.setValue("annee", annee);
				iFile.setValue("client", client);
				iFile.setValue("numerovente", referenceVDoc);
				iFile.setValue("numerofacture", numFacture);
				
				iFile.save(iContext);
			}
			try {
				libraryModule.commitTransaction();
			} catch (Exception e) {
				libraryModule.rollbackTransaction();
			}
			
		}catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.vente.document.FileCenter methode creationFichiers : "+e.getClass()+" - "+ e.getMessage());
			libraryModule.rollbackTransaction();
		}
	}
}
