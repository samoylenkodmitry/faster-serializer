package ru.ivi.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ObjectMap<Key, Value> {
	
	private volatile Map<Key, Value> mMap = null;
	private volatile IFieldInfo[] mFieldsArray = null;
	private final Object mFillLock = new Object();
	private final Object mSerializerFillLock = new Object();
	private int mParentClassVersionsSum = 0;
	
	protected abstract void fill(final Map mMap);
	
	public abstract <T> T create(Class<T> cls);
	
	public abstract <T> T[] createArray(final int count);
	
	public abstract int getCurrentVersion();
	
	public int getCurrentVersionSum() {
		return getCurrentVersion() + mParentClassVersionsSum;
	}
	
	public final Map<Key, Value> getFields() {
		if (mMap == null) {
			synchronized (mFillLock) {
				if (mMap == null) {
					final Map<Key, Value> objectMap = new ConcurrentHashMap<>();
					fill(objectMap);
					mMap = objectMap;
				}
			}
		}
		return mMap;
	}
	
	public final IFieldInfo[] getSerializerFields() {
		if (mFieldsArray == null) {
			synchronized (mSerializerFillLock) {
				if (mFieldsArray == null) {
					final List<Map.Entry<Key, Value>> entriesList = new ArrayList<>(getFields().entrySet());
					
					Collections.sort(entriesList, ((final Map.Entry<Key, Value> o1, final Map.Entry<Key, Value> o2) ->
						((String) o1.getKey()).compareTo((String) o2.getKey())));
					
					final IFieldInfo[] fieldsArray = new IFieldInfo[entriesList.size()];
					
					for (int i = 0; i < entriesList.size(); i++) {
						final Map.Entry<Key, Value> keyValueEntry = entriesList.get(i);
						fieldsArray[i] = (IFieldInfo) keyValueEntry.getValue();
					}
					
					mFieldsArray = fieldsArray;
				}
			}
		}
		return mFieldsArray;
	}
	
	public void addFields(final Map<Key, Value> fields) {
		synchronized (mFillLock) {
			getFields().putAll(fields);
		}
	}
	
	public void addParentVersionHash(final int versionHash){
		synchronized (mFillLock) {
			mParentClassVersionsSum += versionHash;
		}
	}
}
