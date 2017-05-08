package android.samutils.fasterserializer.mapping;

import java.io.IOException;

public interface ValueReader extends Iterable<String> {
	boolean contains(final String fieldName);

	boolean isNull(final String fieldName);

	boolean readBoolean(final String fieldName);
	boolean readBoolean(final String fieldName, final boolean defaultValue);

	int readInt(final String fieldName);
	int readInt(final String fieldName, final int defaultValue);

	long readLong(final String fieldName);
	long readLong(final String fieldName, final long defaultValue);

	float readFloat(final String fieldName);
	float readFloat(final String fieldName, final float defaultValue);

	double readDouble(final String fieldName, final double defaultValue);

	String readString(final String fieldName);
	String readString(final String fieldName, final String defaultValue);

	<E extends Enum<E>> E readEnum(final String fieldName, final Class<E> enumType) throws Exception;
	<T> T readObject(final String fieldName, final Class<T> objectType) throws Exception;
	<T> T readObject(final String fieldName) throws Exception;

	int[] readIntArray(final String fieldName) throws IOException;
	String[] readStringArray(final String fieldName) throws IOException;
	<T> T[] readObjectArray(final String fieldName, final Class<T> objectType) throws Exception;
}
