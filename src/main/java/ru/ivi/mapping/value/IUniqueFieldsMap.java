package ru.ivi.mapping.value;

import android.support.annotation.Nullable;

public interface IUniqueFieldsMap {

	@Nullable
	<T> String getUniqueKey(final T object, final Class classType);

	<T> void addUniqueKey(final Class<T> classType, final UniqueKey<T> uniqueKey);
}
