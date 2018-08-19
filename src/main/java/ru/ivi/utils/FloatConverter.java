package ru.ivi.utils;

import android.support.annotation.NonNull;

public interface FloatConverter<T> {
	
	float convert(@NonNull T item);
}
