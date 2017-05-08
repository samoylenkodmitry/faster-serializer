package android.samutils.utils;

import android.os.Bundle;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class ReflectUtils {
	
	public static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE_MAP = new HashMap<>();
	static {
		WRAPPER_TO_PRIMITIVE_MAP.put(Boolean.class, boolean.class);
		WRAPPER_TO_PRIMITIVE_MAP.put(Byte.class, byte.class);
		WRAPPER_TO_PRIMITIVE_MAP.put(Short.class, short.class);
		WRAPPER_TO_PRIMITIVE_MAP.put(Character.class, char.class);
		WRAPPER_TO_PRIMITIVE_MAP.put(Integer.class, int.class);
		WRAPPER_TO_PRIMITIVE_MAP.put(Long.class, long.class);
		WRAPPER_TO_PRIMITIVE_MAP.put(Float.class, float.class);
		WRAPPER_TO_PRIMITIVE_MAP.put(Double.class, double.class);
	}

	public static boolean isParametersMatch(final Class<?>[] parameterTypes, final Class<?>[] parameters) {
		if (parameters == null) {
			return parameterTypes == null;
		} else if (parameterTypes == null || parameters.length != parameterTypes.length) {
			return false;
		} else {
			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i] != parameterTypes[i]) {
					return false;
				}
			}

			return true;
		}
	}

	
	public static Method getMethod(Class<?> clazz, final String methodName, final Class<?>... parameters) throws NoSuchMethodException {
		while (clazz != null) {
			final Method[] methods = clazz.getDeclaredMethods();
			for (final Method method : methods) {
				if (method.getName().equals(methodName) && isParametersMatch(method.getParameterTypes(), parameters)) {
					method.setAccessible(true);

					return method;
				}
			}
			clazz = clazz.getSuperclass();
		}

		throw new NoSuchMethodException();
	}

	
	public static Field getField(Class<?> clazz, final String fieldName) throws NoSuchFieldException {
		while (clazz != null) {
			try {
				final Field field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);

				return field;
			} catch (final NoSuchFieldException ignore) {
			}

			clazz = clazz.getSuperclass();
		}

		throw new NoSuchFieldException();
	}

	
	public static <T> T readField(final Object object, final String fieldName) {
		return readField(object, fieldName, object.getClass());
	}

	
	public static <T> T readStaticField(final Class cls, final String fieldName) {
		return (T) readField(cls, fieldName, cls);
	}

	
	public static <T> T readField(final Object object, final String fieldName, final Class<?> aClass) {
		T result = null;
		try {
			final Field field = getField(aClass, fieldName);
			result = (T) field.get(object);
		} catch (final NoSuchFieldException ex) {
//			L.e(ex);
		} catch (final IllegalAccessException ex) {
//			L.e(ex);
		}

		return result;
	}

	public static void setField(final Object object, final Object value, final String fieldName) {
		setField(object, value, fieldName, object.getClass());
	}

	public static void setField(final Object object, final Object value, final String fieldName, final Class<?> aClass) {
		try {
			final Field field = getField(aClass, fieldName);

			field.set(object, value);
		} catch (final NoSuchFieldException ex) {
//			L.e(ex);
		} catch (final IllegalAccessException ex) {
//			L.e(ex);
		}
	}

	public static Object invoke(final Object object, final String methodName, final Object... params) {
		try {
			final Class[] classes = new Class[params.length];
			for (int i = 0; i < params.length; i++) {
				classes[i] = params[i].getClass();
			}

			final Method method = getMethod(object.getClass(), methodName, classes);

			return method.invoke(object, params);
		} catch (final NoSuchMethodException ex) {
//			L.e(ex);
		} catch (final IllegalAccessException ex) {
//			L.e(ex);
		} catch (final InvocationTargetException ex) {
//			L.e(ex);
		}

		return null;
	}

	public static Object invokePrimitive(final Object object, final String methodName, final Object... params) {
		try {
			final Class[] classes = new Class[params.length];
			for (int i = 0; i < params.length; i++) {
				final Class<?> aClass = params[i].getClass();
				classes[i] = convertWrapperToPrimitive(aClass);
			}

			final Method method = getMethod(object.getClass(), methodName, classes);

			return method.invoke(object, params);
		} catch (final NoSuchMethodException ex) {
//			L.e(ex);
		} catch (final IllegalAccessException ex) {
//			L.e(ex);
		} catch (final InvocationTargetException ex) {
//			L.e(ex);
		}

		return null;
	}

	public static Class convertWrapperToPrimitive(final Class<?> aClass) {
		final Class<?> result = WRAPPER_TO_PRIMITIVE_MAP.get(aClass);
		return result != null ? result : aClass;
	}
	
	public static <T> T createReflect(final Class<T> type) {
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
		
		return result;
	}
	
	@SafeVarargs
	public static <T> Bundle packClasses(final String key, final Class<? extends T>... classes) {
		if (!TextUtils.isEmpty(key) && !ArrayUtils.isEmpty(classes)) {
			final ArrayList<String> classNames = new ArrayList<>(classes.length);

			for (final Class<? extends T> clazz : classes) {
				if (clazz != null) {
					classNames.add(clazz.getName());
				}
			}

			if (!classNames.isEmpty()) {
				final Bundle bundle = new Bundle();

				bundle.putStringArrayList(key, classNames);

				return bundle;
			}
		}

		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T>[] unpackClasses(final Bundle bundle, final String key) {
		if (bundle != null && !TextUtils.isEmpty(key)) {
			final Collection<String> classNames = bundle.getStringArrayList(key);

			if (classNames != null && !classNames.isEmpty()) {
				final Collection<Class<? extends T>> classSet = new HashSet<>(classNames.size());

				for (final String className : classNames) {
					if (!TextUtils.isEmpty(className)) {
						final Class<? extends T> clazz;

						try {
							clazz = (Class<? extends T>) Class.forName(className);
						} catch (final ClassNotFoundException e) {

							continue;
						}

						classSet.add(clazz);
					}
				}

				if (!classSet.isEmpty()) {
					//noinspection SuspiciousToArrayCall
					return (Class<? extends T>[]) classSet.toArray(new Class<?>[classSet.size()]);
				}
			}
		}

		return null;
	}
}
