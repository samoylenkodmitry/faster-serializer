package ru.ivi.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ThreadUtils {
	
	public static final long MAIN_THREAD_DEADLINE_MILLIS = 4000L;
	public static final long NON_MAIN_THREAD_DEADLINE_MILLIS = 15000L;
	public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	private static final Looper MAIN_LOOPER = Looper.getMainLooper();
	public static final Handler MAIN_THREAD_HANDLER = new Handler(MAIN_LOOPER);
	private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
	
	public interface ValueObtainer<T> {
		
		void obtainValue(ValueContainer<T> container);
	}
	
	public interface ValueContainer<T> {
		
		void writeValue(T val);
		
		T readValue();
	}
	
	/**
	 * Blocking await until {@link ValueContainer#writeValue(Object)} called
	 */
	public static <T> T blockingObtain(final ValueObtainer<T> obtainer) {
		final CountDownLatch latch = new CountDownLatch(1);
		final ValueContainer<T> container = new ValueContainer<T>() {
			
			private volatile T mVal;
			
			@Override
			public void writeValue(final T val) {
				mVal = val;
				latch.countDown();
			}
			
			@Override
			public T readValue() {
				return mVal;
			}
		};
		
		obtainer.obtainValue(container);
		
		try {
			latch.await();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		
		return container.readValue();
	}
	
	@Nullable
	public static <T> T tryRunWithDeadline(final Callable<T> runnable) {
		final long millisDeadline = isOnMainThread() ? MAIN_THREAD_DEADLINE_MILLIS : NON_MAIN_THREAD_DEADLINE_MILLIS;
		return tryRunWithDeadline(runnable, millisDeadline);
	}
	
	@Nullable
	public static <T> T tryRunWithDeadline(final Callable<T> runnable, final long deadlineMillis) {
		
		final StackTraceElement[] traceForHistory = Thread.currentThread().getStackTrace();
		final Future<T> future = EXECUTOR.submit(() -> {
			try {
				return runnable.call();
			} catch (final Throwable e) {
				final Exception exception = new Exception("could not run", e);
				exception.setStackTrace(traceForHistory);
				
				Assert.nonFatal(exception);
			}
			return null;
		});
		
		T result = null;
		try {
			result = future.get(deadlineMillis, TimeUnit.MILLISECONDS);
		} catch (final Throwable ignore) {
			ignore.printStackTrace();
		}
		return result;
	}
	
	public static void tryRunWithDeadline(final Runnable runnable) {
		final long millisDeadline = isOnMainThread() ? MAIN_THREAD_DEADLINE_MILLIS : NON_MAIN_THREAD_DEADLINE_MILLIS;
		tryRunWithDeadline(runnable, millisDeadline);
	}
	
	public static void tryRunWithDeadline(final Runnable runnable, final long deadlineMillis) {
		
		final StackTraceElement[] traceForHistory = Thread.currentThread().getStackTrace();
		final Future<?> future = EXECUTOR.submit(() -> {
			try {
				runnable.run();
				return null;
			} catch (final Throwable e) {
				final Exception exception = new Exception("could not run", e);
				exception.setStackTrace(traceForHistory);
				
				Assert.nonFatal(exception);
			}
			return null;
		});
		
		try {
			future.get(deadlineMillis, TimeUnit.MILLISECONDS);
		} catch (final Throwable ignore) {
			ignore.printStackTrace();
		}
	}
	
	@Nullable
	public static <T> T runBlocking(final Callable<T> callable, final ExecutorService executor) {
		final StackTraceElement[] traceForHistory = Thread.currentThread().getStackTrace();
		final Future<T> future = executor.submit(() -> {
			try {
				return callable.call();
			} catch (final Throwable e) {
				final Exception exception = new Exception("could not run", e);
				exception.setStackTrace(traceForHistory);
				
				Assert.nonFatal(exception);
			}
			return null;
		});
		
		T result = null;
		try {
			result = future.get();
		} catch (final Throwable ignore) {
			ignore.printStackTrace();
		}
		return result;
	}
	
	public static void assertMainThread(final String mes) {
		Assert.assertTrue(mes, isOnMainThread());
	}
	
	public static void assertMainThread() {
		Assert.assertTrue(isOnMainThread());
	}
	
	public static boolean isOnMainThread() {
		return MAIN_LOOPER == Looper.myLooper();
	}
	
	public static void removeUiCallback(final Runnable callback) {
		MAIN_THREAD_HANDLER.removeCallbacks(callback);
	}
	
	public static void runOnUiThreadAndAwait(final Runnable action) {
		if (isOnMainThread()) {
			action.run();
		} else {
			final CountDownLatch latch = new CountDownLatch(1);
			
			runOnUiThread(() -> {
				action.run();
				
				latch.countDown();
				
			});
			
			try {
				latch.await();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static <T> T getOnUiThreadAndAwait(final Callable<T> action) {
		if (isOnMainThread()) {
			try {
				return action.call();
			} catch (final Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			final CountDownLatch latch = new CountDownLatch(1);
			
			final AtomicReference<T> value = new AtomicReference<>();
			runOnUiThread(() -> {
				try {
					value.set(action.call());
				} catch (final Exception e) {
					e.printStackTrace();
				}
				
				latch.countDown();
				
			});
			
			try {
				latch.await();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			
			return value.get();
		}
	}
	
	public static void postOnUiThread(final Runnable action) {
		final Error callerCause = new Error();
		
		MAIN_THREAD_HANDLER.post(() -> {
			try {
				action.run();
			} catch (final Throwable ex) {
				final Error error = new Error(ex);
				error.setStackTrace(ArrayUtils.concat(ex.getStackTrace(), callerCause.getStackTrace()));
				
				throw error;
			}
		});
	}
	
	public static void runOnUiThread(final Runnable action) {
		if (Looper.myLooper() == MAIN_LOOPER) {
			action.run();
		} else {
			
			//preserve caller stack trace for errors
			final Error callerCause = new Error();
			
			MAIN_THREAD_HANDLER.post(() -> {
				try {
					action.run();
				} catch (final Throwable ex) {
					final Error error = new Error(ex);
					error.setStackTrace(ArrayUtils.concat(ex.getStackTrace(), callerCause.getStackTrace()));
					
					throw error;
				}
			});
		}
	}
	
	public static void runOnWorker(final Runnable runnable) {
		if (isOnMainThread()) {
			EXECUTOR.submit(runnable);
		} else {
			runnable.run();
		}
	}
	
	public static class TaskExecuteReducer {
		
		private final ExecutorService mExecutor;
		private final int mCountTasks;
		private final CountDownLatch[] mLatches;
		
		public TaskExecuteReducer(final ExecutorService executor, final int countTasks) {
			mExecutor = executor;
			mCountTasks = countTasks;
			mLatches = new CountDownLatch[countTasks];
			for (int i = 0; i < mLatches.length; i++) {
				mLatches[i] = new CountDownLatch(1);
			}
		}
		
		public void reduceTask(final Runnable task, final int taskPosition) {
			Assert.assertTrue(taskPosition < mCountTasks);
			Assert.assertTrue(taskPosition >= 0);
			mExecutor.submit(() -> {
				
				try {
					if (taskPosition > 0) {
						try {
							final int prevPosition = taskPosition - 1;
							mLatches[prevPosition].await();
						} catch (final InterruptedException e) {
							e.printStackTrace();
						}
					}
					task.run();
				} finally {
					mLatches[taskPosition].countDown();
				}
			});
		}
		
		public void reduceTaskOnUi(final Runnable task, final int taskPosition) {
			reduceTask(() -> {
				ThreadUtils.runOnUiThreadAndAwait(task);
			}, taskPosition);
		}
	}
}
