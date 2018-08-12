package com.moovapps.sogedi.acquisitionproduction.campagne.document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.axemble.vdoc.sdk.interfaces.IWorkflow;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.utils.Logger;
import com.moovapps.sogedi.Helper.Helper;

public class SyntheseCampagne extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(SyntheseCampagne.class);
	
	protected IContext context = null;
	protected IProject project = null;
	protected ICatalog catalogRef = null, catalog = null;
	protected IWorkflow workflow = null;
	protected IResourceDefinition refBout = null;
	
	@Override
	public boolean onAfterLoad() {
		try {
			context = getWorkflowModule().getSysadminContext();
			project = getWorkflowInstance().getCatalog().getProject();
			catalogRef = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
			catalog = getWorkflowInstance().getCatalog();
			workflow = getWorkflowModule().getWorkflow(context, catalog, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.campagne.workflow.name"));
			refBout = getWorkflowModule().getResourceDefinition(context, catalogRef, "Bouteilles");
			/********************************************SYNTHESE DE LA CAMPAGNE*********************************************************/
			getWorkflowInstance().setValue("SyntheseCMP", null);
			Collection<? extends IWorkflowInstance> rampes = getWorkflowInstance().getLinkedWorkflowInstances("Rampes");
			if(rampes != null && rampes.size() > 0)
			{
				Collection<String> allBouteilles = getAllbouteilles(rampes);
				Map<String, Float> nombreParType = new HashMap<>();
				if(allBouteilles != null && allBouteilles.size() > 0)
				{
					nombreParType = getNombreParType(context, getWorkflowModule(), refBout, allBouteilles);
					if(nombreParType != null && nombreParType.size() > 0)
					{
						getSynthese(context, getWorkflowModule(), getWorkflowInstance(), refBout, nombreParType, allBouteilles,"SyntheseCMP");
						//getWorkflowInstance().save(context);
					}
				}
			}
			/*******************************************SYNTHESE DU MOIS********************************************************************/
			getWorkflowInstance().setValue("SyntheseMois", null);
			IViewController viewController = getWorkflowModule().getViewController(context);
			viewController.addEqualsConstraint("DocumentState", "Archivé");
			Collection<IWorkflowInstance> campagnes = viewController.evaluate(workflow);

			if(campagnes != null && campagnes.size() > 0)
			{
				for(IWorkflowInstance campagne : campagnes)
				{
					Date dateDeLaCampagne = (Date) getWorkflowInstance().getValue("DateDeLaCampagne");
					if(dateDeLaCampagne != null)
					{
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(dateDeLaCampagne);
						
						Calendar calendar2 = Calendar.getInstance();
						calendar2.setTime(new Date());
						
						if((calendar.get(Calendar.YEAR) != calendar2.get(Calendar.YEAR)) || (calendar.get(Calendar.MONTH) != calendar2.get(Calendar.MONTH)))
						{
							campagnes.remove(campagne);
						}
					}
				}
			}
			//Ajout de la campagne courante
			campagnes.add(getWorkflowInstance());

			//CREATION DE LA SYNTHESE
			if(campagnes != null && campagnes.size() > 0)
			{
			//Recuperation des categories
			Collection<String> categories = new ArrayList<String>();
			categories = getListeTypeMois(campagnes);
			if(categories != null && categories.size() > 0)
			{
				Collection<ILinkedResource> synthses = getLinesAllTableauDynamiqueMois(campagnes);
				if(synthses != null && synthses.size() > 0)
				{
					getSyntheseMois(context, getWorkflowModule(), getWorkflowInstance(), categories, synthses,"SyntheseMois");
				}
			}
			}
			//FIN CREATION DE LA SYNTHESE
			
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.acquisitionproduction.campagne.document.SyntheseCampagne methode onAfterLoad : " + e.getClass() + " - " + e.getMessage());
		}
		return super.onAfterLoad();
	}
	
	public static Collection<String> getAllbouteilles(Collection<? extends IWorkflowInstance> rampes) throws Exception
	{
		Collection<String> bouteilles = new ArrayList<String>();
		Collection<String> Numeros = new ArrayList<String>(); 
		for(IWorkflowInstance rampe : rampes)
		{
			Numeros = (Collection<String>) rampe.getValue("NBouteillesProduites");
			if(Numeros != null && Numeros.size() > 0)
			{
				bouteilles.addAll(Numeros);
			}
		}
		if(bouteilles != null && bouteilles.size() > 0)
		{
			return bouteilles;
		}
		
		return null;
	}
	
	public static Map<String, Float> getNombreParType(IContext iContext, IWorkflowModule workflowModule, IResourceDefinition ref, Collection<String> bouteilles) throws Exception
	{
		Map<String, Float> liste = new HashMap<>();
		for(String bouteille : bouteilles)
		{
			IStorageResource storageResource = Helper.getBouteilleByCode(iContext, workflowModule, ref, bouteille);
			if(storageResource != null)
			{
				if(storageResource.getValue("TypeGazBout") != null)
				{
					if(!liste.containsKey((String) storageResource.getValue("TypeGazBout")))
					{
						liste.put((String) storageResource.getValue("TypeGazBout"), 1F);
					}
					else
					{
						for(Entry<String, Float> dataEntry : liste.entrySet())
						{
							if(dataEntry.getKey().equals((String) storageResource.getValue("TypeGazBout")))
							{
								dataEntry.setValue(dataEntry.getValue() + 1F);
							}
						}
					}	
				}
			}
		}
		return liste;
	}
	
	public static void getSynthese(IContext iContext, IWorkflowModule workflowModule, IWorkflowInstance workflowInstance, IResourceDefinition ref, Map<String, Float> nbreParType, Collection<String> bouteilles,String nomTableauCible) throws Exception
	{
		for(Entry<String, Float> dataEntry : nbreParType.entrySet())
		{
			Float volume = 0F;
			for(String numeroBout : bouteilles)
			{
				IStorageResource storageResource = Helper.getBouteilleByCode(iContext, workflowModule, ref, numeroBout);
				if(storageResource != null)
				{
					if(dataEntry.getKey().equals(storageResource.getValue("TypeGazBout")))
					{
						volume += (Float) storageResource.getValue("Capacite");
					}
				}
			}
			createLineTableau(iContext, workflowInstance, dataEntry.getKey(), dataEntry.getValue(), volume,nomTableauCible);
		}
		
	}
	
	public static void createLineTableau(IContext iContext, IWorkflowInstance workflowInstance, String type,Float nombre,Float volume,String nomTableauCible) throws Exception
	{
		ILinkedResource linkedResource = workflowInstance.createLinkedResource(nomTableauCible);
		if(linkedResource != null)
		{
			linkedResource.setValue("Type", type);
			linkedResource.setValue("Nombre", nombre);
			linkedResource.setValue("Volume", volume);
			
			linkedResource.save(iContext);
			workflowInstance.addLinkedResource(linkedResource);
			workflowInstance.save(iContext);
		}
	}
	
	private Collection<String> getListeTypeMois(Collection<IWorkflowInstance> workflowInstances) throws Exception
	{
		Collection<String> categories = new ArrayList<String>();
		for(IWorkflowInstance workflowInstance : workflowInstances)
		{
			Collection<? extends ILinkedResource> linkedResources = workflowInstance.getLinkedResources("SyntheseCMP");
			if(linkedResources != null && linkedResources.size() > 0)
			{
				for(ILinkedResource linkedResource : linkedResources)
				{
					if(linkedResource.getValue("Type") != null && !categories.contains((String) linkedResource.getValue("Type")))
					{
						categories.add((String) linkedResource.getValue("Type"));
					}
					
				}
			}
		}
		return categories;
	}
	
	private Collection<ILinkedResource> getLinesAllTableauDynamiqueMois(Collection<IWorkflowInstance> workflowInstances) throws Exception
	{
		Collection<ILinkedResource> linkedResources = new ArrayList<ILinkedResource>();
		for(IWorkflowInstance workflowInstance : workflowInstances)
		{
			Collection<? extends ILinkedResource> syntheses = workflowInstance.getLinkedResources("SyntheseCMP");
			if(syntheses != null && syntheses.size() > 0)
			{
				for(ILinkedResource synthese : syntheses)
				{
					linkedResources.add(synthese);
				}
			}
			
		}
		return linkedResources;
	}
	
	private void getSyntheseMois(IContext iContext, IWorkflowModule workflowModule, IWorkflowInstance workflowInstance, Collection<String> listeTypeGaz,Collection<ILinkedResource> linkedResources,String nomTableauCible) throws Exception
	{
		for(String type : listeTypeGaz)
		{
			Float volume = 0F, nombre = 0F;
			for(ILinkedResource linkedResource : linkedResources)
			{
				if(type.equals(linkedResource.getValue("Type")))
				{
					volume += (Float) linkedResource.getValue("Volume");
					nombre += (Float) linkedResource.getValue("Nombre");
				}
			}
			createLineTableau(iContext, workflowInstance, type, nombre, volume,nomTableauCible);
		}
		
	}


}
