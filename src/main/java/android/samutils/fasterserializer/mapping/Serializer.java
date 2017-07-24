package android.samutils.fasterserializer.mapping;

import android.samutils.fasterserializer.mapping.value.IUniqueFieldsMap;
import android.samutils.fasterserializer.mapping.value.IValueMap;
import android.samutils.fasterserializer.mapping.value.UniqueObject;
import android.samutils.utils.Assert;
import android.text.TextUtils;
import android.util.SparseArray;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class Serializer extends ValueHelper {
	
	@SuppressWarnings("PublicStaticCollectionField")
	public static final Map<Object, Object> UNIQUE_OBJECTS_POOL = new HashMap<>();
	private static final byte FALSE_FLAG = (byte) -21; //unique constants for simpler debug
	private static final byte TRUE_FLAG = (byte) -22;
	private static final byte NULL_FLAG = (byte) -23;
	private static final byte NOT_NULL_FLAG = (byte) -24;
	private static final int NULL_ARR_SIZE = -25;
	private static final Map<String, byte[]> BYTES_POOL = new ConcurrentHashMap<>();
	private static final Map<Class<?>, SparseArray<SparseArray<VersionMigration>>> mVersionMigrationMap = new HashMap<>();
	
	public interface VersionMigration<T> {
		
		void read(final Parcel parcel, final T result) throws Exception;
		
		Class<?> getType();
		
		int getFrom();
		
		int getTo();
	}
	
	private static IUniqueFieldsMap sUniqueFieldsMap = null;
	private static IValueMap sValueMap = null;
	
	public static void setUniqueFieldsMap(final IUniqueFieldsMap uniqueFieldsMap) {
		sUniqueFieldsMap = uniqueFieldsMap;
	}
	
	public static void clearObjectPool() {
		UNIQUE_OBJECTS_POOL.clear();
		BYTES_POOL.clear();
	}
	
	public static void setValueMap(final IValueMap valueMap) {
		sValueMap = valueMap;
	}
	
	public static void registerMigration(final VersionMigration migration) {
		SparseArray<SparseArray<VersionMigration>> typeMigrations = mVersionMigrationMap.get(migration.getType());
		//noinspection Java8MapApi
		if (typeMigrations == null) {
			typeMigrations = new SparseArray<>();
			mVersionMigrationMap.put(migration.getType(), typeMigrations);
		}
		SparseArray<VersionMigration> fromMigrations = typeMigrations.get(migration.getFrom());
		if (fromMigrations == null) {
			fromMigrations = new SparseArray<>();
			typeMigrations.put(migration.getFrom(), fromMigrations);
		}
		fromMigrations.put(migration.getTo(), migration);
	}
	
	public static <T> T[] readArray(final byte[] data, final Class<T> type) {
		if (data != null && data.length > 0) {
			Parcel parcel = null;
			
			try {
				parcel = Parcel.obtain();
				
				try {
					parcel.unmarshall(data, 0, data.length);
					parcel.setDataPosition(0);
				} catch (final Exception e) {
					e.printStackTrace();
					
					return null;
				}
				
				return readArray(parcel, type);
			} finally {
				if (parcel != null) {
					parcel.recycle();
				}
			}
		}
		
		return null;
	}
	
	public static void writeStringArray(final Parcel parcel, final String[] arr) {
		final int size = arr == null ? NULL_ARR_SIZE : arr.length;
		parcel.writeInt(size);
		if (arr != null) {
			parcel.writeStringArray(arr);
		}
	}
	
	public static void writeByteArray(final Parcel parcel, final byte[] arr) {
		final int size = arr == null ? NULL_ARR_SIZE : arr.length;
		parcel.writeInt(size);
		if (arr != null) {
			parcel.writeByteArray(arr);
		}
	}
	
	public static void writeIntArray(final Parcel parcel, final int[] arr) {
		final int size = arr == null ? NULL_ARR_SIZE : arr.length;
		parcel.writeInt(size);
		if (arr != null) {
			parcel.writeIntArray(arr);
		}
	}
	
	public static void writeCharArray(final Parcel parcel, final char[] arr) {
		final int size = arr == null ? NULL_ARR_SIZE : arr.length;
		parcel.writeInt(size);
		if (arr != null) {
			parcel.writeCharArray(arr);
		}
	}
	
	public static void writeCharArray(final Parcel parcel, final long[] arr) {
		final int size = arr == null ? NULL_ARR_SIZE : arr.length;
		parcel.writeInt(size);
		if (arr != null) {
			parcel.writeLongArray(arr);
		}
	}
	
	public static void writeBooleanArray(final Parcel parcel, final boolean[] arr) {
		final int size = arr == null ? NULL_ARR_SIZE : arr.length;
		parcel.writeInt(size);
		if (arr != null) {
			parcel.writeBooleanArray(arr);
		}
	}
	
	public static void writeFloatArray(final Parcel parcel, final float[] arr) {
		final int size = arr == null ? NULL_ARR_SIZE : arr.length;
		parcel.writeInt(size);
		if (arr != null) {
			parcel.writeFloatArray(arr);
		}
	}
	
	public static void writeDoubleArray(final Parcel parcel, final double[] arr) {
		final int size = arr == null ? NULL_ARR_SIZE : arr.length;
		parcel.writeInt(size);
		if (arr != null) {
			parcel.writeDoubleArray(arr);
		}
	}
	
	public static String[] readStringArray(final Parcel parcel) {
		final int size = parcel.readInt();
		if (size == NULL_ARR_SIZE) {
			return null;
		}
		final String[] arr = new String[size];
		parcel.readStringArray(arr);
		return arr;
	}
	
	public static byte[] readByteArray(final Parcel parcel) {
		final int size = parcel.readInt();
		if (size == NULL_ARR_SIZE) {
			return null;
		}
		final byte[] arr = new byte[size];
		parcel.readByteArray(arr);
		return arr;
	}
	
	public static int[] readIntArray(final Parcel parcel) {
		final int size = parcel.readInt();
		if (size == NULL_ARR_SIZE) {
			return null;
		}
		final int[] arr = new int[size];
		parcel.readIntArray(arr);
		return arr;
	}
	
	public static char[] readCharArray(final Parcel parcel) {
		final int size = parcel.readInt();
		if (size == NULL_ARR_SIZE) {
			return null;
		}
		final char[] arr = new char[size];
		parcel.readCharArray(arr);
		return arr;
	}
	
	public static long[] readLongArray(final Parcel parcel) {
		final int size = parcel.readInt();
		if (size == NULL_ARR_SIZE) {
			return null;
		}
		final long[] arr = new long[size];
		parcel.readLongArray(arr);
		return arr;
	}
	
	public static boolean[] readBooleanArray(final Parcel parcel) {
		final int size = parcel.readInt();
		if (size == NULL_ARR_SIZE) {
			return null;
		}
		final boolean[] arr = new boolean[size];
		parcel.readBooleanArray(arr);
		return arr;
	}
	
	public static float[] readFloatArray(final Parcel parcel) {
		final int size = parcel.readInt();
		if (size == NULL_ARR_SIZE) {
			return null;
		}
		final float[] arr = new float[size];
		parcel.readFloatArray(arr);
		return arr;
	}
	
	public static double[] readDoubleArray(final Parcel parcel) {
		final int size = parcel.readInt();
		if (size == NULL_ARR_SIZE) {
			return null;
		}
		final double[] arr = new double[size];
		parcel.readDoubleArray(arr);
		return arr;
	}
	
	@SuppressWarnings("unchecked")
	public static void write(final Parcel parcel, final Object object, final Class<?> inType) {
		final Class<?> cls = inType == Object.class && object != null ? object.getClass() : inType;
		
		final JacksonJsoner.ObjectMap<String, JacksonJsoner.IFieldInfo> objectMap = sValueMap.getObjectMap(cls);
		if (objectMap != null || cls == Object.class) {
			final int startPos = parcel.dataPosition();
			
			Assert.assertTrue(startPos >= 0);
			
			parcel.writeInt(startPos);
			
			if (object != null && objectMap != null) {
				try {
					parcel.writeString(sUniqueFieldsMap == null ? "" : sUniqueFieldsMap.getUniqueKey(object, cls));
					
					if (inType == Object.class) {
						parcel.writeString(cls.getName());
					}
					
					parcel.writeInt(objectMap.getCurrentVersion());
					
					final JacksonJsoner.IFieldInfo[] objFields = objectMap.getSerializerFields();
					
					final int size = objFields.length;
					
					for (int i = 0; i < size; i++) {
						objFields[i].write(object, parcel);
					}
					
					if (object instanceof CustomSerializable) {
						final SerializableWriter writer = new SerializableWriter(parcel);
						
						writer.startWrite();
						
						((CustomSerializable) object).write(writer);
						
						writer.endWrite();
					}
					final int endPos = parcel.dataPosition();
					parcel.setDataPosition(startPos);
					parcel.writeInt(endPos);
					parcel.setDataPosition(endPos);
					Assert.assertTrue(startPos < endPos);
					
				} catch (final Exception e) {
					
					e.printStackTrace();
					
					parcel.setDataPosition(startPos);
					
					String message = e.getMessage();
					if (message == null) {
						message = e.getClass().getName();
					}
					Assert.assertTrue(message + Arrays.toString(e.getStackTrace()), false);
				}
			}
		}
	}
	
	public static <T> T read(final Parcel parcel, Class<T> type) {
		final int startPos = parcel.dataPosition();
		final int endPos = parcel.readInt();
		
		Assert.assertTrue(startPos + " <= " + endPos, startPos <= endPos);
		
		if (startPos >= endPos) {
			return null;
		}
		
		try {
			
			final String uniqueKeyName = parcel.readString();
			
			T result;
			
			if (!TextUtils.isEmpty(uniqueKeyName)) {
				result = (T) UNIQUE_OBJECTS_POOL.get(uniqueKeyName);
				if (result != null) {
					
					final String objUniqueKeyName = sUniqueFieldsMap == null ? "" : sUniqueFieldsMap.getUniqueKey(result, result.getClass());
					
					if (uniqueKeyName.equals(objUniqueKeyName)) {
						
						parcel.setDataPosition(endPos);
						
						return result;
					}
				}
			}
			
			if (type == Object.class) {
				final String className = parcel.readString();
				type = (Class<T>) Class.forName(className);
			}
			
			
			final JacksonJsoner.ObjectMap<String, JacksonJsoner.IFieldInfo> objectMap = sValueMap.getObjectMap(type);
			
			Assert.assertNotNull(type.getName(), objectMap);
			
			final int currentVersion = objectMap.getCurrentVersion();
			final int readVersion = parcel.readInt();
			
			result = objectMap.create(type);
			if (currentVersion != readVersion) {
				final VersionMigration versionMigration = getVersionMigration(type, readVersion, currentVersion);
				if (versionMigration != null) {
					versionMigration.read(parcel, result);
					parcel.setDataPosition(endPos);
					return result;
				}
				throw new VersionChangedException(type + ": " + readVersion + "!=" + currentVersion);
			}
			
			final JacksonJsoner.IFieldInfo[] objFields = objectMap.getSerializerFields();
			
			final int size = objFields.length;
			
			for (int i = 0; i < size; i++) {
				objFields[i].read(result, parcel);
			}
			
			if (result instanceof CustomSerializable) {
				final SerializableReader reader = new SerializableReader(parcel);
				
				reader.startRead();
				
				try {
					((CustomSerializable) result).read(reader);
				} catch (final IOException e) {
					e.printStackTrace();
				}
				
				
				reader.endRead();
			}
			
			if (!TextUtils.isEmpty(uniqueKeyName)) {
				UNIQUE_OBJECTS_POOL.put(uniqueKeyName, result);
			}
			
			Assert.assertEquals(endPos, parcel.dataPosition());
			
			return result;
		} catch (final Exception e) {
			e.printStackTrace();
			
			parcel.setDataPosition(endPos);
			
			String message = e.getMessage();
			if (message == null) {
				message = e.getClass().getName();
			}
			Assert.assertTrue(message + Arrays.toString(e.getStackTrace()), false);
		}
		
		return null;
	}
	
	public static void writeArray(final Parcel parcel, final Object[] objects, final Class<?> type) {
		if (objects == null) {
			parcel.writeInt(NULL_ARR_SIZE);
		} else {
			
			Assert.assertFalse(type.isEnum());
			
			final int length = objects.length;
			
			parcel.writeInt(length);
			
			for (int i = 0; i < length; i++) {
				write(parcel, objects[i], type);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] readArray(final Parcel parcel, final Class<T> type) {
		Assert.assertNotNull(type);
		
		Assert.assertFalse(type.isEnum());
		
		final int count = parcel.readInt();
		if (count == NULL_ARR_SIZE) {
			return null;
		}
		
		final T[] array;
		if (type == Object.class) {
			array = (T[]) new Object[count];
		} else {
			final JacksonJsoner.ObjectMap<String, JacksonJsoner.IFieldInfo> objectMap = sValueMap.getObjectMap(type);
			if (objectMap == null) {
				array = (T[]) Array.newInstance(type, count);
			} else {
				array = objectMap.createArray(count);
			}
		}
		
		for (int i = 0; i < count; i++) {
			array[i] = read(parcel, type);
		}
		
		return array;
	}
	
	public static <E extends Enum<E>> void writeEnum(final Parcel parcel, final E obj) {
		parcel.writeByte(obj == null ? NULL_FLAG : (byte) obj.ordinal());
	}
	
	public static <E extends Enum<E>> E readEnum(final Parcel parcel, final Class<E> type) {
		final byte ordinal = parcel.readByte();
		if (ordinal == NULL_FLAG) {
			return null;
		}
		
		return type.getEnumConstants()[ordinal];
	}
	
	public static <E extends Enum<E>> E[] readEnumArray(final Parcel parcel, final Class<E> type) {
		final int length = parcel.readInt();
		
		@SuppressWarnings("unchecked")        final E[] arr = (E[]) Array.newInstance(type, length == NULL_ARR_SIZE ? 0 : length);
		
		if (length != NULL_ARR_SIZE) {
			final byte[] values = new byte[length];
			parcel.readByteArray(values);
			
			for (int i = 0; i < values.length; i++) {
				arr[i] = type.getEnumConstants()[values[i]];
			}
		}
		
		return arr;
	}
	
	public static <E extends Enum<E>> void writeEnumArray(final Parcel parcel, final E[] obj, final Class<E> type) {
		final int length = obj == null ? NULL_ARR_SIZE : obj.length;
		parcel.writeInt(length);
		if (obj != null) {
			final byte[] values = new byte[length];
			for (int i = 0; i < length; i++) {
				values[i] = (byte) obj[i].ordinal();
			}
			
			parcel.writeByteArray(values);
		}
	}
	
	public static <T> byte[] toBytes(final T object, final Class type) {
		if (object == null) {
			return null;
		}
		String key = null;
		if (object instanceof UniqueObject) {
			key = sUniqueFieldsMap == null ? "" : sUniqueFieldsMap.getUniqueKey(object, type);
			if (!TextUtils.isEmpty(key)) {
				//noinspection unchecked
				final byte[] result = BYTES_POOL.get(key);
				if (result != null) {
					return result;
				}
			}
		}
		
		Parcel parcel = null;
		
		try {
			parcel = Parcel.obtain();
			
			write(parcel, object, type);
			
			final byte[] marshall = parcel.marshall();
			if (!TextUtils.isEmpty(key)) {
				BYTES_POOL.put(key, marshall);
				UNIQUE_OBJECTS_POOL.put(key, object);
			}
			return marshall;
		} finally {
			if (parcel != null) {
				parcel.recycle();
			}
		}
	}
	
	public static <T> byte[] arrayToBytes(final T[] objects, Class<?> type) {
		Parcel parcel = null;
		
		try {
			
			if (objects != null) {
				parcel = Parcel.obtain();
				
				writeArray(parcel, objects, type);
				
				return parcel.marshall();
			} else {
				return null;
			}
		} finally {
			if (parcel != null) {
				parcel.recycle();
			}
		}
	}
	
	public static <T> T read(final byte[] data, final Class<T> type) {
		if (data != null && data.length > 0) {
			Parcel parcel = null;
			
			try {
				parcel = Parcel.obtain();
				
				try {
					parcel.unmarshall(data, 0, data.length);
					parcel.setDataPosition(0);
				} catch (final Exception e) {
					e.printStackTrace();
					
					return null;
				}
				
				return read(parcel, type);
			} finally {
				if (parcel != null) {
					parcel.recycle();
				}
			}
		}
		
		return null;
	}
	
	public static class VersionChangedException extends Error {
		
		public VersionChangedException(final String s) {
			super(s);
		}
	}
	
	public static boolean readBoolean(final Parcel parcel) {
		return parcel.readByte() == TRUE_FLAG;
	}
	
	public static void writeBoolean(final Parcel parcel, final boolean bool) {
		parcel.writeByte(bool ? TRUE_FLAG : FALSE_FLAG);
	}
	
	public static boolean readIsNull(final Parcel parcel) {
		return parcel.readByte() == NULL_FLAG;
	}
	
	public static void writeIsNull(final Parcel parcel, final Object object) {
		parcel.writeByte(object == null ? NULL_FLAG : NOT_NULL_FLAG);
	}
	
	private static <T> VersionMigration getVersionMigration(final Class<? extends T> type, final int readVersion, final int currentVersion) {
		final SparseArray<SparseArray<VersionMigration>> typeMigrations = mVersionMigrationMap.get(type);
		if (typeMigrations != null) {
			final SparseArray<VersionMigration> fromReadMigrations = typeMigrations.get(readVersion);
			if (fromReadMigrations != null) {
				return fromReadMigrations.get(currentVersion);
			}
		}
		return null;
	}
}
