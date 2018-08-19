package android.samutils.utils;

import java.util.Arrays;

public class Assert {
	
	public interface AssertNonFatalHandler {
		
		void handleNonFatal(final Error e);
	}
	
	public static volatile AssertNonFatalHandler sNonFatalHandler = null;
	
	public static void assertNotNull(final Object o) {
		if (o == null) {
			if (sNonFatalHandler != null) {
				sNonFatalHandler.handleNonFatal(createException());
			} else {
				throw createException();
			}
		}
	}
	
	public static void assertNotNull(final String mes, final Object o) {
		if (o == null) {
			if (sNonFatalHandler != null) {
				sNonFatalHandler.handleNonFatal(createException(mes));
			} else {
				throw createException(mes);
			}
		}
	}
	
	public static void assertEquals(final int i, final int j) {
		if (i != j) {
			if (sNonFatalHandler != null) {
				sNonFatalHandler.handleNonFatal(createException());
			} else {
				throw createException("i=" + i + ", j=" + j);
			}
		}
	}
	
	public static void assertEquals(final String mes, final int i, final int j) {
		if (i != j) {
			if (sNonFatalHandler != null) {
				sNonFatalHandler.handleNonFatal(createException());
			} else {
				throw createException(mes + " i=" + i + ", j=" + j);
			}
		}
	}
	
	public static void assertNotSame(final int i, final int j) {
		if (i == j) {
			if (sNonFatalHandler != null) {
				sNonFatalHandler.handleNonFatal(createException());
			} else {
				throw createException("i=" + i + ", j=" + j);
			}
		}
	}
	
	public static void assertEquals(final String mes, final Object o, final Object o1) {
		if (!(o == o1 || o == null && o1 == null || o != null && o.equals(o1))) {
			if (sNonFatalHandler != null) {
				sNonFatalHandler.handleNonFatal(createException(mes));
			} else {
				throw createException(mes, "o=" + o + ", o1=" + o1);
			}
		}
	}
	
	public static void assertEquals(final Object o, final Object o1) {
		if (!(o == o1 || o == null && o1 == null || o != null && o.equals(o1))) {
			if (sNonFatalHandler != null) {
				sNonFatalHandler.handleNonFatal(createException());
			} else {
				throw createException("o=" + o + ", o1=" + o1);
			}
		}
	}
	
	public static void assertFalse(final boolean condition) {
		if (condition) {
			if (sNonFatalHandler != null) {
				sNonFatalHandler.handleNonFatal(createException());
			} else {
				throw createException();
			}
		}
	}
	
	public static void assertFalse(final String mes, final boolean condition) {
		if (condition) {
			if (sNonFatalHandler != null) {
				sNonFatalHandler.handleNonFatal(createException(mes));
			} else {
				throw createException(mes);
			}
		}
	}
	
	public static void assertTrue(final String mes, final boolean condition) {
		if (!condition) {
			if (sNonFatalHandler != null) {
				sNonFatalHandler.handleNonFatal(createException(mes));
			} else {
				throw createException(mes);
			}
		}
	}
	
	public static void assertTrue(final boolean condition) {
		if (!condition) {
			if (sNonFatalHandler != null) {
				sNonFatalHandler.handleNonFatal(createException());
			} else {
				throw createException();
			}
		}
	}
	
	public static void assertNull(final Object o) {
		if (o != null) {
			if (sNonFatalHandler != null) {
				sNonFatalHandler.handleNonFatal(createException());
			} else {
				throw createException();
			}
		}
	}
	
	private static Error createException(final String... mes) {
		final StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		final StackTraceElement elem = trace[4];
		final String message = elem.getClassName() + "#" + elem.getMethodName() + ":" + elem.getLineNumber() + " " + Arrays.toString(mes);
		final Error e = new Error(message);
		final StackTraceElement[] copy = Arrays.copyOfRange(trace, 4, trace.length);
		e.setStackTrace(copy);
		return e;
	}
	
}
