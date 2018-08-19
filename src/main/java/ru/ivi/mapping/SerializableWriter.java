package ru.ivi.mapping;

import android.text.TextUtils;

import java.util.HashSet;
import java.util.Set;

public class SerializableWriter extends BaseValueWriter<Parcel> {
	private final Set<String> mFields = new HashSet<>();
	private int mFieldCountPosition = - 1;
	private int mFieldSizePosition = - 1;
	private int mFieldDataPosition = - 1;

	SerializableWriter(final Parcel data) {
		super(data);
	}

	@Override
	public void startWrite() {
		mFields.clear();
		mFieldCountPosition = mData.dataPosition();

		// for future - field counter
		mData.writeInt(0);
	}

	@Override
	public void endWrite() {
		if (!mFields.isEmpty()) {
			final int endPosition = mData.dataPosition();

			mData.setDataPosition(mFieldCountPosition);

			mData.writeInt(mFields.size());

			mData.setDataPosition(endPosition);
		}
	}

	private void startWriteField(final String fieldName) {
		ru.ivi.utils.Assert.assertFalse(TextUtils.isEmpty(fieldName));
		ru.ivi.utils.Assert.assertFalse(mFields.contains(fieldName));
		ru.ivi.utils.Assert.assertEquals(mFieldSizePosition, - 1);
		ru.ivi.utils.Assert.assertEquals(mFieldDataPosition, - 1);

		mData.writeString(fieldName);

		mFieldSizePosition = mData.dataPosition();

		// for future - field size
		mData.writeInt(0);

		mFieldDataPosition = mData.dataPosition();

		mFields.add(fieldName);
	}

	private void endWriteField() {
		final int endPosition = mData.dataPosition();

		mData.setDataPosition(mFieldSizePosition);

		mData.writeInt(endPosition - mFieldDataPosition);

		mData.setDataPosition(endPosition);

		mFieldSizePosition = - 1;
		mFieldDataPosition = - 1;
	}

	@Override
	public void writeInt(final String fieldName, final int value) {
		startWriteField(fieldName);

		mData.writeInt(value);

		endWriteField();
	}

	public void writeLong(final String fieldName, final long value) {
		startWriteField(fieldName);

		mData.writeLong(value);

		endWriteField();
	}

	public void writeFloat(final String fieldName, final float value) {
		startWriteField(fieldName);

		mData.writeFloat(value);

		endWriteField();
	}

	public void writeDouble(final String fieldName, final double value) {
		startWriteField(fieldName);

		mData.writeDouble(value);

		endWriteField();
	}

	@Override
	public void writeString(final String fieldName, final String value) {
		startWriteField(fieldName);

		mData.writeString(value);

		endWriteField();
	}

	public <E extends Enum<E>> void writeEnum(final String fieldName, final E value) {
		// TODO
	}

	@Override
	public <T> void writeObject(final String fieldName, final T value) {
		startWriteField(fieldName);

		Serializer.writeIsNull(mData, value);

		if (value != null) {
			Serializer.write(mData, value, value.getClass());
		}

		endWriteField();
	}

	public void writeBooleanArray(final String fieldName, final boolean[] valueArray) {
		startWriteField(fieldName);

		if (valueArray != null) {
			mData.writeInt(valueArray.length);
			mData.writeBooleanArray(valueArray);
		} else {
			mData.writeInt(- 1);
		}

		endWriteField();
	}

	public void writeIntArray(final String fieldName, final int[] valueArray) {
		startWriteField(fieldName);

		if (valueArray != null) {
			mData.writeInt(valueArray.length);
			mData.writeIntArray(valueArray);
		} else {
			mData.writeInt(- 1);
		}

		endWriteField();
	}

	public void writeLongArray(final String fieldName, final long[] valueArray) {
		startWriteField(fieldName);

		if (valueArray != null) {
			mData.writeInt(valueArray.length);
			mData.writeLongArray(valueArray);
		} else {
			mData.writeInt(- 1);
		}

		endWriteField();
	}

	@Override
	public void writeStringArray(final String fieldName, final String[] valueArray) {
		startWriteField(fieldName);

		if (valueArray != null) {
			mData.writeInt(valueArray.length);
			mData.writeStringArray(valueArray);
		} else {
			mData.writeInt(- 1);
		}

		endWriteField();
	}

	@Override
	public <T> void writeObjectArray(final String fieldName, final T[] valueArray) {
		startWriteField(fieldName);

		if (valueArray != null) {
			mData.writeInt(valueArray.length);

			for (final T object : valueArray) {
				Serializer.writeIsNull(mData, object);

				if (object != null) {
					Serializer.write(mData, object, object.getClass());
				}
			}
		} else {
			mData.writeInt(- 1);
		}

		endWriteField();
	}
}
