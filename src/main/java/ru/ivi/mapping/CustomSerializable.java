package ru.ivi.mapping;


import java.io.IOException;

public interface CustomSerializable {
	void read(final SerializableReader reader) throws IOException;
	void write(final SerializableWriter writer);
}
