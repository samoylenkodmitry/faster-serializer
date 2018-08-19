package ru.ivi.utils;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({ "TooBroadScope", "WeakerAccess", "unused" })
public class Tracer {
	
	private static final boolean IS_DISABLED = !BuildConfig.DEBUG;
	private static final boolean FULL_LOGS = false;
	private static final Map<String, Long> SUM = new HashMap<>();
	private static final Map<String, Long> MAX = new HashMap<>();
	private static final Map<String, Long> MIN = new HashMap<>();
	private static final Map<String, Long> COUNT = new HashMap<>();
	private static final Map<String, Long> FROM_TIME = new HashMap<>();
	private static final Map<String, String> FROM_EX = new HashMap<>();
	private static final AtomicInteger FROM_TO_COUNTER = new AtomicInteger();
	private static final Queue<Integer> COUNTER_QUE = new ConcurrentLinkedQueue<>();
	
	public interface TimeProvider {
		
		long time();
	}
	
	public interface Logger {
		
		void log(final String tag, final String mes);
	}
	
	public interface Go<Result> {
		
		Result go();
	}
	
	public interface GoVoid {
		
		void go();
	}
	
	private static TimeProvider sTimeProvider = SystemClock::elapsedRealtimeNanos;
	private static Logger sLogger = Log::e;
	private static final GoVoid TEST_EMPTY_VOID = () -> {
	};
	
	public static void setTimeProvider(final TimeProvider timeProvider) {
		sTimeProvider = timeProvider;
	}
	
	public static void setLogger(final Logger logger) {
		sLogger = logger;
	}
	
	public static void logCallStack(final String tag) {
		if (IS_DISABLED) {
			return;
		}
		logCallStackN(tag, 1);
	}
	
	public static void logCallStackAll(final String tag) {
		if (IS_DISABLED) {
			return;
		}
		logCallStackN(tag, 1);
		new Error(tag).printStackTrace();
	}
	
	public static void logCallStack2(final String tag) {
		if (IS_DISABLED) {
			return;
		}
		logCallStackN(tag, 2);
	}
	
	public static void logCallStackN(final String tag, final int n) {
		if (IS_DISABLED) {
			return;
		}
		final Thread thread = Thread.currentThread();
		final StackTraceElement[] stackTrace = thread.getStackTrace();
		ThreadUtils.runOnWorker(() ->
			log(getThreadTag(thread), getExecutionPoint(stackTrace[n + 3]) + "\t<-\t" + getExecutionPoint(stackTrace[n + 4]) + "\t" + tag));
	}
	
	public static void logCall(final String tag, final StackTraceElement[] stackTrace, final Thread thread) {
		final int n = 0;
		logCallN(tag, stackTrace, thread, n);
	}
	
	public static void logCall1(final String tag, final StackTraceElement[] stackTrace, final Thread thread) {
		final int n = 1;
		logCallN(tag, stackTrace, thread, n);
	}
	
	public static void logCallN(final String tag, final StackTraceElement[] stackTrace, final Thread thread, final int n) {
		if (IS_DISABLED) {
			return;
		}
		ThreadUtils.runOnWorker(() ->
			log(getThreadTag(thread), getExecutionPoint(stackTrace[n + 3]) + "\t<-\t" + getExecutionPoint(stackTrace[n + 4]) + "\t" + tag));
	}
	
	public static void from(final String tag) {
		if (IS_DISABLED) {
			return;
		}
		final String fromEx = getExecutionPoint(Thread.currentThread().getStackTrace()[3]);
		final int count = FROM_TO_COUNTER.incrementAndGet();
		COUNTER_QUE.add(count);
		final String tagAndCount = tag + count;
		FROM_EX.put(tagAndCount, fromEx + tag);
		final long v;
		v = time();
		FROM_TIME.put(tagAndCount, v);
	}
	
