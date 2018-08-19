package ru.ivi.mapping.value;

import ru.ivi.mapping.IFieldInfo;
import ru.ivi.mapping.ObjectMap;

public interface IValueMap {
	
	ObjectMap<String, IFieldInfo> getObjectMap(final Class<?> classType);
}
