package ru.ivi.utils;


import android.text.TextUtils;

public final class ParseUtils {

	public static Integer tryParseInt(final String value) {
		if (TextUtils.isEmpty(value)) {
			return null;
		}

		try {
			return Integer.parseInt(value);
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	public static int tryParseInt(final String value, final int defValue) {
		final Integer result = tryParseInt(value);
		return result == null ? defValue : result.intValue();
	}

	public static Float tryParseFloat(final String value) {
		if (TextUtils.isEmpty(value)) {
			return null;
		}

		try {
			return Float.parseFloat(value);
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	@SuppressWarnings("SameParameterValue")
	public static float tryParseFloat(final String value, final float defValue) {
		final Float result = tryParseFloat(value);
		return result == null ? defValue : result.floatValue();
	}

	public static Long tryParseLong(final String value) {
		if (TextUtils.isEmpty(value)) {
			return null;
		}

		try {
			return Long.parseLong(value);
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	public static long tryParseLong(final String value, final long defValue) {
		final Long result = tryParseLong(value);
		return result == null ? defValue : result.longValue();
	}

	public static Double tryParseDouble(final String value) {
		if (TextUtils.isEmpty(value)) {
			return null;
		}

		try {
			return Double.parseDouble(value);
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	public static double tryParseDouble(final String value, final double defValue) {
		final Double result = tryParseDouble(value);
		return result == null ? defValue : result.doubleValue();
	}

	public static Boolean tryParseBoolean(final String value) {
		if (TextUtils.isEmpty(value)) {
			return null;
		}

		try {
			return Boolean.parseBoolean(value);
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	public static boolean tryParseBoolean(final String value, final boolean defValue) {
		final Boolean result = tryParseBoolean(value);
		if (result == null || result == Boolean.FALSE) {
			final Integer intResult = tryParseInt(value, 0);
			return intResult == null ? defValue : intResult == 1;
		} else {
			return result;
		}
	}
}
