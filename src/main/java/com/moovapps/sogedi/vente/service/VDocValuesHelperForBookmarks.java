/**
 * 
 */
package com.moovapps.sogedi.vente.service;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.imageio.ImageIO;

import com.aspose.words.Bookmark;
import com.aspose.words.BreakType;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.StyleIdentifier;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IGroup;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.ILocalization;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.structs.Period;

/**
 * @author flucas
 *
 */
public class VDocValuesHelperForBookmarks
{
	/** the default class logger */
	@SuppressWarnings("unused")
	private static final com.axemble.vdoc.sdk.utils.Logger LOG = com.axemble.vdoc.sdk.utils.Logger.getLogger(VDocValuesHelperForBookmarks.class);
	@SuppressWarnings("unused")
	private static final String SYS_TITLE = "sys_Title";

	private VDocValuesHelperForBookmarks ()
	{}

	/**
	 * Gestion des types de champs support�s <br>
	 * "m�thode r�cursive"
	 * 
	 * @param resourceValue
	 * @param bookmark
	 * @param builder
	 * @throws Exception
	 */
	public static void setType(Object resourceValue, Bookmark bookmark, DocumentBuilder builder, IWorkflowModule workflowModule) throws Exception
	{
		if(resourceValue == null )
		{
			bookmark.setText("");
		}
		else
		{
			if (resourceValue instanceof Float || resourceValue instanceof Double)
			{
				VDocValuesHelperForBookmarks.setNumberType(resourceValue, bookmark, workflowModule);
			}
			else if (resourceValue instanceof Date)
			{
				VDocValuesHelperForBookmarks.setDateType((Date)resourceValue, bookmark, workflowModule);
			}
			else if (resourceValue instanceof Period)
			{
				VDocValuesHelperForBookmarks.setPeriodType((Period)resourceValue, bookmark, workflowModule);
			}
			else if (resourceValue instanceof IUser || resourceValue instanceof IOrganization || resourceValue instanceof ILocalization || resourceValue instanceof IGroup)
			{
				VDocValuesHelperForBookmarks.setDirectoryType(resourceValue, bookmark);
			}
			else if (resourceValue instanceof IStorageResource)
			{
				VDocValuesHelperForBookmarks.setStorageResourceType((IStorageResource)resourceValue, bookmark);
			}
			else if (resourceValue instanceof ILinkedResource)
			{
				VDocValuesHelperForBookmarks.setLinkedResourceType((ILinkedResource)resourceValue, bookmark);
			}			
			else if (resourceValue instanceof IAttachment)
			{
				VDocValuesHelperForBookmarks.setAttachmentType((IAttachment)resourceValue, bookmark, builder);
			}
			else if (resourceValue instanceof Collection)
			{
				//S�parateur
				String separator = workflowModule.getConfiguration().getStringProperty("com.vdoc.connector.aspose.collection.separator");

				StringBuilder textBuilder = new StringBuilder();

				@SuppressWarnings("unchecked")
				Collection<Object> values = (Collection<Object>)resourceValue;

				boolean isText = true;

				for (Object value : values)
				{
					setType(value, bookmark, builder, workflowModule);

					if (value instanceof IAttachment)
					{
						isText = false;

						//S�parer les liens/images par un saut de ligne
						builder.insertBreak(BreakType.LINE_BREAK);
					}
					else
					{
						textBuilder.append(bookmark.getText());
						textBuilder.append(separator);
						textBuilder.append(" ");
					}
				}

				if (values.size() > 0 && isText)
				{
					//Supprimer la derni�re position du s�parateur
					bookmark.setText(textBuilder.substring(0, textBuilder.lastIndexOf(separator)));
				}
			}
			else
			//Types textes et autres
			{
				VDocValuesHelperForBookmarks.setStringType(resourceValue, bookmark);
			}
		}
	}

	/**
	 * @param propertyName
	 * @param value
	 * @param bookmark
	 * @throws Exception
	 */
	public static void setStringType(Object value, Bookmark bookmark) throws Exception
	{
		bookmark.setText(value.toString());
	}

