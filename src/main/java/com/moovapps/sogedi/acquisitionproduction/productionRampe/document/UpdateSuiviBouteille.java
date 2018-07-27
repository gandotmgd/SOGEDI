package com.moovapps.sogedi.acquisitionproduction.productionRampe.document;

import java.util.Collection;
import java.util.Date;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.utils.Logger;

public class UpdateSuiviBouteille extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(DeclarationRampe.class);
	
	protected IContext context = null;
	protected IOrganization organization = null;
	protected IProject project = null;
	protected ICatalog catalogRef = null;
	protected IResourceDefinition resourceDefinitionSuiviBouteille = null;
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		try {
			if(action.getName().equals("AccuserReception"))
			{
				context = getWorkflowModule().getSysadminContext();
				project = getWorkflowInstance().getCatalog().getProject();
				catalogRef = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
				
				Collection<String> bouteilles = (Collection<String>) getWorkflowInstance().getValue("NBouteillesProduites");
				if(bouteilles != null && bouteilles.size()> 0)
				{
					resourceDefinitionSuiviBouteille = getWorkflowModule().getResourceDefinition(context, catalogRef, "SuiviBouteille");
					
					for(String bouteille : bouteilles)
					{
						IStorageResource suivi = getWorkflowModule().createStorageResource(context, resourceDefinitionSuiviBouteille, null);
						if(suivi != null)
						{
							//Date suivi
							suivi.setValue("DateDeSuivi", new Date());
							//Numero bouteille
							suivi.setValue("CodeEmballage", bouteille);
							//Type de bouteille
							String typeBout = (String) getWorkflowInstance().getValue("TypeDeBouteilleSurLaRampe");
							if(typeBout != null)
							{
								suivi.setValue("TypeGazSuivi", typeBout);
							}
							//Etat de la bouteille
							suivi.setValue("EtatBoutSuivi", "Remplie");
							//Depot
							suivi.setValue("Depot", "AGENCE");
							//Destination bouteille
							suivi.setValue("CodeTiersMouv", "AGENCE");
							//Entree/Sortie
							suivi.setValue("EntreeSortie", "E");
							//Date mouvement
							suivi.setValue("DateMouv", new Date());
							
							suivi.save(context);
							
						}
					}
				}
			}
			
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.acquisitionproduction.productionRampe.document.UpdateSuiviBouteille methode onBeforeSubmit : " + e.getClass() + " - " + e.getMessage());
		}
		return super.onBeforeSubmit(action);
	}

}
