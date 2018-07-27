/**
 * 
 */
package com.moovapps.sogedi.acquisitionproduction.productionRampe.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IGroup;
import com.axemble.vdoc.sdk.interfaces.IOptionList.IOption;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
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
	protected IResourceDefinition resourceDefinitionSuiviBouteille = null;
		
	@Override
	public boolean onAfterLoad() {
		try {
			context = getWorkflowModule().getSysadminContext();
			organization = getDirectoryModule().getOrganization(context, getWorkflowModule().getConfiguration().getStringProperty("com.moovapps.sogedi.organization.name"));
			project = getWorkflowInstance().getCatalog().getProject();
			catalogRef = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
			resourceDefinitionSuiviBouteille = getWorkflowModule().getResourceDefinition(context, catalogRef, "SuiviBouteille");
			
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
				//optionsOk = new HashMap<>();
				if(typeBout != null)
				{
					ArrayList<IOption> options = Helper.getBouteillesAProduire(context, getWorkflowModule(), getWorkflowInstance(), resourceDefinitionSuiviBouteille, typeBout, "AGENCE", "Vide traitée");
					if(options != null && options.size() > 0)
					{
						getWorkflowInstance().setList("NBouteillesProduites", options);
					}
					else
					{
						getWorkflowInstance().setList("NBouteillesProduites", null);
						getResourceController().inform("NBouteillesProduites", "Aucune bouteille de type "+typeBout+" n'est prête pour une nouvelle production.");
						
					}
				}
				else
				{
					getWorkflowInstance().setList("NBouteillesProduites", null);
				}
				getWorkflowInstance().save(context);
			}
		} catch (Exception e) {
			log.error("Erreur dans la classe com.moovapps.sogedi.acquisitionproduction.productionRampe.document.DeclarationRampe méthode onPropertyChanged "+e.getClass()+" - "+e.getMessage());
		}
		super.onPropertyChanged(property);
	}


}
