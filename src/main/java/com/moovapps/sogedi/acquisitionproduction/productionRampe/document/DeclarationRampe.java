/**
 * 
 */
package com.moovapps.sogedi.acquisitionproduction.productionRampe.document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IGroup;
import com.axemble.vdoc.sdk.interfaces.IOptionList.IOption;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.utils.Logger;
import com.moovapps.sogedi.Helper.Helper;

/**
 * @author Portable
 *
 */
public class DeclarationRampe extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(DeclarationRampe.class);
	
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
			project = getWorkflowInstance().getCatalog().getProject();
			catalogRef = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
			refBout = getWorkflowModule().getResourceDefinition(context, catalogRef, "Bouteilles");
			
			//Jour de production de la rampe
//			Calendar calendar = Calendar.getInstance();
//			calendar.setTime(new Date());
//			getWorkflowInstance().setValue("JourDeProduction", String.valueOf(calendar.get(Calendar.DAY_OF_WEEK)));
			//Equipe de production
			IGroup groupeProduction = getDirectoryModule().getGroup(context, organization, "EquipeDeProduction");
			if(groupeProduction != null)
			{
				Collection<? extends IUser> users = groupeProduction.getAllMembers();
				if(users != null && users.size() > 0)
				{
					Collection<IOption> options = new ArrayList<IOption>();
					for(IUser user : users)
					{
						options.add(getWorkflowModule().createListOption(user.getFullName(), user.getFullName()));
					}
					getWorkflowInstance().setList("EquipeDeProduction", options);
				}
			}
			//Bouteilles à vendre
			String typeBout = (String) getWorkflowInstance().getValue("TypeDeBouteilleSurLaRampe");

			if(typeBout != null)
			{
				ArrayList<IOption> options = Helper.getBouteilles(context, getWorkflowModule(), getWorkflowInstance(), refBout, typeBout,"USINE","Vide traitée",true);
				if(options != null && options.size() > 0)
				{
					getWorkflowInstance().setList("NBouteillesProduites", options);
				}
				else
				{
					getWorkflowInstance().setValue("NBouteillesProduites", null);
					getWorkflowInstance().setList("NBouteillesProduites", null);
					getResourceController().inform("NBouteillesProduites", "Aucune bouteille de type "+typeBout+" n'est prête pour une nouvelle production.");
					
				}
			}
			else
			{
				getWorkflowInstance().setValue("NBouteillesProduites", null);
				getWorkflowInstance().setList("NBouteillesProduites", null);
			}
			//Bouteilles en rebus
			Collection<String> bouteillesSurRampe = (Collection<String>) getWorkflowInstance().getValue("NBouteillesProduites");
			if(bouteillesSurRampe != null && bouteillesSurRampe.size() > 0)
			{
				ArrayList<IOption> liste = new ArrayList<IOption>();
				for(String bouteille : bouteillesSurRampe)
				{
					liste.add(getWorkflowModule().createListOption(bouteille, bouteille));
				}
				getWorkflowInstance().setList("NDesBouteillesEnRebus", liste);
			}
			else
			{
				getWorkflowInstance().setValue("NDesBouteillesEnRebus", null);
				getWorkflowInstance().setList("NDesBouteillesEnRebus", null);
			}
			
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.acquisitionproduction.productionRampe.document.DeclarationRampe méthode onAfterLoad "+e.getClass()+" - "+e.getMessage());
		}
		return super.onAfterLoad();
	}
	
	@Override
	public void onPropertyChanged(IProperty property) {
		try {
			if(property.getName().equals("TypeDeBouteilleSurLaRampe"))
			{
				String typeBout = (String) getWorkflowInstance().getValue("TypeDeBouteilleSurLaRampe");

				if(typeBout != null)
				{
					//ArrayList<IOption> options = Helper.getBouteillesAProduire(context, getWorkflowModule(), getWorkflowInstance(), resourceDefinitionSuiviBouteille, typeBout, "AGENCE", "Vide traitée");
					ArrayList<IOption> options = Helper.getBouteilles(context, getWorkflowModule(), getWorkflowInstance(), refBout, typeBout,"USINE","Vide traitée",true);
					if(options != null && options.size() > 0)
					{
						getWorkflowInstance().setList("NBouteillesProduites", options);
					}
					else
					{
						getWorkflowInstance().setValue("NBouteillesProduites", null);
						getWorkflowInstance().setList("NBouteillesProduites", null);
						getResourceController().inform("NBouteillesProduites", "Aucune bouteille de type "+typeBout+" n'est prête pour une nouvelle production.");
						
					}
				}
				else
				{
					getWorkflowInstance().setValue("NBouteillesProduites", null);
					getWorkflowInstance().setList("NBouteillesProduites", null);
				}
			}
			if(property.getName().equals("NBouteillesProduites"))
			{
				Collection<String> bouteillesSurRampe = (Collection<String>) getWorkflowInstance().getValue("NBouteillesProduites");
				if(bouteillesSurRampe != null && bouteillesSurRampe.size() > 0)
				{
					ArrayList<IOption> liste = new ArrayList<IOption>();
					for(String bouteille : bouteillesSurRampe)
					{
						liste.add(getWorkflowModule().createListOption(bouteille, bouteille));
					}
					getWorkflowInstance().setList("NDesBouteillesEnRebus", liste);
				}
				else
				{
					getWorkflowInstance().setValue("NDesBouteillesEnRebus", null);
					getWorkflowInstance().setList("NDesBouteillesEnRebus", null);
				}
			}
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.acquisitionproduction.productionRampe.document.DeclarationRampe méthode onPropertyChanged "+e.getClass()+" - "+e.getMessage());
		}
		super.onPropertyChanged(property);
	}


}
