package com.moovapps.sogedi.Helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.interfaces.IOptionList.IOption;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;

public class Helper extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static ArrayList<IOption> getBouteillesAProduire(IContext iContext, IWorkflowModule workflowModule, IWorkflowInstance workflowInstance, IResourceDefinition resDef, String type,String depot, String etatBout) throws Exception
	{
		IViewController viewController = workflowModule.getViewController(iContext, IResource.class);
		
		viewController.addEqualsConstraint("TypeGazSuivi", type);
//		if(type.equals("Acétylène N26") || type.equals("Acétylène dissout"))
//		{
//			viewController.addLessConstraint("NiveauChargeBoutSuivi", 3);
//		}
		//viewController.addEqualsConstraint("Depot", depot);
				
		viewController.setOrderBy("DateDeSuivi", Date.class, false);
		
		Collection<IStorageResource> storageResources = viewController.evaluate(resDef);
		if(storageResources != null && !storageResources.isEmpty())
		{
			Map<String,String> listeNumero = new HashMap<>();
			String numero = null;
			for(IStorageResource storageResource : storageResources)
			{
				if(storageResource != null)
				{
					
					numero = (String)storageResource.getValue("CodeEmballage"); 
					if(numero != null && !listeNumero.containsValue(numero))
					{
						listeNumero.put((String) storageResource.getValue("sys_Reference"),numero);
					}
					
				}
				
			}
			if(listeNumero != null)
			{ 
				int statut = 0;
				ArrayList<IOption> options = new ArrayList<IOption>();
				//optionsSysRef = new HashMap<>();
				for(Entry<String, String> dataEntry : listeNumero.entrySet())
				{
					statut = getStatusbyEtatDepot(iContext, workflowModule, resDef, dataEntry.getKey(), etatBout,depot);
					if(statut == 1)
					{
						options.add(workflowModule.createListOption(dataEntry.getValue(),dataEntry.getValue()));
						//optionsSysRef.put(dataEntry.getKey(),dataEntry.getValue());
					}
				}
				if(options != null && options.size() > 0)
				{
					return options;
				}
				else
				{
					return null;
				}
			}
		}
		return null;
	}
	
	public static ArrayList<IOption> getBouteillesVente(IContext iContext, IWorkflowModule workflowModule, IWorkflowInstance workflowInstance, IResourceDefinition resDef, String depot, String etatBout) throws Exception
	{
		IViewController viewController = workflowModule.getViewController(iContext, IResource.class);
						//viewController.addEqualsConstraint("Depot", depot);	
		viewController.setOrderBy("DateDeSuivi", Date.class, false);
		
		Collection<IStorageResource> storageResources = viewController.evaluate(resDef);
		if(storageResources != null && !storageResources.isEmpty())
		{
			Map<String,String> listeNumero = new HashMap<>();
			String numero = null;
			for(IStorageResource storageResource : storageResources)
			{
				if(storageResource != null)
				{
					
					numero = (String)storageResource.getValue("CodeEmballage"); 
					//String etat = (String)storageResource.getValue("EtatBoutSuivi"); 
					if(numero != null && !listeNumero.containsValue(numero))
					{
						listeNumero.put((String) storageResource.getValue("sys_Reference"),numero);
					}
				}
				
			}
			if(listeNumero != null)
			{ 
				int statut = 0;
				ArrayList<IOption> options = new ArrayList<IOption>();
				for(Entry<String, String> dataEntry : listeNumero.entrySet())
				{
					statut = getStatusbyEtatDepot(iContext, workflowModule, resDef, dataEntry.getKey(), etatBout,depot);
					if(statut == 1)
					{
						options.add(workflowModule.createListOption(dataEntry.getValue(),dataEntry.getValue()));
						//optionsSysRef.put(dataEntry.getKey(),dataEntry.getValue());
					}
				}
				if(options != null && options.size() > 0)
				{
					return options;
				}
				else
				{
					return null;
				}
			}
		}
		return null;
	}
	
	public static ArrayList<IOption> getBouteillesARendre(IContext iContext, IWorkflowModule workflowModule, IWorkflowInstance workflowInstance, IResourceDefinition resDef, String client, String etatBout) throws Exception
	{
		IViewController viewController = workflowModule.getViewController(iContext, IResource.class);

		viewController.setOrderBy("DateDeSuivi", Date.class, false);
		
		Collection<IStorageResource> storageResources = viewController.evaluate(resDef);
		if(storageResources != null && !storageResources.isEmpty())
		{
			Map<String,String> listeNumero = new HashMap<>();
			String numero = null;
			for(IStorageResource storageResource : storageResources)
			{
				if(storageResource != null)
				{
					
					numero = (String)storageResource.getValue("CodeEmballage"); 
					if(numero != null && !listeNumero.containsValue(numero))
					{
						listeNumero.put((String) storageResource.getValue("sys_Reference"),numero);
					}
				}
				
			}
			if(listeNumero != null)
			{ 
				int statut = 0;
				ArrayList<IOption> options = new ArrayList<IOption>();
				for(Entry<String, String> dataEntry : listeNumero.entrySet())
				{
					statut = getStatusEtatClient(iContext, workflowModule, resDef, dataEntry.getKey(), etatBout,client);
					if(statut == 1)
					{
						options.add(workflowModule.createListOption(dataEntry.getValue(),dataEntry.getValue()));
						//optionsSysRef.put(dataEntry.getKey(),dataEntry.getValue());
					}
				}
				if(options != null && options.size() > 0)
				{
					return options;
				}
				else
				{
					return null;
				}
			}
		}
		return null;
	}
		
	public static int getStatus(IContext icontext, IWorkflowModule workflowModule, IResourceDefinition res, String reference,String etatSuivi) throws WorkflowModuleException
	{
		IViewController controller = workflowModule.getViewController(icontext,IResource.class);
		
		controller.addEqualsConstraint("sys_Reference", reference);
		Collection<? extends IStorageResource> storageResources = controller.evaluate(res);
		if(storageResources != null && storageResources.size() > 0)
		{
			IStorageResource storageResource = storageResources.iterator().next();
			String etat = (String) storageResource.getValue("EtatBoutSuivi");
			if(etat != null && etat.equals(etatSuivi))
			{
				return 1;
			}
		}
		return 0;
	}
	
	public static int getStatusEtatClient(IContext icontext, IWorkflowModule workflowModule, IResourceDefinition res, String reference,String etatSuivi, String client) throws WorkflowModuleException
	{
		IViewController controller = workflowModule.getViewController(icontext,IResource.class);
		
		controller.addEqualsConstraint("sys_Reference", reference);
		Collection<? extends IStorageResource> storageResources = controller.evaluate(res);
		if(storageResources != null && storageResources.size() > 0)
		{
			IStorageResource storageResource = storageResources.iterator().next();
			String etat = (String) storageResource.getValue("EtatBoutSuivi");
			String nomClient = (String) storageResource.getValue("Client");
			if(etat != null && etat.equals(etatSuivi) && nomClient != null && nomClient.equals(client))
			{
				return 1;
			}
		}
		return 0;
	}
	
	public static int getStatusbyEtatDepot(IContext icontext, IWorkflowModule workflowModule, IResourceDefinition res, String reference,String etatSuivi, String depot) throws WorkflowModuleException
	{
		IViewController controller = workflowModule.getViewController(icontext,IResource.class);
		
		controller.addEqualsConstraint("sys_Reference", reference);
		Collection<? extends IStorageResource> storageResources = controller.evaluate(res);
		if(storageResources != null && storageResources.size() > 0)
		{
			IStorageResource storageResource = storageResources.iterator().next();
			String etat = (String) storageResource.getValue("EtatBoutSuivi");
			String nomDepot = (String) storageResource.getValue("Depot");
			if(etat != null && etat.equals(etatSuivi) && nomDepot != null && nomDepot.equals(depot))
			{
				return 1;
			}
		}
		return 0;
	}

	public static IStorageResource getBouteilleByCode(IContext context, IWorkflowModule workflowModule, IResourceDefinition res, String codeBouteille) throws Exception
	{
		IViewController controller = workflowModule.getViewController(context,IResource.class);
						controller.addEqualsConstraint("CodeEmballage", codeBouteille);				
		Collection<? extends IStorageResource> storageResources = controller.evaluate(res);
		if(storageResources != null && storageResources.size() > 0)
		{
			return  storageResources.iterator().next();
		}
		return null;
	}
}
