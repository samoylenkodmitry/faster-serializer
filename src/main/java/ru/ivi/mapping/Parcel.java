package ru.ivi.mapping;

import android.support.annotation.NonNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class Parcel {
	
	private static final byte[] BYTES = new byte[0];
	private static final int ONE_BYTE_MASK = 0xff;
	private volatile byte buf[];
	private int pos;
	private DataInputStream mStreamIn = null;
	private DataOutputStream mStreamOut = null;
	
	public Parcel() {
		this(BYTES);
	}
	
	public Parcel(final byte[] bufIn) {
		for (int i = 0, bufInLength = bufIn.length; i < bufInLength; i++) {
			bufIn[i] = 0;
		}
		buf = bufIn;
		pos = 0;
		mStreamOut = new DataOutputStream(new OutputStream() {
			
			@Override
			public void write(final int oneByte) throws IOException {
				ensureCapacity(pos + 1);
				buf[pos] = (byte) oneByte;
				pos += 1;
			}
			
			@Override
			public void write(@NonNull final byte[] bytes, final int off, final int len) throws IOException {
				if ((off < 0) || (off > bytes.length) || (len < 0) ||
					((off + len) - bytes.length > 0)) {
					throw new IndexOutOfBoundsException();
				}
				ensureCapacity(pos + len);
				System.arraycopy(bytes, off, buf, pos, len);
				pos += len;
			}
			
		});
		mStreamIn = new DataInputStream(new InputStream() {
			
			@Override
			public int read() throws IOException {
				return (pos < buf.length) ? (buf[pos++] & ONE_BYTE_MASK) : -1;
			}
			
			@Override
			public int read(@NonNull final byte[] bytes, final int off, final int len) throws IOException {
				int len1 = len;
				if (bytes == null) {
					throw new NullPointerException();
				}
				if (off < 0 || len1 < 0 || len1 > bytes.length - off) {
					throw new IndexOutOfBoundsException();
				}
				
				if (pos >= buf.length) {
					return -1;
				}
				
				final int avail = buf.length - pos;
				if (len1 > avail) {
					len1 = avail;
				}
				if (len1 <= 0) {
					return 0;
				}
				System.arraycopy(buf, pos, bytes, off, len1);
				pos += len1;
				return len1;
			}
			
			@Override
			public long skip(final long count) throws IOException {
				long avail = buf.length - pos;
				if (count < avail) {
					avail = count < 0 ? 0 : count;
				}
				
				pos += avail;
				return avail;
			}
			
			@Override
			public int available() throws IOException {
				return buf.length - pos;
			}
			
			@Override
			public synchronized void mark(final int readlimit) {
				throw new Error();
			}
			
			@Override
			public void reset() throws IOException {
				throw new Error();
			}
		});
	}
	
	public static Parcel obtain() {
		return new Parcel(BYTES);
	}
	
	private void ensureCapacity(final int minCapacity) {
		// overflow-conscious code
		if (minCapacity - buf.length > 0) {
			grow(minCapacity);
		}
	}
	
	private void grow(final int minCapacity) {
		// overflow-conscious code
		final int oldCapacity = buf.length;
		int newCapacity = oldCapacity << 1;
		if (newCapacity - minCapacity < 0) {
			newCapacity = minCapacity;
		}
		if (newCapacity < 0) {
			if (minCapacity < 0) // overflow
			{
				throw new OutOfMemoryError();
			}
			newCapacity = Integer.MAX_VALUE;
		}
		buf = Arrays.copyOf(buf, newCapacity);
	}
	
	public void setDataPosition(final int i) {
		if (i > buf.length) {
			throw new RuntimeException("attempt to set position outside buffer size: pos=" + i + " size=" + buf.length);
		}
		pos = i;
	}
	
	public void unmarshall(final byte[] data, final int offs, final int length) {
		pos = length;
		buf = Arrays.copyOfRange(data, offs, length);
	}
	
	public void recycle() {
		mStreamOut = null;
		mStreamIn = null;
		pos = -1;
		buf = null;
	}
	
	public int dataPosition() {
		return pos;
	}
	
	public void writeInt(final int i) {
		try {
			mStreamOut.writeInt(i);
		} catch (final IOException e) {
			handleEx(e);
		}
	}
	
	public void writeStringArray(final String[] arr) {
		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				writeString(arr[i]);
			}
		}
	}
	
	public void readStringArray(final String[] arr) {
		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				arr[i] = readString();
			}
		}
	}
	
	public void writeByteArray(final byte[] arr) {
		try {
			mStreamOut.write(arr);
		} catch (final IOException e) {
			handleEx(e);
		}
	}
	
	public void readByteArray(final byte[] arr) {
		try {
			if (-1 == mStreamIn.read(arr, 0, arr.length)) {
				throw new Error("-1");
			}
		} catch (final IOException e) {
			handleEx(e);
		}
	}
	
	public void writeIntArray(final int[] arr) {
		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				writeInt(arr[i]);
			}
		}
	}
	
	public void readIntArray(final int[] arr) {
		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				arr[i] = readInt();
			}
		}
	}
	
	public void writeCharArray(final char[] arr) {
		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				writeChar(arr[i]);
			}
		}
	}
	
	public void readCharArray(final char[] arr) {
		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				arr[i] = readChar();
			}
		}
	}
	
	private char readChar() {
		try {
			return mStreamIn.readChar();
		} catch (final IOException e) {
			handleEx(e);
			throw new Error(e);
		}
	}
	
	private void writeChar(final char c) {
		try {
			mStreamOut.writeChar(c);
		} catch (final IOException e) {
			handleEx(e);
		}
	}
	
	public void writeLongArray(final long[] arr) {
		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				writeLong(arr[i]);
			}
		}
	}
	
	public void readLongArray(final long[] arr) {
		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				arr[i] = readLong();
			}
		}
	}
	
	public void writeBooleanArray(final boolean[] arr) {
		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				writeBoolean(arr[i]);
			}
		}
	}
	
	public void readBooleanArray(final boolean[] arr) {
		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				arr[i] = readBoolean();
			}
		}
	}
	
	private boolean readBoolean() {
		try {
			return mStreamIn.readBoolean();
		} catch (final IOException e) {
			handleEx(e);
			throw new Error(e);
		}
	}
	
	private void writeBoolean(final boolean b) {
		try {
			mStreamOut.writeBoolean(b);
		} catch (final IOException e) {
			handleEx(e);
		}
	}
	
	public void writeFloatArray(final float[] arr) {
		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				writeFloat(arr[i]);
			}
		}
	}
	
	public void readFloatArray(final float[] arr) {
		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				arr[i] = readFloat();
			}
		}
	}
	
	public void writeDoubleArray(final double[] arr) {
		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				writeDouble(arr[i]);
			}
		}
	}
	
	public void readDoubleArray(final double[] arr) {
		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				arr[i] = readDouble();
			}
		}
	}
	
	public int readInt() {
		try {
			return mStreamIn.readInt();
		} catch (final IOException e) {
			
			handleEx(e);
			throw new Error(e);
		}
	}
	
	public void writeString(final String str) {
		final int len = str == null ? -1 : str.length();
		try {
			mStreamOut.writeInt(len);
			if (len > 0) {
				final char[] chars = new char[len];
				str.getChars(0, len, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					mStreamOut.writeChar(chars[i]);
				}
			}
		} catch (final IOException e) {
			handleEx(e);
		}
	}
	
	public String readString() {
		try {
			final int len = mStreamIn.readInt();
			if (len == -1) {
				return null;
			}
			if (len == 0) {
				return "";
			}
			
			final char[] chars = new char[len];
			for (int i = 0; i < len; i++) {
				chars[i] = mStreamIn.readChar();
			}
			return new String(chars);
		} catch (final IOException e) {
			handleEx(e);
			throw new Error(e);
		}
	}
	
	public byte[] marshall() {
		return Arrays.copyOf(buf, pos);
	}
	
	public byte readByte() {
		try {
			return mStreamIn.readByte();
		} catch (final IOException e) {
			handleEx(e);
			throw new Error(e);
		}
	}
	
	public void writeByte(final byte b) {
		try {
			mStreamOut.writeByte(b);
		} catch (final IOException e) {
			handleEx(e);
		}
	}
	
	public double readDouble() {
		try {
			return mStreamIn.readDouble();
		} catch (final IOException e) {
			handleEx(e);
			throw new Error(e);
		}
		
	}
	
	public float readFloat() {
		try {
			return mStreamIn.readFloat();
		} catch (final IOException e) {
			handleEx(e);
			throw new Error(e);
		}
	}
	
	public long readLong() {
		try {
			return mStreamIn.readLong();
		} catch (final IOException e) {
			handleEx(e);
			throw new Error(e);
		}
	}
	
	public void writeDouble(final double value) {
		
		try {
			mStreamOut.writeDouble(value);
		} catch (final IOException e) {
			handleEx(e);
		}
	}
	
	public void writeFloat(final float value) {
		
		
		try {
			mStreamOut.writeFloat(value);
		} catch (final IOException e) {
			handleEx(e);
		}
	}
	
	public void writeLong(final long value) {
		
		try {
			mStreamOut.writeLong(value);
		} catch (final IOException e) {
			handleEx(e);
		}
	}
	
	private static void handleEx(final IOException e) {
		e.printStackTrace();
		throw new Error(e);
	}
	
	
}
