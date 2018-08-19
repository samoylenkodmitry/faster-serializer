package ru.ivi.utils;

import java.lang.reflect.InvocationTargetException;

public class ReflectUtils {
	
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
}