	/**
	 * @param propertyName
	 * @param numberValue
	 * @param bookmark
	 * @throws Exception
	 */
	public static void setNumberType(Object numberValue, Bookmark bookmark, IWorkflowModule workflowModule) throws Exception
	{
		String format = workflowModule.getConfiguration().getStringProperty("com.vdoc.connector.aspose.number.format");

		DecimalFormat decimalFormat = new DecimalFormat(format);

		bookmark.setText(decimalFormat.format(numberValue));
	}

	/**
	 * @param propertyName
	 * @param date
	 * @param bookmark
	 * @throws Exception
	 */
	public static void setDateType(Date date, Bookmark bookmark, IWorkflowModule workflowModule) throws Exception
	{
		String format = workflowModule.getConfiguration().getStringProperty("com.vdoc.connector.aspose.date.format");

		SimpleDateFormat dateFormatter = new SimpleDateFormat(format);

		bookmark.setText(dateFormatter.format(date));
	}

	/**
	 * @param propertyName
	 * @param period
	 * @param bookmark
	 * @throws Exception
	 */
	public static void setPeriodType(Period period, Bookmark bookmark, IWorkflowModule workflowModule) throws Exception
	{
		Date startDate = period.getStartDate();
		Date endDate = period.getEndDate();

		if (startDate != null && endDate != null)
		{
			String format = workflowModule.getConfiguration().getStringProperty("com.vdoc.connector.aspose.period.format");
			SimpleDateFormat dateFormatter = new SimpleDateFormat(format);

			String formattedStartDate = dateFormatter.format(startDate);
			String formattedEndDate = dateFormatter.format(endDate);
			bookmark.setText("Du " + formattedStartDate + " Au " + formattedEndDate );
		}
	}

	/**
	 * @param propertyName
	 * @param directoryValue
	 * @param bookmark
	 * @throws Exception
	 */
	public static void setDirectoryType(Object directoryValue, Bookmark bookmark) throws Exception
	{
		if (directoryValue instanceof IUser)
		{
			bookmark.setText(((IUser)directoryValue).getFullName());
		}
		else if (directoryValue instanceof IOrganization)
		{
			bookmark.setText(((IOrganization)directoryValue).getLabel());
		}
		else if (directoryValue instanceof ILocalization)
		{
			bookmark.setText(((ILocalization)directoryValue).getLabel());
		}
		else if (directoryValue instanceof IGroup)
		{
			bookmark.setText(((IGroup)directoryValue).getLabel());
		}
	}
	
	/**
	 * @param propertyName
	 * @param storageResource
	 * @param bookmark
	 * @throws Exception
	 */
	public static void setStorageResourceType(IStorageResource storageResource, Bookmark bookmark) throws Exception
	{
		bookmark.setText((String) storageResource.getValue(IProperty.System.TITLE));
	}

	/**
	 * @param propertyName
	 * @param linkedResource
	 * @param bookmark
	 * @throws Exception
	 */
	public static void setLinkedResourceType(ILinkedResource linkedResource, Bookmark bookmark) throws Exception
	{
		bookmark.setText(linkedResource.getDefinition().getName());
	}

	/**
	 * @param propertyName
	 * @param attachment
	 * @param bookmark
	 * @param builder
	 * @throws Exception
	 */
	public static void setAttachmentType(IAttachment attachment, Bookmark bookmark, DocumentBuilder builder) throws Exception
	{
		InputStream inputStream = attachment.getInputStream();

		if (inputStream != null)
		{
			//Si la pi�ce jointe est de type image
			BufferedImage img = null;

			img = ImageIO.read(inputStream);

			if (img != null)
			{
				builder.moveToBookmark(bookmark.getName());
				builder.insertImage(img);
			}
			else
			{
				//Sinon
				String baseUrl = Navigator.getNavigator().getExecutionContext().getRequest().getBaseUrl();

				builder.moveToBookmark(bookmark.getName());
				int previousStyleIdentifier = builder.getFont().getStyleIdentifier();
				builder.getFont().setStyleIdentifier(StyleIdentifier.HYPERLINK);
				builder.insertHyperlink(attachment.getName(), baseUrl + attachment.getURI(), false);
				builder.getFont().setStyleIdentifier(previousStyleIdentifier);
			}

			inputStream.close();
		}
	}
}
