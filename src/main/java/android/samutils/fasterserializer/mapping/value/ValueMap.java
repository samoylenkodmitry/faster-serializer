package android.samutils.fasterserializer.mapping.value;

import android.samutils.fasterserializer.mapping.JacksonJsoner;
import android.samutils.utils.ReflectUtils;

import java.util.HashMap;
import java.util.Map;


public abstract class ValueMap implements IValueMap {
	private final Object mFillLock = new Object();
	@SuppressWarnings("WeakerAccess")
	protected volatile Map<Class<?>, JacksonJsoner.ObjectMap<String, JacksonJsoner.IFieldInfo>> mValues = null;
	
	public ValueMap() {
	}
	
	@Override
	public JacksonJsoner.ObjectMap<String, JacksonJsoner.IFieldInfo> getObjectMap(final Class<?> classType) {
		if (classType == null) {
			return null;
		}
		
		if (mValues == null) {
			synchronized (mFillLock) {
				if (mValues == null) {
					mValues = new HashMap<>();
					
					fill();
					collectTypeHierarchy();
				}
			}
		}
		
		JacksonJsoner.ObjectMap<String, JacksonJsoner.IFieldInfo> objectMap = mValues.get(classType);
		if (objectMap == null) {
			//find model in super classes
			for (Class<?> superClass = classType; superClass != Object.class; superClass = superClass.getSuperclass()) {
				final JacksonJsoner.ObjectMap<String, JacksonJsoner.IFieldInfo> superMap = mValues.get(superClass);
				if (superMap != null) {
					objectMap = new EmptyFieldsClassMap();
					objectMap.getFields().putAll(superMap.getFields());
					synchronized (mFillLock) {
						mValues.put(classType, objectMap);
					}
					
					break;
				}
			}
		}
		
		return objectMap;
	}
	
	private void collectTypeHierarchy() {
		for (final Class<?> key : mValues.keySet()) {
			final JacksonJsoner.ObjectMap childClassObjectMap = mValues.get(key);
			for (
				Class<?> superClass = key.getSuperclass();
				superClass != null && !Object.class.equals(superClass);
				superClass = superClass.getSuperclass()
				) {
				final JacksonJsoner.ObjectMap superObjectMap = mValues.get(superClass);
				if (superObjectMap != null) {
					//noinspection unchecked
					childClassObjectMap.getFields().putAll(superObjectMap.getFields());
				}
			}
		}
	}
	
	public abstract void fill();
	
	private static class EmptyFieldsClassMap extends JacksonJsoner.ObjectMap<String, JacksonJsoner.IFieldInfo> {
		
		@Override
		protected void fill() {
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
