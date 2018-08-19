package ru.ivi.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateUtils extends android.text.format.DateUtils {
	
	public static final int DAY_IN_SECONDS = (int) (DAY_IN_MILLIS / SECOND_IN_MILLIS);
	public static final int HOUR_IN_SECONDS = (int) (HOUR_IN_MILLIS / SECOND_IN_MILLIS);
	public static final int MINUTE_IN_SECONDS = 60;
	public static final int DAY_IN_IVI_MONTH = 30;
	public static final int DAY_IN_HOURS = 24;
	public static final int DAYS_IN_WEEK = 7;
	public static final Locale RU_LOCALE = new Locale("ru");
	
	private static final String SHORT_IVI_DATE_PATTERN = "yyyy-MM-dd";
	private static final String SHORT_IVI_TIME_PATTERN = "HH:mm:ss";
	private static final String IVI_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
	private static final String FINISH_TIME_DATE_PATTERN = "HH:mm dd.MM";
	private static final String SHORT_FINISH_TIME_DATE_PATTERN = "dd.MM";
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(IVI_DATE_PATTERN, Locale.US);
	private static final String ISO8601_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
	private static final String ISO8601_UTC_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String CLIENT_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";
	private static final String SHORT_DATE_PATTERN = "dd.MM.yyyy";
	private static final String LONG_DATE_PATTERN = "dd MMMM yyyy";
	private static final String DATE_PATTERN_TIMEZONE = "ZZZZZ";
	private static final String CARD_DATE_PATTERN = "MM/yy";
	private static final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm";
	private static final String DATE_LOCAL_PATTERN = "dd MMMM";
	private static final String HOURS_MINUTES_PATTERN = "HH:mm";
	private static final String LOGCAT_DATE_PATTERN = "MM-dd HH:mm:ss.SSS";
	private static final String LONG_DATE_PATTERN_WITHOUT_ZEROS = "d MMMM yyyy";
	private static final String DATE_LOCAL_PATTERN_WITHOUT_ZEROS = "d MMMM";
	
	
	
	public static String getTimezone() {
		return new SimpleDateFormat(DATE_PATTERN_TIMEZONE, RU_LOCALE).format(new Date());
	}
	
	public static DateFormat getShortIviDateFormat() {
		return new SimpleDateFormat(SHORT_IVI_DATE_PATTERN, RU_LOCALE);
	}
	
	public static DateFormat getShortIviTimeFormat() {
		return new SimpleDateFormat(SHORT_IVI_TIME_PATTERN, Locale.US);
	}
	
	private static DateFormat getFinishTimeDateFormat() {
		return new SimpleDateFormat(FINISH_TIME_DATE_PATTERN, RU_LOCALE);
	}
	
	private static DateFormat getShortFinishTimeDateFormat() {
		return new SimpleDateFormat(SHORT_FINISH_TIME_DATE_PATTERN, RU_LOCALE);
	}
	
	private static DateFormat getLogcatDateFormat() {
		return new SimpleDateFormat(LOGCAT_DATE_PATTERN, Locale.US);
	}
	
	public static Date parseDate(final String dateString, final DateFormat dateFormat) throws ParseException {
		if (dateFormat == null) {
			throw new IllegalArgumentException("dateFormat must not be null!");
		}
		
		if (!TextUtils.isEmpty(dateString)) {
			return dateFormat.parse(dateString);
		} else {
			return null;
		}
	}
	
	public static Date parseIviDate(final String dateString) {
		if (!TextUtils.isEmpty(dateString)) {
			try {
				return parseDate(
					dateString.substring(0, Math.min(dateString.length(), IVI_DATE_PATTERN.length())),
					getIviDateFormat());
			} catch (final ParseException e) {
//				L.e(e);
			}
		}
		
		return null;
	}
	
	public static Date parseIso8601Date(final String dateString) {
		if (!TextUtils.isEmpty(dateString)) {
			try {
				return parseDate(dateString, dateString.contains("Z") ? getIso8601UTCDateFormat() : getIso8601DateFormat());
			} catch (final ParseException e) {
//				L.e(e);
			}
		}
		
		return null;
	}
	
	public static Date parseShortIviDate(final String dateString) {
		if (!TextUtils.isEmpty(dateString)) {
			try {
				return parseDate(dateString, getShortIviDateFormat());
			} catch (final ParseException e) {
//				L.e(e);
			}
		}
		
		return null;
	}
	
	public static int getCurrentYear() {
		return Calendar.getInstance().get(Calendar.YEAR);
	}
	
	public static long getTimestamp(final Date date) {
		return date != null ? date.getTime() : 0;
	}
	
	public static String formatDate(final Date date, final DateFormat dateFormat) {
		if (dateFormat == null) {
			throw new IllegalArgumentException("dateFormat must not be null!");
		}
		
		return date != null ? dateFormat.format(date) : null;
	}
	
	public static String formatDate(final long timestamp, final DateFormat dateFormat) {
		if (dateFormat == null) {
			throw new IllegalArgumentException("dateFormat must not be null!");
		}
		
		return timestamp == 0 ? null : dateFormat.format(new Date(timestamp));
	}
	
	public static String formatFinishTimeDate(final long timestamp, final boolean isShortFormat) {
		return formatDate(timestamp, isShortFormat ? getShortFinishTimeDateFormat() : getFinishTimeDateFormat());
	}
	
	public static String formatIviDate(final long timestamp) {
		return formatDate(timestamp, getIviDateFormat());
	}
	
	public static String formatIso8601Date(final long timestamp) {
		return formatDate(timestamp, getIso8601DateFormat());
	}
	
	public static String formatClientDate(final long timestamp) {
		return formatDate(timestamp, getClientDateFormat());
	}
	
	public static String formatShortDate(final Date date) {
		return formatDate(date, getShortDateFormat());
	}
	
	public static String formatShortDate(final long timestamp) {
		return formatDate(timestamp, getShortDateFormat());
	}
	
	public static String formatLongDate(final Date date) {
		return formatDate(date, getLongDateFormat());
	}
	
	public static String formatLongDateWithoutZeros(final Date date) {
		return formatDate(date, getLongDateFormatWithoutZeros());
	}
	
	public static String formatLogcatDate(final Date date) {
		return formatDate(date, getLogcatDateFormat());
	}
	
	public static String formatCardDate(final long timestamp) {
		if (timestamp == 0) {
			return "";
		}
		final Calendar cardDate = Calendar.getInstance();
		cardDate.setTimeInMillis(timestamp);
		cardDate.add(Calendar.MONTH, -1);
		return formatDate(cardDate.getTimeInMillis(), getCardDateFormat());
	}
	
	public static String formatIso8601UtcDate(final long timestamp) {
		return formatDate(timestamp, getIso8601UTCDateFormat());
	}
	
	public static String formatTime(final int timeInSecs) {
		return formatTime(timeInSecs, false);
	}
	
	@SuppressLint("DefaultLocale")
	public static String formatTime(final int timeInSecs, final boolean addZeros) {
		final int secs = timeInSecs % 60;
		
		final int timeInMins = timeInSecs / 60;
		
		final int mins = timeInMins % 60;
		final int hours = timeInMins / 60;
		
		if (hours > 0 || addZeros) {
			return String.format("%d:%02d:%02d", hours, mins, secs);
		} else {
			return String.format("%d:%02d", mins, secs);
		}
	}
	
	public static int getTimeFromFormattedString(final String timeStr) {
		if (!TextUtils.isEmpty(timeStr)) {
			final String[] parts = timeStr.split(":");
			
			try {
				return Integer.parseInt(parts[0]) * MINUTE_IN_SECONDS * MINUTE_IN_SECONDS
					+ Integer.parseInt(parts[1]) * MINUTE_IN_SECONDS
					+ Integer.parseInt(parts[2]);
			} catch (final Exception e) {
//				L.e(e);
			}
		}
		
		return 0;
	}
	
	public static String formatHoursMinutesDate(final Date date) {
		return formatDate(date, getHoursMinutesFormat());
	}
	
	public static String formatHoursMinutesDate(final Date date, final TimeZone timeZone) {
		final DateFormat format = getHoursMinutesFormat();
		format.setTimeZone(timeZone);
		return formatDate(date, format);
	}
	
	public static int trimMillisToSeconds(final int millis) {
		return (int) (SECOND_IN_MILLIS * (millis / SECOND_IN_MILLIS));
	}
	
	public static int getDays(final int periodInSeconds) {
		return periodInSeconds / DAY_IN_SECONDS;
	}
	
	public static int getHours(final int periodInSeconds) {
		return (periodInSeconds - getDays(periodInSeconds) * DAY_IN_SECONDS) / HOUR_IN_SECONDS;
	}
	
	public static int getMinutes(final int periodInSeconds) {
		return (periodInSeconds
			- getDays(periodInSeconds) * DAY_IN_SECONDS
			- getHours(periodInSeconds) * HOUR_IN_SECONDS) / MINUTE_IN_SECONDS;
	}
	
	public static int getSeconds(final int periodInSeconds) {
		return periodInSeconds
			- getDays(periodInSeconds) * DAY_IN_SECONDS
			- getHours(periodInSeconds) * HOUR_IN_SECONDS
			- getMinutes(periodInSeconds) * MINUTE_IN_SECONDS;
	}
	
	public static DateFormat getDateTimeFormat() {
		return new SimpleDateFormat(DATE_TIME_PATTERN, RU_LOCALE);
	}
	
	public static String getDateRuLocalText(final Date date) {
		return new SimpleDateFormat(DATE_LOCAL_PATTERN, RU_LOCALE).format(date);
	}
	
	public static String getDateRuLocalTextWithoutZeros(final Date date) {
		return new SimpleDateFormat(DATE_LOCAL_PATTERN_WITHOUT_ZEROS, RU_LOCALE).format(date);
	}
	
	public static boolean isDelayExpired(final long startTimestamp, final long delayMillis) {
		return System.currentTimeMillis() - startTimestamp > delayMillis;
	}
	
	private static DateFormat getIviDateFormat() {
		return SIMPLE_DATE_FORMAT;
	}
	
	private static DateFormat getIso8601DateFormat() {
		return new SimpleDateFormat(ISO8601_DATE_PATTERN, Locale.US);
	}
	
	private static DateFormat getClientDateFormat() {
		return new SimpleDateFormat(CLIENT_DATE_PATTERN, Locale.US);
	}
	
	private static DateFormat getIso8601UTCDateFormat() {
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ISO8601_UTC_DATE_PATTERN, Locale.US);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return simpleDateFormat;
	}
	
	private static DateFormat getShortDateFormat() {
		return new SimpleDateFormat(SHORT_DATE_PATTERN, RU_LOCALE);
	}
	
	private static DateFormat getLongDateFormat() {
		return new SimpleDateFormat(LONG_DATE_PATTERN, RU_LOCALE);
	}
	
	private static DateFormat getLongDateFormatWithoutZeros() {
		return new SimpleDateFormat(LONG_DATE_PATTERN_WITHOUT_ZEROS, RU_LOCALE);
	}
	
	private static DateFormat getCardDateFormat() {
		return new SimpleDateFormat(CARD_DATE_PATTERN, Locale.US);
	}
	
	private static DateFormat getHoursMinutesFormat() {
		return new SimpleDateFormat(HOURS_MINUTES_PATTERN, RU_LOCALE);
	}
}