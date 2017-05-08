package android.samutils.utils;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class StringUtils {
	
	private static final String ZEROS = "00000000000000000000000000000000"; // 32 zeros character
	public static final String ELLIPSISZE = "…";

	public static final String LF = "\n";
	private static final String CHARSET_ISO_8859_1 = "ISO_8859_1";
	
	// Source: http://stackoverflow.com/a/32101331
	public static String filterEmojis(final String emojis) {
		if (emojis != null) {
			final int count = emojis.length();

			if (count > 1) {
				final StringBuilder builder = new StringBuilder();

				final int lastIndex = count - 1;

				for (int i = 0; i < count; i++) {
					if (i < lastIndex) { // Emojis are two characters long in java, e.g. a rocket emoji is "\uD83D\uDE80";
						final int next_i = i + 1;

						if (Character.isSurrogatePair(emojis.charAt(i), emojis.charAt(next_i))) {
							i = next_i; //also skip the second character of the emoji

							continue;
						}
					}

					builder.append(emojis.charAt(i));
				}

				return builder.toString();
			} else {
				return emojis;
			}
		} else {
			return null;
		}
	}

	public static String smartShorten(final String str, final int maxLen, final boolean useWordBoundary) {
		if (maxLen <= 0) {
			throw new IllegalArgumentException("Argument maxLen must be greater than 0!");
		}
		
		if (str != null) {
			if (str.length() > maxLen) {
				if (useWordBoundary) {
					final int lastSpaceIndex = str.lastIndexOf(" ");
					
					if (0 < lastSpaceIndex && lastSpaceIndex < maxLen) {
						return str.substring(0, lastSpaceIndex);
					}
				}
				
				return str.substring(0, maxLen);
			} else {
				return str;
			}
		} else {
			return null;
		}
	}
	
	public static boolean hasDigitCharacter(final CharSequence chars) {
		final int len = chars.length();
		for (int i = 0; i < len; i++) {
			if (Character.isDigit(chars.charAt(i))) {

				return true;
			}
		}

		return false;
	}

	public static int getLength(final CharSequence chars) {
		return chars == null ? 0 : chars.length();
	}

	public static String getDigits(final String str) {
		if (TextUtils.isEmpty(str)) {

			return str;
		}
		final int sz = str.length();
		final char[] chs = new char[sz];
		int count = 0;
		for (int i = 0; i < sz; i++) {
			if (Character.isDigit(str.charAt(i))) {
				chs[count++] = str.charAt(i);
			}
		}
		if (count == sz) {

			return str;
		}

		return new String(chs, 0, count);
	}

	public static boolean startsWith(final CharSequence chars, final CharSequence prefix) {
		if (chars == null || prefix == null) {

			return false;
		}

		final int lenPrefix = prefix.length();

		if (lenPrefix > chars.length()) {

			return false;
		}

		for (int i = 0; i < lenPrefix; i++) {
			if (prefix.charAt(i) != chars.charAt(i)) {

				return false;
			}
		}

		return true;
	}

	public static String concat(final String[] strings, final String delimiter) {
		if (ArrayUtils.isEmpty(strings)) {
			return "";
		} else {
			final StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < strings.length; i++) {
				final String title = strings[i];
				if (title != null && title.length() > 0) {
					if (0 < i && stringBuilder.length() > 0) {
						stringBuilder.append(delimiter);
					}
					stringBuilder.append(title);
				}
			}

			return stringBuilder.toString();
		}
	}
	
	public static CharSequence removeHtmlTags(final CharSequence seq) {
		if (seq == null || seq.length() == 0) {
			return seq;
		}
		
		final StringBuilder stringBuilder = new StringBuilder(seq);
		int index = 0;
		while (index != -1) {
			index = stringBuilder.indexOf("<");
			final int indexEnd = stringBuilder.indexOf(">", index);
			if (index != -1 && indexEnd != -1) {
				stringBuilder.delete(index, indexEnd + 1);
			}
		}
		
		return stringBuilder.toString();
	}

	public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
		if (cs1 == cs2) {
			return true;
		}
		if (cs1 == null || cs2 == null) {
			return false;
		}
		if (cs1 instanceof String && cs2 instanceof String) {
			return cs1.equals(cs2);
		}
		return cs1.toString().equals(cs2.toString());
	}

	public static boolean contains(final CharSequence seq, final CharSequence searchSeq) {
		if (seq == null || searchSeq == null) {
			return false;
		}
		return indexOf(seq, searchSeq, 0) >= 0;
	}

	public static int indexOf(final CharSequence cs, final CharSequence searchChar, final int start) {
		return cs.toString().indexOf(searchChar.toString(), start);
	}

	public static String linefeedToHtml(final String text) {
		if (TextUtils.isEmpty(text)) {
			return null;
		}

		return text.replaceAll(LF, "<br />");
	}
	
	public static String getMd5Hash(final String input) {
		try {
			final MessageDigest md = MessageDigest.getInstance("MD5");
			final byte[] messageDigest = md.digest(input.getBytes());
			final BigInteger number = new BigInteger(1, messageDigest);
			String md5 = number.toString(16);

			final int addendCount = 32 - md5.length();

			if (addendCount > 0) {
				md5 = ZEROS.substring(0, addendCount) + md5;
			}

			return md5;
		} catch (final NoSuchAlgorithmException e) {
			return null;
		}
	}
	
	public static byte[] getBytes(final String sIn) {
		if (sIn != null) {
			try {
				return sIn.getBytes(CHARSET_ISO_8859_1);
			} catch (final UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public static String wrapBytes(final byte[] bytes) {
		if (bytes != null) {
			try {
				return new String(bytes, CHARSET_ISO_8859_1);
			} catch (final UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static String left(final String str, final int len) {
		return str == null ? null : (len < 0 ? "" : (str.length() <= len ? str : str.substring(0, len)));
	}
	
	/**
	 * <p>Checks if a CharSequence is whitespace, empty ("") or null.</p>
	 *
	 * <pre>
	 * StringUtils.isBlank(null)      = true
	 * StringUtils.isBlank("")        = true
	 * StringUtils.isBlank(" ")       = true
	 * StringUtils.isBlank("bob")     = false
	 * StringUtils.isBlank("  bob  ") = false
	 * </pre>
	 *
	 * @param cs  the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is null, empty or whitespace
	 * @since 2.0
	 * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
	 */
	public static boolean isBlank(final CharSequence cs) {
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(cs.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}
}
