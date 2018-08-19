package android.samutils.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtils extends android.text.format.DateUtils {

	public static final int DAY_IN_SECONDS = (int) (DAY_IN_MILLIS / SECOND_IN_MILLIS);
	public static final int MINUTE_IN_SECONDS = 60;

	private static final String SHORT_IVI_DATE_PATTERN = "yyyy-MM-dd";
	private static final String IVI_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
	private static final String ISO8601_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
	private static final String SHORT_DATE_PATTERN = "dd.MM.yyyy";
	private static final String LONG_DATE_PATTERN = "dd MMMM yyyy";

	private static DateFormat getShortIviDateFormat() {
		return new SimpleDateFormat(SHORT_IVI_DATE_PATTERN, Locale.US);
	}

	private static DateFormat getIviDateFormat() {
		return new SimpleDateFormat(IVI_DATE_PATTERN, Locale.US);
	}

	private static DateFormat getIso8601DateFormat() {
		return new SimpleDateFormat(ISO8601_DATE_PATTERN, Locale.US);
	}

	private static DateFormat getShortDateFormat() {
		return new SimpleDateFormat(SHORT_DATE_PATTERN, new Locale("ru"));
	}

	private static DateFormat getLongDateFormat() {
		return new SimpleDateFormat(LONG_DATE_PATTERN, new Locale("ru"));
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
				return parseDate(dateString, getIso8601DateFormat());
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

	public static String formatIviDate(final Date date) {
		return formatDate(date, getIviDateFormat());
	}

	public static String formatIviDate(final long timestamp) {
		return formatDate(timestamp, getIviDateFormat());
	}

	public static String formatIso8601Date(final Date date) {
		return formatDate(date, getIso8601DateFormat());
	}

	public static String formatIso8601Date(final long timestamp) {
		return formatDate(timestamp, getIso8601DateFormat());
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

	public static String formatLongDate(final long timestamp) {
		return formatDate(timestamp, getLongDateFormat());
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
}
