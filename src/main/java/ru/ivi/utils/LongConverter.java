package ru.ivi.utils;

import android.support.annotation.NonNull;

public interface LongConverter<T> {
	
	long convert(@NonNull T item);
}
