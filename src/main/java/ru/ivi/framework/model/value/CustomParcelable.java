package ru.ivi.framework.model.value;

import android.os.Parcel;
import android.os.Parcelable;

public interface CustomParcelable extends Parcelable {
	void read(final Parcel parcel);
	void write(final Parcel parcel, final int flags);
}
