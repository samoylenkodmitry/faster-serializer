package android.samutils.fasterserializer.mapping;

public abstract class BaseValueWriter<D> implements ValueWriter {
	protected final D mData;

	protected BaseValueWriter(final D data) {
		mData = data;
	}

	public final D getData() {
		return mData;
	}

	public abstract void startWrite();
	public abstract void endWrite();
}
