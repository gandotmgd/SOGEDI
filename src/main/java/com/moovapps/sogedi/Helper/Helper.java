package com.moovapps.sogedi.Helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.interfaces.IOptionList.IOption;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;

public class Helper extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static String getDepot(IContext context, IWorkflowModule workflowModule, IResourceDefinition refDef, IUser user) throws Exception
	{
			IViewController viewController = workflowModule.getViewController(context, IResource.class);
			viewController.addEqualsConstraint("ResponsableDepot", user);
			
			Collection<IStorageResource> storageResources = viewController.evaluate(refDef);
			if(storageResources != null && storageResources.size() > 0)
			{
				IStorageResource storageResource = storageResources.iterator().next();
				if(storageResource != null)
				{
					return (String) storageResource.getValue("NomDepot");
				}
			}

		return null;
	}
	
	public static ArrayList<IOption> getBouteilles(IContext iContext, IWorkflowModule workflowModule, IWorkflowInstance workflowInstance, IResourceDefinition resDef, String type,String emplacement, String etat,boolean epreuve) throws Exception
	{
		IViewController viewController = workflowModule.getViewController(iContext, IResource.class);
		if(epreuve == true)
		{
			viewController.addGreaterOrEqualConstraint("DateProchainControle", new Date());
		}
		if(type != null)
		{
			viewController.addEqualsConstraint("TypeGazBout", type);
		}
		viewController.addEqualsConstraint("CodeTiersMouvement", emplacement);
		if(etat != null)
		{
			viewController.addEqualsConstraint("EtatBouteille", etat);
		}
		
		
		Collection<IStorageResource> storageResources = viewController.evaluate(resDef);
		if(storageResources != null && !storageResources.isEmpty())
		{
			ArrayList<IOption> options = new ArrayList<IOption>();
			for(IStorageResource storageResource : storageResources)
			{
				options.add(workflowModule.createListOption((String)storageResource.getValue("CodeEmballage"),(String)storageResource.getValue("CodeEmballage")));				
			}
			if(options != null && options.size() > 0)
			{
				return options;
			}
		}
		return null;
	}
	
	public static IStorageResource getBouteilleByCode(IContext iContext, IWorkflowModule workflowModule, IResourceDefinition resDef, String code) throws Exception
	{
		IViewController viewController = workflowModule.getViewController(iContext, IResource.class);
		
		viewController.addEqualsConstraint("CodeEmballage", code);
			
		Collection<IStorageResource> storageResources = viewController.evaluate(resDef);
		if(storageResources != null && !storageResources.isEmpty())
		{
			return storageResources.iterator().next();
		}
		return null;
	}
}
