package com.moovapps.sogedi.acquisitionproduction.campagne;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.ui.framework.widgets.CtlButton;

public class Initiation extends BaseDocumentExtension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(Initiation.class);
	
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
					botton.setHidden(false);
				}
			}
			else
			{
				botton.setHidden(true);
			}
		} catch (Exception e) {
			log.error("Erreur dans la com.moovapps.sogedi.acquisitionproduction.campagne.Initiation methode onAfterLoad : " + e.getClass() + " - " + e.getMessage());
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
						botton.setHidden(false);
					}
				}
				else
				{
					botton.setHidden(true);
				}
			}
		} catch (Exception e) {
			log.error("Erreur dans la com.moovapps.sogedi.acquisitionproduction.campagne.Initiation methode onPropertyChanged : " + e.getClass() + " - " + e.getMessage());
		}
		super.onPropertyChanged(property);
	}
}
