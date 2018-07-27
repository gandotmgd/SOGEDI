package com.moovapps.sogedi.vente.service;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

import com.axemble.vdoc.sdk.structs.Period;


public class DateService
{
	/** the default class logger */
	@SuppressWarnings("unused")
	private static com.axemble.vdoc.sdk.utils.Logger LOG = com.axemble.vdoc.sdk.utils.Logger.getLogger(DateService.class);

	private static final long MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;

	public enum Mois
	{
		_1("1", "Janvier"), _2("2", "Février"), _3("3", "Mars"), _4("4", "Avril"), _5("5", "Mai"), _6("6", "Juin"), _7("7", "Juillet"), _8("8", "Août"), _9("9", "Septembre"), _10("10", "Octobre"), _11("11", "Novembre"), _12("12", "Decembre");
		protected String key;
		protected String label;

		Mois (String key, String label)
		{
			this.key = key;
			this.label = label;
		}
		public String getKey()
		{
			return this.key;
		}
		public String getLabel()
		{
			return this.label;
		}
	}

	/**
	 * Vérifie si c'est un Lundi
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isMonday(Date date)
	{
		Calendar cal = Calendar.getInstance();
		// Set date
		cal.setTime(date);

		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

		if (dayOfWeek == Calendar.MONDAY)
		{
			return true;
		}

		return false;
	}

	/**
	 * Vérifie si c'est le premier jour du mois
	 * 
	 * @param date
	 */
	public static boolean isFirstDayOfMonth(Date date)
	{
		int dayOfMonth = getDayOfMonth(date);

		if (dayOfMonth == 1)
		{
			return true;
		}

		return false;
	}

	/**
	 * Retourne un jour de la semaine
	 * 
	 * @param day
	 * @param week
	 * @param mount
	 * @param year
	 * @return
	 */
	public static Date getDayOfWeek(int day, int week, int mount, int year)
	{
		Calendar cal = Calendar.getInstance();
		// Set date
		cal.set(Calendar.DAY_OF_WEEK, day);
		cal.set(Calendar.WEEK_OF_MONTH, week);
		cal.set(Calendar.MONTH, mount);
		cal.set(Calendar.YEAR, year);

		// Clear time
		clearTime(cal);

		return cal.getTime();
	}

	/**
	 * Initialise le calendar
	 * 
	 * @param cal
	 */
	private static void clearTime(Calendar cal)
	{
		// clear time
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
	}

	/**
	 * Initialise le date à 0h 00m 00millis
	 * 
	 * @param cal
	 */
	public static Date clearDate(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		// clear time
		clearTime(cal);

		return cal.getTime();
	}

	/**
	 * Initialise le période
	 * 
	 * @see com.vdoc.visiativ.timesheet.services.DateService.clearDate(Date)
	 * @param cal
	 */
	public static Period clearPeriode(Period period)
	{
		return new Period(clearDate(period.getStartDate()), clearDate(period.getEndDate()));
	}


	/**
	 * Retourne le nombre de jours entre deux dates
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
//	public static Float getDaysBetweenDates(Date startDate, Date endDate)
//	{
//		if (startDate != null && endDate != null)
//		{
//			return (float)((endDate.getTime() - startDate.getTime()) / MILLISECONDS_PER_DAY);
//		}
//
//		return 0.0F;
//	}
	
	/*****
	 * Retourner l'âge correspondant à une date de naissance
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static Float getAge(Date startDate, Date endDate)
	{
		if (startDate != null && endDate != null)
		{
			Calendar start = Calendar.getInstance();
			start.setTime(startDate);
			
			Calendar end = Calendar.getInstance();
			end.setTime(new Date());
			return (float)((end.get(Calendar.YEAR) - start.get(Calendar.YEAR)));
		}

		return 0.0F;
	}

	/**
	 * Ajouter des jours à une date
	 * 
	 * @param date
	 * @param days
	 * @return
	 */
	public static Date addDays(Date date, int days)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		cal.add(Calendar.DATE, days);

