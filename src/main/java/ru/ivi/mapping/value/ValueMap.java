package ru.ivi.mapping.value;

import java.util.HashMap;
import java.util.Map;

import ru.ivi.mapping.IFieldInfo;
import ru.ivi.mapping.ObjectMap;
import ru.ivi.utils.ReflectUtils;


public abstract class ValueMap implements IValueMap {
	private final Object mFillLock = new Object();
	@SuppressWarnings("WeakerAccess")
	private volatile Map<Class<?>, ObjectMap<String, IFieldInfo>> mValues = null;
	
	public ValueMap() {
	}
	
	@Override
	public ObjectMap<String, IFieldInfo> getObjectMap(final Class<?> classType) {
		if (classType == null) {
			return null;
		}
		
		if (mValues == null) {
			synchronized (mFillLock) {
				if (mValues == null) {
					final HashMap<Class<?>, ObjectMap<String, IFieldInfo>> valuesMap = new HashMap<>();
					
					fill(valuesMap);
					
					for (final Class<?> key : valuesMap.keySet()) {
						final ObjectMap childClassObjectMap = valuesMap.get(key);
						for (
							Class<?> superClass = key.getSuperclass();
							superClass != null && !Object.class.equals(superClass);
							superClass = superClass.getSuperclass()
							) {
							final ObjectMap superObjectMap = valuesMap.get(superClass);
							if (superObjectMap != null) {
								//noinspection unchecked
								childClassObjectMap.addFields(superObjectMap.getFields());
								childClassObjectMap.addParentVersionHash(superObjectMap.getCurrentVersion());
							}
						}
					}
					
					mValues = valuesMap;
				}
			}
		}
		
		ObjectMap<String, IFieldInfo> objectMap = mValues.get(classType);
		if (objectMap == null) {
			//find model in super classes
			for (Class<?> superClass = classType; superClass != null && superClass != Object.class; superClass = superClass.getSuperclass()) {
				final ObjectMap<String, IFieldInfo> superMap = mValues.get(superClass);
				if (superMap != null) {
					objectMap = new EmptyFieldsClassMap();
					objectMap.addFields(superMap.getFields());
					objectMap.addParentVersionHash(superMap.getCurrentVersion());
					synchronized (mFillLock) {
						mValues.put(classType, objectMap);
					}
					
					break;
				}
			}
		}
		
		return objectMap;
	}
	
	public abstract void fill(final HashMap<Class<?>, ObjectMap<String, IFieldInfo>> mValues);
	
	private static class EmptyFieldsClassMap extends ObjectMap<String, IFieldInfo> {
		
		@Override
		protected void fill(final Map mMap) {
			//nothing
		}
		
		@Override
		public <T> T create(final Class<T> cls) {
			return ReflectUtils.createReflect(cls);
		}
		
		@Override
		public <T> T[] createArray(final int count) {
			//nothing
			return null;
		}
		
		@Override
		public int getCurrentVersion() {
			//nothing
			return 0;
		}
	}
}
