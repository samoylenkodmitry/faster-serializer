package ru.ivi.utils;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({ "WeakerAccess", "unused" })
public class Assert {
	
	private static final AtomicBoolean EXCEPTION_CATCH_IN_PROGRESS_FLAG = new AtomicBoolean();
	private static final long NON_FATAL_DELAY_MILLIS = 1000L;
	private static final Map<Throwable, Drawable> INFO_DRAWABLES = new ConcurrentHashMap<>();
	public static final Checker<StackTraceElement> STACK_TRACE_ELEMENT_HAS_NO_RX_JAVA_CHECKER =
		stackTraceElement -> !stackTraceElement.getClassName().startsWith("io.reactivex");
	
	public static boolean sIsSendNonFatals = true;

	public interface AssertNonFatalHandler {
		
		void handleNonFatal(final Throwable e);
	}

	public interface AssertExceptionHandler {

		void handleException(final Throwable e);
	}

	public static volatile AssertNonFatalHandler sNonFatalHandler = null;

	public static volatile AssertExceptionHandler sDialogHandler = null;
	@SuppressWarnings("WeakerAccess")
	public static boolean sIsThrowOnException = false;//useful for unit-tests
	
	public static void fail() {
		fail(new Exception());
	}
	
	public static Drawable getInfoDrawable(final Throwable thr) {
		return thr == null ? null : INFO_DRAWABLES.get(thr);
	}
	
	public static void fail(final Throwable exception, final Drawable infoDrawable) {
		if (sIsSendNonFatals) {
			handleNonFatal(exception);
		} else {
			INFO_DRAWABLES.put(exception, infoDrawable);
			showReportDialog(exception);
		}
	}
	
	public static void fail(final Throwable exception) {
		if (sIsSendNonFatals) {
			handleNonFatal(exception);
		} else {
			showReportDialog(exception);
		}
	}
	
	public static void fail(final String mes) {
		if (sIsSendNonFatals) {
			handleNonFatal(createException(mes));
		} else {
			showReportDialog(createException(mes));
		}
	}
	
	public static void fail(final String mes, final Throwable exception) {
		final AssertionException ex = createException(mes);
		ex.initCause(exception);
		
		if (sIsSendNonFatals) {
			handleNonFatal(ex);
		} else {
			showReportDialog(ex);
		}
	}
	
	public static void failWithFakeClass(final Throwable exception, final Class fakeClass, final String message) {
		final StackTraceElement fakeElem = new StackTraceElement(
			fakeClass.getCanonicalName(),
			fakeClass.getSimpleName(),
			fakeClass.getSimpleName() + ".java",
			message.hashCode());
		
		exception.setStackTrace(ArrayUtils.concat(
			new StackTraceElement[] { fakeElem },
			removeIoReactive(exception)));
		
		for (Throwable th = exception.getCause(); th != null; th = th.getCause()) {
			th.setStackTrace(removeIoReactive(th));
		}
		
		fail(exception);
	}
	
	@NonNull
	private static StackTraceElement[] removeIoReactive(final Throwable th) {
		return ArrayUtils.filterNonNull(
			StackTraceElement.class, th.getStackTrace(), STACK_TRACE_ELEMENT_HAS_NO_RX_JAVA_CHECKER);
	}
	
	public static void assertNotNull(final Object o) {
		if (o == null) {
			if (sIsSendNonFatals) {
				handleNonFatal(createException());
			} else {
				showReportDialog(createException());
			}
		}
	}

	private static void handleNonFatal(final Throwable exception) {
		if (sIsThrowOnException) {
			throwExceptionNonCatch(exception);
		}
		exception.printStackTrace();
		try {
			sendNonFatal(exception);
		} catch (final Throwable ignore) {
			ignore.printStackTrace();
		}
	}

	public static void assertNotNull(final String mes, final Object o) {
		if (o == null) {
			if (sIsSendNonFatals) {
				handleNonFatal(createException(mes));
			} else {
				showReportDialog(createException(mes));
			}
		}
	}
	
	public static void assertEquals(final int i, final int j) {
		if (i != j) {
			final AssertionException exception = createException("i=" + i + ", j=" + j);
			if (sIsSendNonFatals) {
				handleNonFatal(exception);
			} else {
				showReportDialog(exception);
			}
		}
	}
	
	public static void assertEquals(final String mes, final int i, final int j) {
		if (i != j) {
			final AssertionException exception = createException(mes, " i=" + i + ", j=" + j);
			if (sIsSendNonFatals) {
				handleNonFatal(exception);
			} else {
				showReportDialog(exception);
			}
		}
	}
	
	public static void assertEquals(final long li, final long lj) {
		if (li != lj) {
			final AssertionException exception = createException("i=" + li + ", j=" + lj);
			if (sIsSendNonFatals) {
				handleNonFatal(exception);
			} else {
				showReportDialog(exception);
			}
		}
	}
	
	public static void assertEquals(final String mes, final long li, final long lj) {
		if (li != lj) {
			final AssertionException exception = createException(mes, " i=" + li + ", j=" + lj);
			if (sIsSendNonFatals) {
				handleNonFatal(exception);
			} else {
				showReportDialog(exception);
			}
		}
	}
	
	public static void assertNotSame(final int i, final int j) {
		if (i == j) {
			final AssertionException exception = createException("i=" + i + ", j=" + j);
			if (sIsSendNonFatals) {
				handleNonFatal(exception);
			} else {
				showReportDialog(exception);
			}
		}
	}
	
	public static void assertNotSame(final String mes, final Object o, final Object o1) {
		if (o == o1 || o == null && o1 == null || o != null && o.equals(o1)) {
			final AssertionException exception = createException(mes, "o=" + o + ", o1=" + o1);
			if (sNonFatalHandler != null) {
				handleNonFatal(exception);
			} else {
				showReportDialog(exception);
			}
		}
	}
	