		return cal.getTime();
	}

	/**
	 * Ajouter des semaines à une date
	 * 
	 * @param date
	 * @param weeks
	 * @return
	 */
	public static Date addWeeks(Date date, int weeks)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		cal.add(Calendar.WEEK_OF_MONTH, weeks);

		return cal.getTime();
	}

	/**
	 * Retourne la semaine
	 * 
	 * @param date
	 * @return
	 */
	public static int getWeek(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		return cal.get(Calendar.WEEK_OF_MONTH);
	}

	/**
	 * Retourne le mois du Calendar java
	 * 
	 * @param date
	 * @return
	 */
	public static int getMonth(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		return cal.get(Calendar.MONTH);
	}

	/**
	 * Retourne l'année
	 * 
	 * @param date
	 * @return
	 */
	public static int getYear(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		return cal.get(Calendar.YEAR);
	}

	/**
	 * Retourne le nombre de semaines dans le mois
	 * 
	 * @param month
	 * @param year
	 * @return
	 */
	public static int getNumberOfWeeks(int month, int year)
	{
		Calendar cal = Calendar.getInstance();
		Date date = getDate(1, month, year);
		cal.setTime(date);

		int numOfWeeksInMonth = 1;
		while (cal.get(Calendar.MONTH) == month)
		{
			cal.add(Calendar.DAY_OF_MONTH, 1);
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
			{
				numOfWeeksInMonth++;
			}
		}

		return numOfWeeksInMonth;
	}

	/**
	 * Retourne le nombre de jours dans le mois
	 * 
	 * @param month
	 * @param year
	 * @return
	 */
	public static int getMaxDaysInMonth(int month, int year)
	{
		Calendar cal = Calendar.getInstance();
		Date date = getDate(1, month, year);
		cal.setTime(date);

		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Affiche le mois sur deux digits
	 * 
	 * @param month Calendar.Month
	 * @return
	 */
	public static String getFormatedMonth(int month)
	{
		NumberFormat nf = new DecimalFormat("00");

		return nf.format(month + 1);
	}

	/**
	 * Retourne la date du premier jour du mois
	 * 
	 * @param date
	 * @return
	 */
	public static Date getFirstDayInMonth(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 1);

		clearTime(cal);

		return cal.getTime();
	}

	/**
	 * Tester si une date entre dans une période
	 * 
	 * @param date
	 * @param period
	 * @return
	 */
	public static boolean isInPeriod(Date date, Period period)
	{
		long dateTime = date.getTime();
		long startDateTime = period.getStartDate().getTime();
		long endDateTime = period.getEndDate().getTime();

		if (startDateTime <= dateTime && endDateTime >= dateTime)
		{
			return true;
		}

		return false;
	}

	/**
	 * Retourne le jour du mois d'une date
	 * 
	 * @param date
	 * @return
	 */
	public static int getDayOfMonth(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		return cal.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Retourne l'index du jour de la semaine
	 * 
	 * @param date
	 * @return
	 */
	public static int getDayOfWeek(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		return cal.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * Retourne le dernier jour du mois
	 * 
	 * @param date
	 * @return
	 */
	public static Date getLastDayInMonth(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		clearTime(cal);

		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

		return cal.getTime();
	}

	

	/**
	 * Tester si le jour n'est pas un WE
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isNotWEDay(Date date)
	{
		Calendar cal = Calendar.getInstance();
		// Set date
		cal.setTime(date);

		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

		if (dayOfWeek % 7 < 2)
		{
			return false;
		}

		return true;
	}

	/**
	 * Get date
	 * 
	 * @param dayOfMonth
	 * @param month
	 * @param year
	 * @return
	 */
	public static Date getDate(int dayOfMonth, int month, int year)
	{
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.YEAR, year);

		clearTime(cal);

		return cal.getTime();
	}

	/**
	 * Vérifier si l'on est dans la dernière semaine du mois
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isLastWeekOfMonth(Date date)
	{
		Calendar cal = Calendar.getInstance();
		// Set date
		cal.setTime(date);

		int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);

		int maxWeekOfMonth = cal.getActualMaximum(Calendar.WEEK_OF_MONTH);

		if (weekOfMonth == maxWeekOfMonth)
		{
			return true;
		}

		return false;
	}
}
