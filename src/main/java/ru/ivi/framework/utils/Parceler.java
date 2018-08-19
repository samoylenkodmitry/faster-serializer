package ru.ivi.framework.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ru.ivi.framework.model.value.CustomParcelable;

import ru.ivi.mapping.ValueHelper;
import ru.ivi.processor.Value;

public final class Parceler extends ValueHelper {
	private static class ParcelableFieldInfo extends FieldInfo {
		public ParcelableFieldInfo(final Field field) {
			super(field);
		}

		public void readFrom(
			final Parcel parcel, final Parcelable object
			) throws IllegalAccessException, UnknownFieldTypeException
		{
			final Class<?> type = Field.getType();

			if (boolean.class == type) {
				Field.set(object, readBoolean(parcel));
			} else if (int.class == type) {
				Field.set(object, parcel.readInt());
			} else if (long.class == type) {
				Field.set(object, parcel.readLong());
			} else if (float.class == type) {
				Field.set(object, parcel.readFloat());
			} else if (double.class == type) {
				Field.set(object, parcel.readDouble());
			} else if (String.class == type) {
				Field.set(object, parcel.readString());
			} else if (Parcelable.class.isAssignableFrom(type)) {
				Field.set(object, parcel.readParcelable(type.getClassLoader()));
			} else if (type.isArray()) {
				final int size = parcel.readInt();

				if (size >= 0) {
					final Object array;

					final Class<?> componentType = type.getComponentType();

					if (boolean.class == componentType) {
						array = parcel.createBooleanArray();
					} else if (int.class == componentType) {
						array = parcel.createIntArray();
					} else if (long.class == componentType) {
						array = parcel.createLongArray();
					} else if (float.class == componentType) {
						array = parcel.createFloatArray();
					} else if (double.class == componentType) {
						array = parcel.createDoubleArray();
					} else if (String.class == componentType) {
						array = parcel.createStringArray();
					} else if (Parcelable.class.isAssignableFrom(componentType)) {
						final Parcelable[] parcelableArray = parcel.readParcelableArray(componentType.getClassLoader());

						array = Array.newInstance(componentType, parcelableArray.length);

						for (int i = 0; i < parcelableArray.length; i++) {
							Array.set(array, i, parcelableArray[i]);
						}
					} else {
						throw new UnknownFieldTypeException(type);
					}

					Field.set(object, array);
				} else {
					Field.set(object, null);
				}
			} else {
				throw new UnknownFieldTypeException(type);
			}
		}

		public void writeTo(
			final Parcel parcel, final int flags, final Parcelable object
			) throws IllegalAccessException, UnknownFieldTypeException
		{
			final Class<?> type = Field.getType();

			if (boolean.class == type) {
				writeBoolean(parcel, Field.getBoolean(object));
			} else if (int.class == type) {
				parcel.writeInt(Field.getInt(object));
			} else if (long.class == type) {
				parcel.writeLong(Field.getLong(object));
			} else if (float.class == type) {
				parcel.writeFloat(Field.getFloat(object));
			} else if (double.class == type) {
				parcel.writeDouble(Field.getDouble(object));
			} else if (String.class == type) {
				parcel.writeString((String) Field.get(object));
			} else if (Parcelable.class.isAssignableFrom(type)) {
				parcel.writeParcelable((Parcelable) Field.get(object), flags);
			} else if (type.isArray()) {
				final Class<?> componentType = type.getComponentType();
				final Object array = Field.get(object);

				if (array != null) {
					final int size = Array.getLength(array);

					parcel.writeInt(size);

					if (boolean.class == componentType) {
						parcel.writeBooleanArray((boolean[]) array);
					} else if (int.class == componentType) {
						parcel.writeIntArray((int[]) array);
					} else if (long.class == componentType) {
						parcel.writeLongArray((long[]) array);
					} else if (float.class == componentType) {
						parcel.writeFloatArray((float[]) array);
					} else if (double.class == componentType) {
						parcel.writeDoubleArray((double[]) array);
					} else if (String.class == componentType) {
						parcel.writeStringArray((String[]) array);
					} else if (Parcelable.class.isAssignableFrom(componentType)) {
						parcel.writeParcelableArray((Parcelable[]) array, flags);
					} else {
						throw new UnknownFieldTypeException(type);
					}
				} else {
					parcel.writeInt(- 1);
				}
			} else {
				throw new UnknownFieldTypeException(type);
			}
		}
	}

	@SuppressWarnings("serial")
	private static final BaseFieldsCache<ParcelableFieldInfo> PARCELABLE_FIELDS_CACHE = new BaseFieldsCache<ParcelableFieldInfo>() {
		@Override
		public ParcelableFieldInfo getFieldInfo(final Class<?> type, final Field field, final Value annotation) {
			return new ParcelableFieldInfo(field);
		}
	};

