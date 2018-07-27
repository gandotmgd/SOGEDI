package com.moovapps.sogedi.acquisitionproduction.campagne;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IConnectionDefinition;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.utils.Logger;

public class SyntheseProduction extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(SyntheseProduction.class);
	
	protected IContext context = null;
	protected IConnectionDefinition<java.sql.Connection> connectionDefinition = null;
	protected Connection connection = null;
	protected PreparedStatement statement = null;
	protected ResultSet resultSet = null;
	protected String query = null;
	
	@Override
	public boolean onAfterLoad() {
		try {
			context = getWorkflowModule().getSysadminContext();
			connectionDefinition = (IConnectionDefinition<Connection>) getPortalModule().getConnectionDefinition(context, "RefVDoc");
			connection = connectionDefinition.getConnection();
			
			String typeProduction = (String) getWorkflowInstance().getValue("TypeProductionRemplissage");
			String CumulEnregistre = (String) getWorkflowInstance().getValue("CumulEnregistre");
			if(typeProduction != null && typeProduction.equals("Acétylène"))
			{
				//Acétylène
				Float i = (Float) getWorkflowInstance().getValue("TotalRampe");
				Float cumulMois = getCumulNombre("CumulMoisRampe");
				if(cumulMois != null && i != null)
				{
					if(CumulEnregistre.equals("Oui"))
					{
						getWorkflowInstance().setValue("CumulMoisRampe", cumulMois);
					}
					else
					{
						getWorkflowInstance().setValue("CumulMoisRampe", cumulMois + i);
					}
					
				}
			}
			else if(typeProduction != null && typeProduction.equals("Gaz de l'air"))
			{
				//Oygène
				Collection<? extends ILinkedResource> ox = getWorkflowInstance().getLinkedResources("SyntheseOx");
				if(ox != null && ox.size() > 0)
				{
					Float i = (Float) getWorkflowInstance().getValue("TotalJourOx");
					
					Float cumulMois = getCumulNombre("CumulMoisOx");
					if(cumulMois != null && i != null)
					{
						if(CumulEnregistre.equals("Oui"))
						{
							getWorkflowInstance().setValue("CumulMoisOx", cumulMois);
						}
						else
						{
							getWorkflowInstance().setValue("CumulMoisOx", cumulMois + i);
						}
					}
				}
				//Oxy Med
				Collection<? extends ILinkedResource> oxMed = getWorkflowInstance().getLinkedResources("SyntheseOxMed");
				if(oxMed != null && oxMed.size() > 0)
				{
					Float i = (Float) getWorkflowInstance().getValue("TotalJourOxMed");
					Float cumulMois = getCumulNombre("CumulMoisOxMed");
					if(cumulMois != null && i != null)
					{
						if(CumulEnregistre.equals("Oui"))
						{
							getWorkflowInstance().setValue("CumulMoisOxMed", cumulMois);
						}
						else
						{
							getWorkflowInstance().setValue("CumulMoisOxMed", cumulMois + i);
						}
						
					}
				}
				//Azote
				Collection<? extends ILinkedResource> azote = getWorkflowInstance().getLinkedResources("SyntheseAzote");
				if(azote != null && azote.size() > 0)
				{
					Float i = (Float) getWorkflowInstance().getValue("TotalJourAzote");
					Float cumulMois = getCumulNombre("CumulMoisAzote");
					if(cumulMois != null && i != null)
					{
						if(CumulEnregistre.equals("Oui"))
						{
							getWorkflowInstance().setValue("CumulMoisAzote", cumulMois);
						}
						else
						{
							getWorkflowInstance().setValue("CumulMoisAzote", cumulMois + i);
						}
						
					}
				}
			}
			
			
		} catch (Exception e) {
			log.error("Erreur dans la classe SyntheseProduction méthode onAfterLoad: "+e.getClass()+"-"+e.getMessage());
		}
		finally
		{
			if (resultSet != null)
			{
				try
				{
					resultSet.close();
				}
				catch (SQLException e)
				{
				}
			}
			if (statement != null)
			{
				try
				{
					statement.close();
				}
				catch (SQLException e)
				{
				}
			}
			if (connection != null)
			{
				try
				{
					connection.close();
				}
				catch (SQLException e)
				{
				}
			}
		}
		return super.onAfterLoad();
	}
	
	@Override
	public boolean onBeforeSave() {
		try {
			context = getWorkflowModule().getSysadminContext();
			getWorkflowInstance().setValue("CumulEnregistre", "Oui");
		} catch (Exception e) {
			log.error("Erreur dans la classe SyntheseProduction méthode onBeforeSave: "+e.getClass()+"-"+e.getMessage());
		}
		return super.onBeforeSave();
	}
	
	private  Float getCumulNombre(String champTotalProd)
	{
		try
		{
			String query = "SELECT max("+champTotalProd+")"
					+ "FROM r_woracquisitio WHERE YEAR(DateIniProd)=YEAR(SYSDATE()) AND MONTH(DateIniProd)=MONTH(SYSDATE())";
					
			
			statement = connection.prepareStatement(query);
			resultSet = statement.executeQuery();
			if (resultSet.next())
			{
				return resultSet.getFloat(1);
			}
			return null;	
		}
		catch (Exception e)
		{
			String message = e.getMessage();
			if (message == null)
			{
				message = "";
			}
			log.error("Erreur dans la classe SyntheseProduction méthode getCumulNombre: " + e.getClass() + " - " + message);
		}
		return null;
	}
}