	public static void assertEquals(final String mes, final Object o, final Object o1) {
		if (!(o == o1 || o == null && o1 == null || o != null && o.equals(o1))) {
			final AssertionException exception = createException(mes, "o=" + o + ", o1=" + o1);
			if (sIsSendNonFatals) {
				handleNonFatal(exception);
			} else {
				showReportDialog(exception);
			}
		}
	}
	
	public static void assertEquals(final Object o, final Object o1) {
		if (!(o == o1 || o == null && o1 == null || o != null && o.equals(o1))) {
			final AssertionException exception = createException("o=" + o + ", o1=" + o1);
			if (sIsSendNonFatals) {
				handleNonFatal(exception);
			} else {
				showReportDialog(exception);
			}
		}
	}
	
	public static void assertFalse(final boolean condition) {
		if (condition) {
			if (sIsSendNonFatals) {
				handleNonFatal(createException());
			} else {
				showReportDialog(createException());
			}
		}
	}
	
	public static void assertFalse(final String mes, final boolean condition) {
		if (condition) {
			if (sIsSendNonFatals) {
				handleNonFatal(createException(mes));
			} else {
				showReportDialog(createException(mes));
			}
		}
	}
	
	public static void assertTrue(final String mes, final boolean condition) {
		if (!condition) {
			if (sIsSendNonFatals) {
				handleNonFatal(createException(mes));
			} else {
				showReportDialog(createException(mes));
			}
		}
	}
	
	public static void assertTrue(final boolean condition) {
		if (!condition) {
			if (sIsSendNonFatals) {
				handleNonFatal(createException());
			} else {
				showReportDialog(createException());
			}
		}
	}
	
	public static void assertNull(final Object o) {
		if (o != null) {
			if (sIsSendNonFatals) {
				handleNonFatal(createException());
			} else {
				showReportDialog(createException());
			}
		}
	}
	
	public static void nonFatal(final Throwable e) {
		handleNonFatal(e);
	}
	
	public static void nonFatal(final String mes) {
		handleNonFatal(createException(mes));
	}

	@SuppressWarnings("unchecked")
	public static <T extends Throwable> void throwExceptionNonCatch(final Throwable exception) throws T {
		throw (T) exception;
	}
	
	private static void showReportDialog(final Throwable exception) {
		handleNonFatal(exception);
		if (sDialogHandler != null) {
			sDialogHandler.handleException(exception);
		}
	}

	public static void throwException(final Throwable exception) {
		ThreadUtils.MAIN_THREAD_HANDLER.postDelayed(() ->
			Assert.throwExceptionNonCatch(exception), 5000L);
	}
	
	@NonNull
	private static AssertionException createException(final String... mes) {
		try {
			final StackTraceElement[] trace = Thread.currentThread().getStackTrace();
			final StackTraceElement elem = trace[4];
			final String message = Arrays.toString(mes) + "\n" +
				elem.getClassName() + "#" +
				elem.getMethodName() + ":" +
				elem.getLineNumber();

			final AssertionException e = new AssertionException(message);
			final StackTraceElement[] copy = Arrays.copyOfRange(trace, 4, trace.length);
			e.setStackTrace(copy);
			e.printStackTrace();
			return e;
		} catch (final Throwable ex) {
			return new AssertionException(ex);
		}
	}
	
	public static class AssertionException extends Exception {
		
		public AssertionException(final String message) {
			super(message);
		}

		public AssertionException(final Throwable ex) {
			super(ex);
		}
		
		public AssertionException(final String message, final Throwable ex) {
			super(message, ex);
		}
	}
	
	private static void sendNonFatal(final Throwable e) {
		ThreadUtils.MAIN_THREAD_HANDLER.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (EXCEPTION_CATCH_IN_PROGRESS_FLAG.compareAndSet(false, true)) {
					try {
						sNonFatalHandler.handleNonFatal(e);
					} catch (final Throwable ignore) {
						ignore.printStackTrace();
					} finally {
						EXCEPTION_CATCH_IN_PROGRESS_FLAG.set(false);
					}
				} else {
					ThreadUtils.MAIN_THREAD_HANDLER.postDelayed(this, NON_FATAL_DELAY_MILLIS);
				}
			}
		}, NON_FATAL_DELAY_MILLIS);
	}
	
	public interface CheckedRunnable {
		
		void run() throws Exception;
	}
	
	public static void safelyRunTaskChecked(final CheckedRunnable runnable) {
		try {
			runnable.run();
		} catch (final Throwable toNoFatal) {
			fail(toNoFatal);
		}
	}
	
	public static void safelyRunTask(final Runnable runnable) {
		try {
			runnable.run();
		} catch (final Throwable toNoFatal) {
			fail(toNoFatal);
		}
	}
	
	public static <T> T safe(final Callable<T> runnable) {
		try {
			return runnable.call();
		} catch (final Throwable toNoFatal) {
			fail(toNoFatal);
		}
		return null;
	}
	
	public static <T> T safe(final String mes, final Callable<T> runnable) {
		try {
			return runnable.call();
		} catch (final Throwable toNoFatal) {
			fail(mes, toNoFatal);
		}
		return null;
	}
	
	public static class Debug {
		
		private static final boolean IS_DEBUG = BuildConfig.DEBUG;
		
		public interface Func1<R> {
			
			R call();
		}
		
		public static void assertTrue(final Func1<String> mes, final Func1<Boolean> condition) {
			if (IS_DEBUG) {
				Assert.assertTrue(mes.call(), condition.call());
			}
		}
	}
}
