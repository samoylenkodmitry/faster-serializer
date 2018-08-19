package ru.ivi.utils;

import android.support.annotation.NonNull;

public interface IntConverter<T> {
	
	int convert(@NonNull T item);
}
