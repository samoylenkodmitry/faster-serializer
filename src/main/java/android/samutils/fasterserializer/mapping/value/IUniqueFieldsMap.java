package android.samutils.fasterserializer.mapping.value;

public interface IUniqueFieldsMap {

	<T> String getUniqueKey(final T object, final Class classType);

	<T> void addUniqueKey(final Class<T> classType, final UniqueKey<T> uniqueKey);
}
