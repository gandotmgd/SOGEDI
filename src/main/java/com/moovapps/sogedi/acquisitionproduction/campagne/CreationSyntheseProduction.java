package com.moovapps.sogedi.acquisitionproduction.campagne;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IConnectionDefinition;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.utils.Logger;

public class CreationSyntheseProduction extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4516237350580553840L;
	protected static final Logger log = Logger.getLogger(CreationSyntheseProduction.class);
	
	protected IContext context = null;
	protected IConnectionDefinition<java.sql.Connection> connectionDefinition = null;
	protected Connection connection = null;
	protected PreparedStatement statement = null;
	protected ResultSet resultSet = null;
	protected String query = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean onBeforeSubmit(IAction action) {
		try 
		{	
			if(action.getName().equals("Accepter"))
			{
				context = getWorkflowModule().getSysadminContext();
				connectionDefinition = (IConnectionDefinition<Connection>) getPortalModule().getConnectionDefinition(context, "RefVDoc");
				connection = connectionDefinition.getConnection();
				
				String typeProduction = (String) getWorkflowInstance().getValue("TypeProductionRemplissage");
				if(typeProduction != null && typeProduction.equals("Acétylène"))
				{
					//Acétylène
					getSyntheseAcetylene("SyntheseAcetylene","TotalRampe");
				}
				else if(typeProduction != null && typeProduction.equals("Gaz de l'air"))
				{
					//Oygène
					getSyntheseGazDeLAir("Oxygène","SyntheseOx","TotalJourOx");
					//Oxy Med
					getSyntheseGazDeLAir("Oxygène médical","SyntheseOxMed","TotalJourOxMed");
					//Azote
					getSyntheseGazDeLAir("Azote","SyntheseAzote","TotalJourAzote");
				}
			}
		} catch (Exception e) {
			log.error("Erreur dans la classe CreationSyntheseProduction méthode onBeforeSubmit: "+e.getClass()+"--"+e.getMessage());
		}
		return super.onBeforeSubmit(action);
	}
	
	private void getSyntheseGazDeLAir(String typeDeGaz, String nomTableauDeSynthese,String champTotal) {
		try 
		{
			getWorkflowInstance().setValue(nomTableauDeSynthese, null);
			Collection<? extends ILinkedResource> linkedResources = getWorkflowInstance().getLinkedResources("Oxygene");
			if(linkedResources != null && linkedResources.size() > 0)
			{
				Map<Float, Float> listeSynthese = calculNombreCapaciteGazDeLAir(linkedResources, typeDeGaz);
				if(listeSynthese != null && listeSynthese.size() > 0)
				{
					ILinkedResource nouveauTab = null;
					Float total = 0.0f;
					for(Entry<Float, Float> dataEntry : listeSynthese.entrySet())
					{
						nouveauTab = getWorkflowInstance().createLinkedResource(nomTableauDeSynthese);
							nouveauTab.setValue("Capacite", dataEntry.getKey());
							nouveauTab.setValue("Nombre", dataEntry.getValue());
							nouveauTab.setValue("Volume", dataEntry.getKey() * dataEntry.getValue());
						getWorkflowInstance().addLinkedResource(nouveauTab);
						total = total + dataEntry.getValue();
					}
					getWorkflowInstance().setValue(champTotal, total);
					getWorkflowInstance().save(context);
					
				}
			}
			else
			{
				getWorkflowInstance().setValue(nomTableauDeSynthese, null);
			}
			
			
		} catch (Exception e) {
			log.error("Erreur dans la classe CreationSyntheseProduction méthode getSyntheseGazDeLAir: "+e.getClass()+"-"+e.getMessage());
		}
	}
	private void getSyntheseAcetylene(String nomTableauDeSynthese,String champTotal) {
		try 
		{
			getWorkflowInstance().setValue(nomTableauDeSynthese, null);
			Collection<? extends ILinkedResource> linkedResources = getWorkflowInstance().getLinkedResources("Acetylene");
			if(linkedResources != null && linkedResources.size() > 0)
			{
				Map<Float, Float> listeSynthese = calculNombreCapaciteAcetylene(linkedResources);
				if(listeSynthese != null && listeSynthese.size() > 0)
				{
					ILinkedResource nouveauTab = null;
					Float total = 0.0f;
					for(Entry<Float, Float> dataEntry : listeSynthese.entrySet())
					{
						nouveauTab = getWorkflowInstance().createLinkedResource(nomTableauDeSynthese);
							nouveauTab.setValue("Capacite", dataEntry.getKey());
							nouveauTab.setValue("Nombre", dataEntry.getValue());
							nouveauTab.setValue("Volume", dataEntry.getKey() * dataEntry.getValue());
						getWorkflowInstance().addLinkedResource(nouveauTab);
						total = total + dataEntry.getValue();
					}
					getWorkflowInstance().setValue(champTotal, total);
					getWorkflowInstance().save(context);
				}
			}
			else
			{
				getWorkflowInstance().setValue(nomTableauDeSynthese, null);
				
			}
		} catch (Exception e) {
			log.error("Erreur dans la méthode getSyntheseAcetylene: "+e.getClass()+"--"+e.getMessage());
		}
	}
	private Map<Float, Float> calculNombreCapaciteGazDeLAir(Collection<? extends ILinkedResource> linkedResources, String typeDeGaz)
	{
		Map<Float, Float> nbreCapacite = new HashMap<>();
		for(ILinkedResource linkedResource : linkedResources)
		{
			Float capacite = (Float) linkedResource.getValue("CapaciteBoutOG");
			if(capacite != null)
			{
				String typeGazCourant = (String) linkedResource.getValue("TypeDeGaz_GA");
				if(typeGazCourant != null && typeGazCourant.equals(typeDeGaz))
				{
					if(!nbreCapacite.containsKey(capacite))
					{
						nbreCapacite.put(capacite, 1F);
					}
					else
					{
						for(Entry<Float, Float> dataEntry : nbreCapacite.entrySet())
						{
							if(dataEntry.getKey().equals(capacite))
							{
								dataEntry.setValue(dataEntry.getValue() + 1F);
							}
						}
					}
				}
			}
		}
		return nbreCapacite;
	}
	private Map<Float, Float> calculNombreCapaciteAcetylene(Collection<? extends ILinkedResource> linkedResources)
	{
		Map<Float, Float> nbreCapacite = new HashMap<>();
		for(ILinkedResource linkedResource : linkedResources)
		{
			Float capacite = (Float) linkedResource.getValue("CapaciteEnM3");
			if(capacite != null)
			{
					if(!nbreCapacite.containsKey(capacite))
					{
						nbreCapacite.put(capacite, 1F);
					}
					else
					{
						for(Entry<Float, Float> dataEntry : nbreCapacite.entrySet())
						{
							if(dataEntry.getKey().equals(capacite))
							{
								dataEntry.setValue(dataEntry.getValue() + 1F);
							}
						}
					}
			}
		}
		return nbreCapacite;
	}
	
}
