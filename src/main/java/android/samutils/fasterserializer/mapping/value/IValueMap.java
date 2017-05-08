package android.samutils.fasterserializer.mapping.value;

import android.samutils.fasterserializer.mapping.JacksonJsoner;

public interface IValueMap {
	
	JacksonJsoner.ObjectMap<String, JacksonJsoner.IFieldInfo> getObjectMap(final Class<?> classType);
}
