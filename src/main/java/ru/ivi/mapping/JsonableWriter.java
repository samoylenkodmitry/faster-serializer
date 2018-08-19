package ru.ivi.mapping;

import org.json.JSONException;
import org.json.JSONObject;

public final class JsonableWriter extends BaseValueWriter<JSONObject> {
	public final boolean AllFields;

	JsonableWriter(final JSONObject data, final boolean allFields) {
		super(data);

		AllFields = allFields;
	}

	@Override
	public void startWrite() { }

	@Override
	public void endWrite() { }

	public void writeBoolean(final String fieldName, final boolean value) throws JSONException {
		mData.put(fieldName, value);
	}

	@Override
	public void writeInt(final String fieldName, final int value) throws JSONException {
		mData.put(fieldName, value);
	}

	public void writeLong(final String fieldName, final long value) throws JSONException {
		mData.put(fieldName, value);
	}

	public void writeFloat(final String fieldName, final float value) throws JSONException {
		mData.put(fieldName, value);
	}

	public void writeDouble(final String fieldName, final double value) throws JSONException {
		mData.put(fieldName, value);
	}

	@Override
	public void writeString(final String fieldName, final String value) throws JSONException {
		mData.put(fieldName, value);
	}

	public <E extends Enum<E>> void writeEnum(final String fieldName, final E value) throws JSONException {
		Jsoner.putEnum(mData, fieldName, value);
	}

	@Override
	public <T> void writeObject(final String fieldName, final T value) throws JSONException {
		mData.put(fieldName, Jsoner.write(value, AllFields));
	}

	public void writeBooleanArray(final String fieldName, final boolean[] valueArray) throws JSONException {
		mData.put(fieldName, Jsoner.toArray(valueArray));
	}

	public void writeIntArray(final String fieldName, final int[] valueArray) throws JSONException {
		mData.put(fieldName, Jsoner.toArray(valueArray));
	}

	public void writeLongArray(final String fieldName, final long[] valueArray) throws JSONException {
		mData.put(fieldName, Jsoner.toArray(valueArray));
	}

	public void writeFloatArray(final String fieldName, final float[] valueArray) throws JSONException {
		mData.put(fieldName, Jsoner.toArray(valueArray));
	}

	public void writeDoubleArray(final String fieldName, final double[] valueArray) throws JSONException {
		mData.put(fieldName, Jsoner.toArray(valueArray));
	}

	@Override
	public void writeStringArray(final String fieldName, final String[] valueArray) throws JSONException {
		mData.put(fieldName, Jsoner.toArray(valueArray));
	}

	public <E extends Enum<E>> void writeEnumArray(final String fieldName, final E[] valueArray) throws JSONException {
		mData.put(fieldName, Jsoner.toArray(valueArray));
	}

	@Override
	public <T> void writeObjectArray(final String fieldName, final T[] valueArray) throws JSONException {
		mData.put(fieldName, Jsoner.toArray(valueArray, AllFields));
	}
}
