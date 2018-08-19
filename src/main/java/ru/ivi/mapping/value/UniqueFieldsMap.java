package ru.ivi.mapping.value;


import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class UniqueFieldsMap implements IUniqueFieldsMap {
	
	private static final String KEY_EMPTY = "";
	private final Map<Class<?>, List<UniqueKey>> KEY_CACHE = new ConcurrentHashMap<>();
	private volatile Map<Class<?>, UniqueKey> mUniqueFields;
	
	@Nullable
	@Override
	public <T> String getUniqueKey(final T object, final Class classType) {
		if (mUniqueFields == null) {
			mUniqueFields = new HashMap<>();
			fill();
		}
		List<UniqueKey> keys = KEY_CACHE.get(classType);
		if (keys == null) {
			keys = new ArrayList<>();
			KEY_CACHE.put(classType, keys);
			if (UniqueObject.class.isAssignableFrom(classType)) {
				final UniqueKey uniqueKey = mUniqueFields.get(classType);
				if (uniqueKey != null) {
					keys.add(uniqueKey);
				}
				
				for (Class cls = classType.getSuperclass();
					 cls != null && !Object.class.equals(cls);
					 cls = cls.getSuperclass()) {
					
					final UniqueKey superClassUniqueKey = mUniqueFields.get(cls);
					if (superClassUniqueKey != null) {
						keys.add(superClassUniqueKey);
					}
				}
			}
		}
		if (!keys.isEmpty()) {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < keys.size(); i++) {
				sb.append(keys.get(i).getUniqueKey(object));
			}
			return sb.toString().intern();
		}
		return KEY_EMPTY;
	}
	
	@Override
	public <T> void addUniqueKey(final Class<T> classType, final UniqueKey<T> uniqueKey) {
		mUniqueFields.put(classType, uniqueKey);
	}
	
	protected abstract void fill();
}
