package android.samutils.fasterserializer.mapping;

import android.samutils.utils.ArrayUtils;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Iterator;

public final class JsonableReader extends BaseValueReader<JsonNode> {

	JsonableReader(final JsonNode data) {
		super(data);
	}

	@Override
	public void startRead() { }

	@Override
	public void endRead() { }

	@Override
	public boolean contains(final String fieldName) {
		return mData.has(fieldName);
	}

	@Override
	protected boolean isNullInner(final String fieldName) {
		final JsonNode node = mData.get(fieldName);
		return node == null || node.isNull();
	}

	@Override
	public boolean readBoolean(final String fieldName, final boolean defaultValue) {
		final JsonNode node = mData.get(fieldName);
		return node != null && (node.asBoolean(defaultValue) || node.asInt() == 1);
	}

	@Override
	public int readInt(final String fieldName, final int defaultValue) {
		final JsonNode node = mData.get(fieldName);
		return node == null ? defaultValue : node.asInt(defaultValue);
	}

	@Override
	public long readLong(final String fieldName, final long defaultValue) {
		final JsonNode node = mData.get(fieldName);
		return node == null ? defaultValue : node.asLong(defaultValue);
	}

	@Override
	public float readFloat(final String fieldName, final float defaultValue) {
		final JsonNode node = mData.get(fieldName);
		return node == null ? defaultValue : node.floatValue();
	}

	@Override
	public double readDouble(final String fieldName, final double defaultValue) {
		final JsonNode node = mData.get(fieldName);
		return node == null ? defaultValue : node.asDouble(defaultValue);
	}

	@Override
	public String readString(final String fieldName, final String defaultValue) {
		final JsonNode node = mData.get(fieldName);
		return node == null ? defaultValue : node.asText(defaultValue);
	}

	@Override
	public <E extends Enum<E>> E readEnum(final String fieldName, final Class<E> enumType) throws IOException {
		final JsonNode node = mData.get(fieldName);
		return node == null ? null : JacksonJsoner.readEnum(node.traverse(), enumType);
	}

	@Override
	public <T> T readObject(final String fieldName, final Class<T> objectType) throws IOException {
		final JsonNode node = mData.get(fieldName);
		return node == null ? null : JacksonJsoner.readObject(node.traverse(), node, objectType);
	}

	public <T> T readObject(final Class<T> objectType) throws IOException {
		return JacksonJsoner.readObject(mData.traverse(), mData, objectType);
	}

	@Override
	@Deprecated
	public <T> T readObject(final String fieldName) {
		return null;
	}

	public boolean[] readBooleanArray(final String fieldName) throws IOException {
		final JsonNode node = mData.get(fieldName);
		return node == null ? null : JacksonJsoner.readBooleanArray(node.traverse());
	}

	@Override
	public int[] readIntArray(final String fieldName) throws IOException {
		final JsonNode node = mData.get(fieldName);
		return node == null ? null : JacksonJsoner.readIntArray(node.traverse());
	}

	public long[] readLongArray(final String fieldName) throws IOException {
		final JsonNode node = mData.get(fieldName);
		return node == null ? null : JacksonJsoner.readLongArray(node.traverse());
	}

	public float[] readFloatArray(final String fieldName) throws IOException {
		final JsonNode node = mData.get(fieldName);
		return node == null ? null : JacksonJsoner.readFloatArray(node.traverse());
	}

	public double[] readDoubleArray(final String fieldName) throws IOException {
		final JsonNode node = mData.get(fieldName);
		return node == null ? null : JacksonJsoner.readDoubleArray(node.traverse());
	}

	@Override
	public String[] readStringArray(final String fieldName) throws IOException{
		final JsonNode node = mData.get(fieldName);
		return node == null ? null : ArrayUtils.toArray(JacksonJsoner.readArray(node.traverse(), node, String.class), String.class);
	}

	public <E extends Enum<E>> E[] readEnumArray(final String fieldName, final Class<E> enumType) throws IOException {
		final JsonNode node = mData.get(fieldName);
		return node == null ? null : JacksonJsoner.readEnumArray(node.traverse(), enumType);
	}

	@Override
	public <T> T[] readObjectArray(final String fieldName, final Class<T> objectType) throws IOException {
		final JsonNode node = mData.get(fieldName);
		return node == null ? null : ArrayUtils.toArray(JacksonJsoner.readArray(node.traverse(), node, objectType), objectType);
	}

	@Override
	public Iterator<String> iterator() {
		return mData.fieldNames();
	}
}
