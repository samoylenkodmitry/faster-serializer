package ru.ivi.mapping;

public abstract class BaseValueReader<D> implements ValueReader {
	protected final D mData;

	protected BaseValueReader(final D data) {
		mData = data;
	}

	public final D getData() {
		return mData;
	}

	public abstract void startRead();
	public abstract void endRead();

	protected abstract boolean isNullInner(final String fieldName);

	@Override
	public final boolean isNull(final String fieldName) {
		return !contains(fieldName) || isNullInner(fieldName);
	}

	@Override
	public final boolean readBoolean(final String fieldName) {
		return readBoolean(fieldName, false);
	}

	@Override
	public final int readInt(final String fieldName) {
		return readInt(fieldName, 0);
	}

	@Override
	public final long readLong(final String fieldName) {
		return readLong(fieldName, 0L);
	}

	@Override
	public final float readFloat(final String fieldName) {
		return readFloat(fieldName, 0.0F);
	}

	public final double readDouble(final String fieldName) {
		return readDouble(fieldName, 0.0);
	}

	@Override
	public final String readString(final String fieldName) {
		return readString(fieldName, null);
	}
}
