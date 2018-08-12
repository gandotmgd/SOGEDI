package com.moovapps.sogedi.acquisitionproduction.campagne.document;

import java.util.Collection;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.ui.framework.widgets.CtlButton;

public class Production extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(Production.class);
	
	@Override
	public boolean onAfterLoad() {
		try {
			String finCampagne = (String) getWorkflowInstance().getValue("FinDeLaCampagne");
			CtlButton botton = getResourceController().getBottomButton("Fin campagne");
			if(finCampagne != null)
			{

				if(finCampagne.equals("Non"))
				{
					botton.setHidden(true);
					
				}
				else
				{
					int statut = getStatusAllRampe();
					if(statut == 1)
					{
						botton.setHidden(false);
					}
					else
					{
						getResourceController().inform("FinDeLaCampagne","Merci de patienter jusqu'à la clôture de toutes les rampes associées pour finir la campagne");
						botton.setHidden(true);
					}
				}
			}
			else
			{
				botton.setHidden(true);
			}
		} catch (Exception e) {
			log.error("Erreur dans la com.moovapps.sogedi.acquisitionproduction.campagne.document.Production methode onAfterLoad : " + e.getClass() + " - " + e.getMessage());
		}
		return super.onAfterLoad();
	}
	
	@Override
	public void onPropertyChanged(IProperty property) {
		try {
			if(property.getName().equals("FinDeLaCampagne"))
			{
				String finCampagne = (String) getWorkflowInstance().getValue("FinDeLaCampagne");
				CtlButton botton = getResourceController().getBottomButton("Fin campagne");
				if(finCampagne != null)
				{

					if(finCampagne.equals("Non"))
					{
						botton.setHidden(true);
						
					}
					else
					{
						int statut = getStatusAllRampe();
						if(statut == 1)
						{
							botton.setHidden(false);
						}
						else
						{
							getResourceController().alert("Merci de patienter jusqu'à la clôture de toutes les rampes associées pour finir la campagne");
							botton.setHidden(true);
						}
					}
				}
				else
				{
					botton.setHidden(true);
				}
			}
		} catch (Exception e) {
			log.error("Erreur dans la com.moovapps.sogedi.acquisitionproduction.campagne.document.Production methode onPropertyChanged : " + e.getClass() + " - " + e.getMessage());
		}
		super.onPropertyChanged(property);
	}
	
	private int getStatusAllRampe()
	{
		Collection<? extends IWorkflowInstance> rampes = getWorkflowInstance().getLinkedWorkflowInstances("Rampes");
		if(rampes != null && rampes.size() > 0)
		{
			String etatDocument = null;
			for(IWorkflowInstance rampe : rampes)
			{
				etatDocument = (String) rampe.getValue("DocumentState");
				if(etatDocument != null && !etatDocument.equals("Archivé"))
				{
					return 0;
				}
			}
		}
		return 1;
	}
}
