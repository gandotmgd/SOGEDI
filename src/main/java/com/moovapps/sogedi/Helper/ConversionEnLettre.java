package com.moovapps.sogedi.Helper;

import java.util.Locale;

import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;

public class ConversionEnLettre 
{
	public static String convertirEnLettre(String nbre)
	{
	    NumberFormat formatter = new RuleBasedNumberFormat(Locale.FRANCE, RuleBasedNumberFormat.SPELLOUT);
		String result = formatter.format(Double.parseDouble(nbre));
		return result;
	}
	

}