	public static <T extends Parcelable> Collection<ParcelableFieldInfo> getFields(final Class<T> type) {
		ru.ivi.utils.Assert.assertNotNull(type);

		return getFields(PARCELABLE_FIELDS_CACHE, type);
	}

	public static <T extends Parcelable> T readFrom(final Parcel parcel, final Class<T> type) {
		final T result = create(type);

		readFrom(parcel, result);

		return result;
	}

	public static void readFrom(final Parcel parcel, final Parcelable object) {
		ru.ivi.utils.Assert.assertNotNull(parcel);
		ru.ivi.utils.Assert.assertNotNull(object);

		final Class<? extends Parcelable> type = object.getClass();

		final long timestamp = System.currentTimeMillis();

		final Collection<ParcelableFieldInfo> fieldInfos = getFields(PARCELABLE_FIELDS_CACHE, type);

		for (final ParcelableFieldInfo fieldInfo : fieldInfos) {
			try {
				fieldInfo.readFrom(parcel, object);
			} catch (final IllegalArgumentException e) {
				throw new RuntimeException(
					"Wrong type of setting parameter. Type of field " + fieldInfo.Field.getName()
					+ " in class " + type.getSimpleName()
					+ " is " + fieldInfo.Field.getType().getSimpleName(), e);
			} catch (final IllegalAccessException e) {
				throw new RuntimeException("Field " + fieldInfo.Field.getName() + " is not accessible", e);
			} catch (final UnknownFieldTypeException e) {
				throw new RuntimeException("Unknown type " + fieldInfo.Field.getType()
				+ " of field " + fieldInfo.Field.getName()
				+ " in class " + type.getSimpleName());
			}
		}

		if (object instanceof CustomParcelable) {
			((CustomParcelable) object).read(parcel);
		}

	}

	public static void writeTo(final Parcel parcel, final int flags, final Parcelable object) {
		ru.ivi.utils.Assert.assertNotNull(parcel);
		ru.ivi.utils.Assert.assertNotNull(object);

		final Class<? extends Parcelable> type = object.getClass();

		final Collection<ParcelableFieldInfo> fieldInfos = getFields(type);

		for (final ParcelableFieldInfo fieldInfo : fieldInfos) {
			try {
				fieldInfo.writeTo(parcel, flags, object);
			} catch (final IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (final IllegalAccessException e) {
				throw new RuntimeException("Field " + fieldInfo.Field.getName()
				+ " in class " + type.getSimpleName()
				+ " is not accessible", e);
			} catch (final UnknownFieldTypeException e) {
				throw new RuntimeException("Unknown type " + fieldInfo.Field.getType()
				+ " of field " + fieldInfo.Field.getName()
				+ " in class " + type.getSimpleName());
			}
		}

		if (object instanceof CustomParcelable) {
			((CustomParcelable) object).write(parcel, flags);
		}
	}

	public static <T extends Parcelable> Creator<T> makeCreator(final Class<T> type) {
		@SuppressWarnings("unchecked")
		final Creator<T> creator = new Creator<T>() {
			@Override
			public T createFromParcel(final Parcel parcel) {
				ru.ivi.utils.Assert.assertNotNull(parcel);

				return readFrom(parcel, type);
			}

			@Override
			public T[] newArray(final int size) {
				ru.ivi.utils.Assert.assertTrue(size >= 0);

				return (T[]) Array.newInstance(type, size);
			}
		};

		return creator;
	}

	public static void writeBoolean(final Parcel parcel, final boolean value) {
		ru.ivi.utils.Assert.assertNotNull(parcel);

		parcel.writeByte((byte) (value ? 1 : 0));
	}

	public static boolean readBoolean(final Parcel parcel) {
		ru.ivi.utils.Assert.assertNotNull(parcel);

		return parcel.readByte() == 1;
	}

	public static Map<String, Integer> readIntMap(final Parcel parcel) {
		ru.ivi.utils.Assert.assertNotNull(parcel);

		final int size = parcel.readInt();

		if (size >= 0) {
			final Map<String, Integer> map = new HashMap<String, Integer>(size);

			for (int i = 0; i < size; i++) {
				map.put(parcel.readString(), parcel.readInt());
			}

			return map;
		} else {
			return null;
		}
	}

	public static void writeIntMap(final Parcel parcel, final Map<String, Integer> map) {
		ru.ivi.utils.Assert.assertNotNull(parcel);

		if (map != null) {
			parcel.writeInt(map.size());

			for (final Entry<String, Integer> entry : map.entrySet()) {
				parcel.writeString(entry.getKey());
				parcel.writeInt(entry.getValue());
			}
		} else {
			parcel.writeInt(- 1);
		}
	}
}
