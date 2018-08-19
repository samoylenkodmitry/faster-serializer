package ru.ivi.mapping;

import android.util.Log;

import java.util.Collection;
import java.util.Map;

import ru.ivi.mapping.value.IValueMap;
import ru.ivi.utils.ArrayUtils;

public final class Copier extends ValueHelper {
	
	private static IValueMap sValueMap = null;
	
	public static void setValueMap(final IValueMap valueMap) {
		sValueMap = valueMap;
	}
	
	public static <T> T cloneObject(final T objectToClone, final Class<T> classType) {
		if (objectToClone == null) {
			return null;
		}
		final ObjectMap<String, IFieldInfo> objectMap = sValueMap.getObjectMap(classType);
		final T result;
		if (objectMap == null) {
			if (BuildConfig.DEBUG) {
				Log.d("Copier", "Object map for class " + classType.toString() + " not found");
			}
			result = copy(objectToClone);
		} else {
			result = objectMap.create(classType);
		}
		
		final Map<String, IFieldInfo> fieldInfoMap = objectMap == null ? null : objectMap.getFields();
		if (fieldInfoMap != null && !fieldInfoMap.isEmpty()) {
			for (final IFieldInfo fieldInfo : fieldInfoMap.values()) {
				//noinspection unchecked
				fieldInfo.clone(result, objectToClone);
			}
		}
		
		if (result instanceof CustomCloneable) {
			//noinspection unchecked
			((CustomCloneable) result).clone(objectToClone);
		}
		
		return result;
	}
	
	public static <T> T[] cloneArray(final T[] arrayToClone, final Class<T> classType) {
		if (arrayToClone == null) {
			return null;
		}
		final Object[] arr = ArrayUtils.newArray(classType, arrayToClone.length);
		final boolean needToClone = !classType.isPrimitive() && !classType.isEnum() && !classType.equals(String.class);
		for (int i = 0, len = arrayToClone.length; i < len; i++) {
			final T elementToClone = arrayToClone[i];
			arr[i] = needToClone ? cloneObject(elementToClone, classType) : elementToClone;
		}
		//noinspection unchecked
		return (T[]) arr;
	}
	
	private static <T> void copy(final T to, final T from) throws IncompatibleTypesException {
		ru.ivi.utils.Assert.assertNotNull(to);
		ru.ivi.utils.Assert.assertNotNull(from);
		
		final Class<?> type = to.getClass();
		
		ru.ivi.utils.Assert.assertNotNull(type);
		
		final Collection<Jsoner.JsonableFieldInfo> fieldInfos = Jsoner.getAllFields(type);
		
		for (final FieldInfo fieldInfo : fieldInfos) {
			try {
				final Class fieldType = fieldInfo.Field.getType();
				if (fieldType.isArray()) {
					final Object[] o = (Object[]) fieldInfo.Field.get(from);
					//noinspection unchecked
					final Object[] o1 = cloneArray(o, fieldType.getComponentType());
					fieldInfo.Field.set(to, o1);
					
				} else {
					fieldInfo.Field.set(to, fieldInfo.Field.get(from));
				}
			} catch (final IllegalAccessException e) {
				throw new RuntimeException("Field " + fieldInfo.Field.getName() + " in class "
					+ to.getClass().getSimpleName() + " is not accessible", e);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T copy(final T from) {
		ru.ivi.utils.Assert.assertNotNull(from);
		
		final T to = create((Class<T>) from.getClass());
		
		copy(to, from);
		
		return to;
	}
}
