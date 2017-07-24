package android.samutils.fasterserializer.mapping;

import android.samutils.fasterserializer.processor.Value;
import android.samutils.utils.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class ValueHelper {
	protected static final boolean LOGGING = false;
	
	@SuppressWarnings("serial")
	public static class IncompatibleTypesException extends RuntimeException {
		public IncompatibleTypesException() {
			super();
		}

		public IncompatibleTypesException(final String detailMessage) {
			super(detailMessage);
		}
	}

	@SuppressWarnings("serial")
	public static class UnknownFieldTypeException extends Exception {
		public UnknownFieldTypeException(final Class<?> type) {
			super(!type.isArray() ? type.getName() : type.getComponentType().getName() + "[]");
		}
	}

	public static class FieldInfo {
		public final Field Field;
		private final Map<Class, Boolean> mAnnotationsPresentMap = new IdentityHashMap<>();
		private final Map<Class, ? super Annotation> mAnnotationsMap = new IdentityHashMap<>();

		public FieldInfo(final Field field) {
			Field = field;
		}

		protected static boolean isPrimitive(final Class<?> type) {
			return
				boolean.class == type
				|| int.class == type || long.class == type
				|| float.class == type || double.class == type
				|| String.class == type;
		}
		
		public boolean isAnnotationPresent(final Class<? extends Annotation> aClass) {
			final Boolean cachedResult = mAnnotationsPresentMap.get(aClass);
			if (cachedResult != null) {
				return cachedResult.booleanValue();
			} else {
				final boolean result = Field.isAnnotationPresent(aClass);
				mAnnotationsPresentMap.put(aClass, result);
				return result;
			}
		}

		public <T extends Annotation> T getAnnotation(final Class<T> tClass) {
			if (isAnnotationPresent(tClass)) {
				T result = (T) mAnnotationsMap.get(tClass);
				if (result == null) {
					result = Field.getAnnotation(tClass);
					mAnnotationsMap.put(tClass, result);
				}

				return result;
			} else {
				return null;
			}
		}
	}

	@SuppressWarnings("serial")
	protected abstract static class BaseFieldsCache<F extends FieldInfo> extends ConcurrentHashMap<Class<?>, Collection<F>> {
		public abstract F getFieldInfo(final Class<?> type, final Field field, final Value annotation);
	}

	protected static <F extends FieldInfo> Collection<F> getFields(
		final BaseFieldsCache<F> fieldsCache, final Class<?> type)
	{
		Assert.assertNotNull(type);

		synchronized (fieldsCache) {
			if (!fieldsCache.containsKey(type)) {
				final Field[] fields = type.getFields();

				final ArrayList<F> collectedFieldInfos = new ArrayList<>(fields.length);

				for (final Field field : fields) {
					final int fieldModifiers = field.getModifiers();

					if (!Modifier.isFinal(fieldModifiers)
						&& !Modifier.isStatic(fieldModifiers))
					{
						final Value annotation = field.getAnnotation(Value.class);

						if (annotation != null) {
							final F fieldInfo = fieldsCache.getFieldInfo(type, field, annotation);

							if (fieldInfo != null) {
								collectedFieldInfos.add(fieldInfo);
							}
						}
					}
				}

				collectedFieldInfos.trimToSize();

				fieldsCache.put(type, collectedFieldInfos);

				return collectedFieldInfos;
			} else {
				return fieldsCache.get(type);
			}
		}
	}

	public static <T> T create(final Class<T> type) {
		Assert.assertNotNull(type);

		final long timestamp = System.currentTimeMillis();

		final T result;

		try {
			result = type.getConstructor().newInstance();
		} catch (final IllegalArgumentException e) {
			throw new RuntimeException("Not reachable", e);
		} catch (final InstantiationException e) {
			throw new RuntimeException("Class " + type + " cannot be instantiated", e);
		} catch (final IllegalAccessException e) {
			throw new RuntimeException("Constructor is not visible", e);
		} catch (final InvocationTargetException e) {
			throw new RuntimeException("Exception in empty constructor has throwed", e);
		} catch (final NoSuchMethodException e) {
			throw new RuntimeException("There is no empty constructor in " + type);
		}

//		if (LOGGING) {
//			L.d("Time of creating object: " + (System.currentTimeMillis() - timestamp));
//		}

		return result;
	}

	public static boolean arraysIsEqual(final Object first, final Object second) {
		if (first == second) {
			return true;
		}

		if (Array.getLength(first) != Array.getLength(second)) {
			return false;
		}

		for (int i = 0; i < Array.getLength(first); i++) {
			if (!Array.get(first, i).equals(Array.get(second, i))) {
				return false;
			}
		}

		return true;
	}

	public static boolean fieldsIsEqual(final Object object1, final Object object2, final Field field) {
		try {
			final boolean result;

			final Object value1 = field.get(object1);
			final Object value2 = field.get(object2);

			if (value1 == null && value2 == null) {
				return true;
			}

			if (value1 == null || value2 == null) {
				return false;
			}

			if (field.getType().isArray()) {
				result = arraysIsEqual(value1, value2);
			} else {
				result = value1.equals(value2);
			}

/*			if (!result && LOGGING) {
				L.d("Not equal field: " + field.getName() + " Value one: " + value1 + " Value other: " + value2);
			}*/

			return result;
		} catch (final IllegalArgumentException e) {
			throw new RuntimeException("Not reachable");
		} catch (final IllegalAccessException e) {
			throw new RuntimeException("Not reachable");
		}
	}

}
