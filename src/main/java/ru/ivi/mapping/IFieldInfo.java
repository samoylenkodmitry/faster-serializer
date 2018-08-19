package ru.ivi.mapping;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface IFieldInfo<Object> {
	String getName();
	
	void read(final Object obj, final JsonParser json, final JsonNode sourceNode) throws IOException;
	
	void read(final Object obj, final Parcel parcel);
	
	void write(final Object obj, final Parcel parcel);
	
	void clone(final Object result, final Object objToClone);
}