	public static void to(final String tag) {
		if (IS_DISABLED) {
			return;
		}
		final long t2 = time();
		final Integer poll = COUNTER_QUE.poll();
		final String tagGet = tag + String.valueOf(poll == null ? FROM_TO_COUNTER.get() : poll.intValue());
		final Long t = FROM_TIME.get(tagGet);
		if (t != null) {
			final String fromEx = FROM_EX.get(tagGet);
			final long dt = t2 - t - getEmptyGoerDelayNanos();
			logInfo(Thread.currentThread().getStackTrace()[3], dt, fromEx);
		}
	}
	
	public static void t(final GoVoid g) {
		if (IS_DISABLED) {
			g.go();
			return;
		}
		final Thread thread = Thread.currentThread();
		final StackTraceElement el = thread.getStackTrace()[3];
		final long t, t2;
		t = time();
		g.go();
		t2 = time();
		final long dt = t2 - t - getEmptyGoerDelayNanos();
		logInfo(el, dt, getThreadTag(thread));
	}
	
	public static long time() {
		return sTimeProvider.time();
	}
	
	public static <R> R t(final Go<R> g) {
		if (IS_DISABLED) {
			return g.go();
		}
		final Thread thread = Thread.currentThread();
		final StackTraceElement el = thread.getStackTrace()[3];
		final long t, t2;
		final R go;
		t = time();
		go = g.go();
		t2 = time();
		final long dt = t2 - t - getEmptyGoerDelayNanos();
		logInfo(el, dt, getThreadTag(thread));
		return go;
	}
	
	@NonNull
	private static String getThreadTag(final Thread thread) {
		return "tr-" + thread.getId() + " " + thread.getName();
	}
	
	private static void log(final String threadTag, final String msg) {
		final int len = msg.length();
		if (FULL_LOGS && len > 100) {
			for (final String s : StringUtils.splitBySize(msg, 100)) {
				sLogger.log(threadTag, s);
			}
		} else {
			sLogger.log(threadTag, msg);
		}
	}
	
	private static long getEmptyGoerDelayNanos() {
		final long t1, t2;
		t1 = time();
		TEST_EMPTY_VOID.go();
		t2 = time();
		return t2 - t1;
	}
	
	private static void logInfo(final StackTraceElement el, final long dt, final String exMes) {
		if (IS_DISABLED) {
			return;
		}
		final String executionPoint = getExecutionPoint(el);
		Long count = COUNT.get(executionPoint);
		if (count == null) {
			count = 0L;
		}
		count++;
		COUNT.put(executionPoint, count);
		
		Long max = MAX.get(executionPoint);
		if (max == null) {
			max = dt;
		}
		if (dt > max) {
			max = dt;
		}
		MAX.put(executionPoint, max);
		
		
		Long min = MIN.get(executionPoint);
		if (min == null) {
			min = dt;
		}
		if (dt < min) {
			min = dt;
		}
		MIN.put(executionPoint, min);
		
		Long sum = SUM.get(executionPoint);
		if (sum == null) {
			sum = dt;
		} else {
			sum += dt;
		}
		SUM.put(executionPoint, sum);
		
		final long avg = sum / count;
		
		log("tracer", dt + executionPoint + (exMes == null ? "" : " " + exMes)
			+ " (avg, min, max, count, sum) = (" + avg + ", " + min + ", " + max + ", " + count + ", " + sum + ")");
	}
	
	@NonNull
	private static String getExecutionPoint(final StackTraceElement el) {
		final String[] split = el.getClassName().split("\\.");
		final String className = split[split.length - 1];
		final String[] inners = className.split("\\$+");
		final String filename;
		if (inners.length < 1) {
			filename = className;
		} else if (inners.length < 2) {
			filename = inners[inners.length - 1];
		} else if (inners.length < 3) {
			filename = inners[inners.length - 2];
		} else {
			filename = inners[inners.length - 3];
		}
		return "\t" + el.getMethodName() + "\t.(" + filename + ".java:" + el.getLineNumber() + ")";
	}
}
