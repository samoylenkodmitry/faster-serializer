package android.samutils.fasterserializer.mapping;


import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SerializableReader extends BaseValueReader<Parcel> {
	private final Map<String, Integer> mFieldPositions = new HashMap<>();
	private int mEndPosition = - 1;

	SerializableReader(final Parcel data) {
		super(data);
	}

	@Override
	public void startRead() {
		mFieldPositions.clear();
		mEndPosition = - 1;

		final int fieldCount = mData.readInt();

		for (int i = 0; i < fieldCount; i++) {
			final String fieldName = mData.readString();
			final int fieldSize = mData.readInt();
			final int fieldPosition = mData.dataPosition();

			mFieldPositions.put(fieldName, fieldPosition);

			mEndPosition = fieldPosition + fieldSize;

			mData.setDataPosition(mEndPosition);
		}
	}

	@Override
	public void endRead() {
		if (mEndPosition >= 0) {
			mData.setDataPosition(mEndPosition);
		}
	}

	@Override
	public boolean contains(final String fieldName) {
		return mFieldPositions.containsKey(fieldName);
	}

	@Override
	public boolean isNullInner(final String fieldName) {
		throw new UnsupportedOperationException();
	}

	private boolean startReadField(final String fieldName) {
		final Integer position = mFieldPositions.get(fieldName);

		if (position != null) {
			mData.setDataPosition(position.intValue());

			return true;
		} else {
			return false;
		}
	}

	private int startReadArrayField(final String fieldName) {
		final Integer position = mFieldPositions.get(fieldName);

		if (position != null) {
			mData.setDataPosition(position.intValue());

			return mData.readInt();
		} else {
			return - 1;
		}
	}

	@Override
	public boolean readBoolean(final String fieldName, final boolean defaultValue) {
		return startReadField(fieldName) ? Serializer.readBoolean(mData) : defaultValue;
	}

	@Override
	public int readInt(final String fieldName, final int defaultValue) {
		return startReadField(fieldName) ? mData.readInt() : defaultValue;
	}

	@Override
	public long readLong(final String fieldName, final long defaultValue) {
		return startReadField(fieldName) ? mData.readLong() : defaultValue;
	}

	@Override
	public float readFloat(final String fieldName, final float defaultValue) {
		return startReadField(fieldName) ? mData.readFloat() : defaultValue;
	}

	@Override
	public double readDouble(final String fieldName, final double defaultValue) {
		return startReadField(fieldName) ? mData.readDouble() : defaultValue;
	}

	@Override
	public String readString(final String fieldName, final String defaultValue) {
		return startReadField(fieldName) ? mData.readString() : defaultValue;
	}

	@Override
	public <E extends Enum<E>> E readEnum(final String fieldName, final Class<E> enumType) {
		// TODO
		return null;
	}

	@Override
	public <T> T readObject(final String fieldName, final Class<T> objectType) {
		return startReadField(fieldName) && !Serializer.readIsNull(mData) ? (T) Serializer.read(mData, objectType) : null;
	}

	public boolean[] readBooleanArray(final String fieldName) {
		final int size = startReadArrayField(fieldName);

		if (size >= 0) {
			final boolean[] result = new boolean[size];

			mData.readBooleanArray(result);

			return result;
		} else {
			return null;
		}
	}

	@Override
	public int[] readIntArray(final String fieldName) {
		final int size = startReadArrayField(fieldName);

		if (size >= 0) {
			final int[] result = new int[size];

			mData.readIntArray(result);

			return result;
		} else {
			return null;
		}
	}

	public long[] readLongArray(final String fieldName) {
		final int size = startReadArrayField(fieldName);

		if (size >= 0) {
			final long[] result = new long[size];

			mData.readLongArray(result);

			return result;
		} else {
			return null;
		}
	}

	public float[] readFloatArray(final String fieldName) {
		final int size = startReadArrayField(fieldName);

		if (size >= 0) {
			final float[] result = new float[size];

			mData.readFloatArray(result);

			return result;
		} else {
			return null;
		}
	}

	public double[] readDoubleArray(final String fieldName) {
		final int size = startReadArrayField(fieldName);

		if (size >= 0) {
			final double[] result = new double[size];

			mData.readDoubleArray(result);

			return result;
		} else {
			return null;
		}
	}

	@Override
	public String[] readStringArray(final String fieldName) {
		final int size = startReadArrayField(fieldName);

		if (size >= 0) {
			final String[] result = new String[size];

			mData.readStringArray(result);

			return result;
		} else {
			return null;
		}
	}

	public <E extends Enum<E>> E[] readEnumArray(final String fieldName, final Class<E> enumType) {
		// TODO
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] readObjectArray(final String fieldName, final Class<T> objectType) {
		final int size = startReadArrayField(fieldName);

		if (size >= 0) {
			final T[] result = (T[]) Array.newInstance(objectType, size);

			for (int i = 0; i < size; i++) {
				result[i] = !Serializer.readIsNull(mData) ? (T) Serializer.read(mData, objectType) : null;
			}

			return result;
		} else {
			return null;
		}
	}

	@Override
	public Iterator<String> iterator() {
		return mFieldPositions.keySet().iterator();
	}
}
