package ru.ivi.mapping;

import org.json.JSONException;

import java.io.IOException;


public interface CustomJsonable {
	void read(final JsonableReader reader) throws IOException;
	
	void write(final JsonableWriter writer) throws JSONException;
}
