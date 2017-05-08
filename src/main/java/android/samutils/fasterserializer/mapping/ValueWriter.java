package android.samutils.fasterserializer.mapping;

public interface ValueWriter {
	void writeInt(final String fieldName, final int value) throws Exception;
	void writeString(final String fieldName, final String value) throws Exception;
	<T> void writeObject(final String fieldName, final T value) throws Exception;

	void writeStringArray(final String fieldName, final String[] valueArray) throws Exception;
	<T> void writeObjectArray(final String fieldName, final T[] valueArray) throws Exception;
}
