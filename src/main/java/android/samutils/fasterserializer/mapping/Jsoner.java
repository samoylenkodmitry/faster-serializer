package android.samutils.fasterserializer.mapping;

import android.samutils.fasterserializer.mapping.value.TokenizedEnum;
import android.samutils.fasterserializer.mapping.value.UniqueObject;
import android.samutils.fasterserializer.mapping.value.UniqueObjectField;
import android.samutils.fasterserializer.processor.Value;
import android.samutils.utils.ArrayUtils;
import android.samutils.utils.Assert;
import android.samutils.utils.DateUtils;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public final class Jsoner extends ValueHelper {

	private static final String TAG = Jsoner.class.getSimpleName();

	private static final String KEY_CLASS_NAME = "__class__";
	private static final String ENUM_VALUE_UNKNOWN = "";

	private static final Map<Class, Collection<JsonableFieldInfo>> UNIQUE_OBJECTS_UNIQUE_FIELDS = new IdentityHashMap<>();
	private static final Comparator<Object> OBJECT_COMPARATOR = new Comparator<Object>() {
		
		@Override
		public int compare(final Object lhs, final Object rhs) {
			if (lhs == rhs) {
				return 0;
			}
			if (lhs == null) {
				if (rhs == null) {
					return 0;
				} else {
					return 1;
				}
			}
			if (rhs == null) {
				return -1;
			}
			
			final Class lhsClass = lhs.getClass();
			final Class rhsClass = rhs.getClass();
			
			if (lhsClass != rhsClass) {
				return lhsClass.getName().compareTo(rhsClass.getName());
			}
			if (lhsClass == Integer.class) {
				return ((Integer) lhs).compareTo((Integer) rhs);
			}
			if (lhsClass == Long.class) {
				return ((Long) lhs).compareTo((Long) rhs);
			}
			if (lhsClass == Byte.class) {
				return ((Byte) lhs).compareTo((Byte) rhs);
			}
			if (lhsClass == String.class) {
				return ((String) lhs).compareTo((String) rhs);
			}
			if (lhsClass == Boolean.class) {
				return ((Boolean) lhs).compareTo((Boolean) rhs);
			}
			
			return 0;
		}
	};
	
	public static <T> String getUniqueObjectKey(final Class<T> type, final List<Object> keys) {
		if (keys == null || keys.size() == 0) {
			return null;
		}
		
		Collections.sort(keys, OBJECT_COMPARATOR);
		
		final StringBuilder stringBuilder = new StringBuilder();
		for (final Object keyObj : keys) {
			stringBuilder.append(keyObj).append('_');
		}
		stringBuilder.append(type);
		
		return stringBuilder.toString();
	}
	
	
	public static final class JsonableFieldInfo extends FieldInfo {
		public final String Key;

		public JsonableFieldInfo(final Field field, final String key) {
			super(field);

			Key = key;
		}

		@Override
		public String toString() {
			return super.toString() + " " + Key;
		}

		public boolean read(
			final JSONObject json, final Object object, final boolean allFields
		) throws IllegalAccessException, UnknownFieldTypeException, JSONException
		{
			if (!json.has(Key) || json.isNull(Key)) {
				return false;
			}

			final Class<?> type = Field.getType();

			if (boolean.class == type) {
				final Object jsonValue = json.get(Key);
				boolean value = false;

				if (jsonValue instanceof Boolean) {
					value = (boolean) jsonValue;
				} else if (jsonValue instanceof Number) {
					value = ((Number) jsonValue).intValue() != 0;
				}

				Field.set(object, value);
			} else if (int.class == type) {
				Field.set(object, json.getInt(Key));
			} else if (long.class == type) {
				Field.set(object, json.getLong(Key));
			} else if (float.class == type) {
				Field.set(object, (float) json.getDouble(Key));
			} else if (double.class == type) {
				Field.set(object, json.getDouble(Key));
			} else if (String.class == type) {
				Field.set(object, json.getString(Key));
			} else if (JSONObject.class == type) {
				Field.set(object, new JSONObject(json.getString(Key)));
			} else if (JSONArray.class == type) {
				Field.set(object, new JSONArray(json.getString(Key)));
			} else if (type.isEnum()) {
				Field.set(object, getEnum(json.getString(Key), (Class<? extends Enum>) type));
			} else if (type.isArray()) {
				if (json.has(Key)) {
					final Class<?> componentType = type.getComponentType();

					final JSONArray jsonArray = json.optJSONArray(Key);

					final Object array;

					if (jsonArray == null) {
						array = Array.newInstance(componentType, 1);

						if (FieldInfo.isPrimitive(componentType)) {
							if (!json.isNull(Key)) {
								Array.set(array, 0, json.get(Key));
							}
						} else if (componentType.isEnum()) {
							if (!json.isNull(Key)) {
								Array.set(
									array, 0,
									getEnum(json.getString(Key), (Class<? extends Enum>) componentType));
							}
						} else {
							if (!json.isNull(Key)) {
								final JSONObject jsonObject = json.optJSONObject(Key);

								if (jsonObject != null) {
									Array.set(
										array, 0,
										allFields ? Jsoner.read(jsonObject) : Jsoner.read(jsonObject, componentType));
								} else {
									final String jsonObjectStr = json.optString(Key);

									Array.set(
										array, 0,
										allFields
											? Jsoner.read(jsonObjectStr)
											: Jsoner.read(jsonObjectStr, componentType));
								}
							}
						}
					} else {
						final int size = jsonArray.length();

						array = Array.newInstance(componentType, size);

						if (FieldInfo.isPrimitive(componentType)) {
							for (int i = 0; i < size; i++) {
								if (!jsonArray.isNull(i)) {
									Array.set(array, i, jsonArray.get(i));
								}
							}
						} else if (componentType.isEnum()) {
							for (int i = 0; i < size; i++) {
								if (!jsonArray.isNull(i)) {
									Array.set(
										array, i,
										getEnum(jsonArray.getString(i), (Class<? extends Enum>) componentType));
								}
							}
						} else {
							for (int i = 0; i < size; i++) {
								if (!jsonArray.isNull(i)) {
									Array.set(
										array, i,
										allFields
											? Jsoner.read(jsonArray.getJSONObject(i))
											: Jsoner.read(jsonArray.getJSONObject(i), componentType));
								}
							}
						}
					}

					Field.set(object, array);
				} else {
					return false;
				}
			} else {
				if (allFields) {
					Field.set(object, Jsoner.read(json.getJSONObject(Key)));
				} else {
					Field.set(object, Jsoner.read(json.getJSONObject(Key), type));
				}
			}

			return true;
		}

		public Object read(
			final JSONObject json) throws IllegalAccessException, UnknownFieldTypeException, JSONException
		{
			if (!json.has(Key) || json.isNull(Key)) {
				return null;
			}

			final Class<?> type = Field.getType();

			if (boolean.class == type) {
				return json.getBoolean(Key);
			} else if (int.class == type) {
				return json.getInt(Key);
			} else if (long.class == type) {
				return json.getLong(Key);
			} else if (float.class == type) {
				return (float) json.getDouble(Key);
			} else if (double.class == type) {
				return json.getDouble(Key);
			} else if (String.class == type) {
				return json.getString(Key);
			} else if (JSONObject.class == type) {
				return new JSONObject(json.getString(Key));
			} else if (JSONArray.class == type) {
				return new JSONArray(json.getString(Key));
			} else if (type.isEnum()) {
				return getEnum(json.getString(Key), (Class<? extends Enum>) type);
			} else if (type.isArray()) {
				if (json.has(Key)) {
					final Class<?> componentType = type.getComponentType();

					final JSONArray jsonArray = json.optJSONArray(Key);

					final Object array;

					if (jsonArray == null) {
						array = Array.newInstance(componentType, 1);

						if (FieldInfo.isPrimitive(componentType)) {
							if (!json.isNull(Key)) {
								Array.set(array, 0, json.get(Key));
							}
						} else if (componentType.isEnum()) {
							if (!json.isNull(Key)) {
								Array.set(
									array, 0,
									getEnum(json.getString(Key), (Class<? extends Enum>) componentType));
							}
						} else {
							if (!json.isNull(Key)) {
								final JSONObject jsonObject = json.optJSONObject(Key);

								if (jsonObject != null) {
									Array.set(
										array, 0,
										Jsoner.read(jsonObject));
								} else {
									final String jsonObjectStr = json.optString(Key);

									Array.set(
										array, 0,
											 Jsoner.read(jsonObjectStr)
											);
								}
							}
						}
					} else {
						final int size = jsonArray.length();

						array = Array.newInstance(componentType, size);

						if (FieldInfo.isPrimitive(componentType)) {
							for (int i = 0; i < size; i++) {
								if (!jsonArray.isNull(i)) {
									Array.set(array, i, jsonArray.get(i));
								}
							}
						} else if (componentType.isEnum()) {
							for (int i = 0; i < size; i++) {
								if (!jsonArray.isNull(i)) {
									Array.set(
										array, i,
										getEnum(jsonArray.getString(i), (Class<? extends Enum>) componentType));
								}
							}
						} else {
							for (int i = 0; i < size; i++) {
								if (!jsonArray.isNull(i)) {
									Array.set(
										array, i,
											 Jsoner.read(jsonArray.getJSONObject(i))
											);
								}
							}
						}
					}

					return array;
				} else {
					return null;
				}
			} else {
				return Jsoner.read(json.getJSONObject(Key));
			}
		}

		private void write(
			final JSONObject json, final Object object, final boolean allFields
		) throws IllegalAccessException, UnknownFieldTypeException, JSONException
		{
			final Class<?> type = Field.getType();

			if (boolean.class == type) {
				json.put(Key, Field.getBoolean(object));
			} else if (int.class == type) {
				json.put(Key, Field.getInt(object));
			} else if (long.class == type) {
				json.put(Key, Field.getLong(object));
			} else if (float.class == type) {
				json.put(Key, Field.getFloat(object));
			} else if (double.class == type) {
				json.put(Key, (Field.getDouble(object)));
			} else if (String.class == type) {
				json.put(Key, Field.get(object));
			} else if (JSONObject.class == type || JSONArray.class == type) {
				json.put(Key, Field.get(object).toString());
			} else if (type.isEnum()) {
				final Object value = Field.get(object);
				if (value != null) {
					if (value instanceof TokenizedEnum<?>) {
						json.put(Key, ((TokenizedEnum<?>) value).getToken());
					} else {
						json.put(Key, ((Enum<?>) value).name());
					}
				}
			} else if (type.isArray()) {
				final Object array = Field.get(object);

				if (array != null) {
					final Class<?> componentType = type.getComponentType();

					if (boolean.class == componentType) {
						json.put(Key, Jsoner.toArray((boolean[]) array));
					} else if (int.class == componentType) {
						json.put(Key, Jsoner.toArray((int[]) array));
					} else if (long.class == componentType) {
						json.put(Key, Jsoner.toArray((long[]) array));
					} else if (float.class == componentType) {
						json.put(Key, Jsoner.toArray((float[]) array));
					} else if (double.class == componentType) {
						json.put(Key, Jsoner.toArray((double[]) array));
					} else if (String.class == componentType) {
						json.put(Key, Jsoner.toArray((String[]) array));
					} else if (componentType.isEnum()) {
						json.put(Key, Jsoner.toArray((Enum[]) array));
					} else {
						json.put(Key, Jsoner.toArray((Object[]) array, allFields));
					}
				}
			} else {
				final Object value = Field.get(object);

				if (value != null) {
					final JSONObject objectJson = new JSONObject();

					Jsoner.write(objectJson, value, allFields);

					json.put(Key, objectJson);
				}
			}
		}
		
		public boolean isServerField() {
			final Value value = getAnnotation(Value.class);
			
			return value.serverField();
		}
		
		public String getJsonKey() {
			final Value value = getAnnotation(Value.class);
			
			final String jsonKey = value.jsonKey();
			if (jsonKey == null || jsonKey.length() == 0 && value.serverField()) {
				return this.Key;
			}
			return jsonKey;
		}
		
		public Class[] getFieldClassAlternatives() {
			final Value value = getAnnotation(Value.class);

			return value.alternatives();
		}

		public boolean containsValues() {
			final Value value = getAnnotation(Value.class);

			return value.containsValues();
		}
	}

	private static final BaseFieldsCache<JsonableFieldInfo> JSONABLE_KEY_FIELDS_CACHE = new BaseFieldsCache<JsonableFieldInfo>() {
		@Override
		public JsonableFieldInfo getFieldInfo(final Class<?> type, final Field field, final Value annotation) {
			final String fieldKey = annotation.jsonKey();

			return !Value.EMPTY_KEY.equals(fieldKey) ? new JsonableFieldInfo(field, fieldKey) : null;
		}
	};

	public static final BaseFieldsCache<JsonableFieldInfo> JSONABLE_ALL_FIELDS_CACHE = new BaseFieldsCache<JsonableFieldInfo>() {
		@Override
		public JsonableFieldInfo getFieldInfo(final Class<?> type, final Field field, final Value annotation) {
			final String fieldKey = annotation.jsonKey();

			return new JsonableFieldInfo(
				field,
				!Value.EMPTY_KEY.equals(fieldKey) ?
					fieldKey
					: field.getName());
		}
	};

	private static final Map<Class<? extends Enum<?>>, Map<String, ? extends Enum<?>>> ENUM_CACHE = new HashMap<>();

	public static <E extends Enum<E>> E getEnum(final String jsonKey, final Class<E> enumType) {

		Map<String, E> enumCache = (Map<String, E>) ENUM_CACHE.get(enumType);

		if (enumCache == null) {
			synchronized (ENUM_CACHE) {
				enumCache = (Map<String, E>) ENUM_CACHE.get(enumType);

				if (enumCache == null) {
					enumCache = new HashMap<>();

					final E[] enumConstants = enumType.getEnumConstants();

					if (!ArrayUtils.isEmpty(enumConstants)) {
						if (TokenizedEnum.class.isAssignableFrom(enumType)) {
							for (final E enumConstant : enumConstants) {
								enumCache.put(((TokenizedEnum<?>) enumConstant).getToken(), enumConstant);
							}

							enumCache.put(ENUM_VALUE_UNKNOWN, ((TokenizedEnum<E>) enumConstants[0]).getDefault());
						} else {
							for (final E enumConstant : enumConstants) {
								enumCache.put(enumConstant.name(), enumConstant);
							}
						}
					}

					ENUM_CACHE.put(enumType, enumCache);
				}
			}
		}

		if (jsonKey == null) {
			return enumCache.get(ENUM_VALUE_UNKNOWN);
		}

		final E enumValue = enumCache.get(jsonKey);

		return enumValue != null ? enumValue : enumCache.get(ENUM_VALUE_UNKNOWN);
	}

	private static <T> Collection<JsonableFieldInfo> getKeyFields(final Class<T> type) {
		Assert.assertNotNull(type);

		return ValueHelper.getFields(JSONABLE_KEY_FIELDS_CACHE, type);
	}

	public static <T> Collection<JsonableFieldInfo> getAllFields(final Class<T> type) {
		Assert.assertNotNull(type);

		return ValueHelper.getFields(JSONABLE_ALL_FIELDS_CACHE, type);
	}

	// use Jackson.read
	public static <T> T read(final String jsonStr) {
		if (!TextUtils.isEmpty(jsonStr)) {
			final JSONObject json;

			try {
				json = new JSONObject(jsonStr);
			} catch (final JSONException e) {
//				L.e(e);

				return null;
			}

			return read(json);
		}

		return null;
	}

	// use Jackson.read
	public static <T> T read(final JSONObject json) {
		if (json == null) {
			return null;
		}

		final String className = json.optString(KEY_CLASS_NAME);

		if (!TextUtils.isEmpty(className)) {
			final Class<? extends T> type;

			try {
				type = (Class<? extends T>) Class.forName(className);
			} catch (final ClassNotFoundException e) {
//				L.e(e);

				return null;
			}

			try {
				return read(json, type, true);
			} catch (final JSONException e) {
//				L.e(e);

				return null;
			}
		}

		return null;
	}

	// use Jackson.read
	public static <T> T read(final String jsonStr, final Class<T> type) {
		return read(jsonStr, type, false);
	}

	// use Jackson.read
	public static <T> T read(final String jsonStr, final Class<T> type, final boolean allFields) {
		try {
			return TextUtils.isEmpty(jsonStr) ? null : read(new JSONObject(jsonStr), type, allFields);
		} catch (final JSONException e) {
//			L.e(new Exception(String.format("Type: %s, jsonStr: %s", type, jsonStr), e));

			return null;
		}
	}

	// use Jackson.read
	public static <T> T read(final JSONObject json, final Class<T> type) throws JSONException {
		return read(json, type, false);
	}

	// use Jackson.read
	public static <T> T read(final JSONObject json, final Class<T> type, final boolean allFields) throws JSONException {

		T result = null;

		if (UniqueObject.class.isAssignableFrom(type)) {

			if (json != null) {
				final String uniqueKeyName = getUniqueObjectKey(json, type);

				if (uniqueKeyName != null) {
					result = (T) Serializer.UNIQUE_OBJECTS_POOL.get(uniqueKeyName);
					if (result != null && ((UniqueObject) result).skipOnRead()) {

						return result;
					}
				}

				if (result == null) {
					result = ValueHelper.create(type);
				}

				read(json, result, allFields);

				Serializer.UNIQUE_OBJECTS_POOL.put(uniqueKeyName, result);

			}
		} else {
			if (json != null) {
				result = ValueHelper.create(type);

				read(json, result, allFields);
			}
		}

		return result;
	}

	private static <T> String getUniqueObjectKey(final JSONObject json, final Class<T> type) throws JSONException {
		final List<Object> keys = readKeys(json, type);

		return getUniqueObjectKey(type, keys);
	}

	private static List<Object> readKeys(final JSONObject json, final Class<?> type) throws JSONException {
		Assert.assertNotNull(json);

		final Collection<JsonableFieldInfo> uniqueFieldInfos = UNIQUE_OBJECTS_UNIQUE_FIELDS.get(type);

		if (uniqueFieldInfos != null) {
			final int size = uniqueFieldInfos.size();
			if (size == 0) {
				return null;
			}

			final List<Object> resultKeys = new ArrayList<>(size);
			for (final JsonableFieldInfo fieldInfo : uniqueFieldInfos) {
				if (fieldInfo!= null) {
					try {
						resultKeys.add(fieldInfo.read(json));
					} catch (final Exception ignored) {
					}
				}
			}

			if (size == resultKeys.size()) {
				return resultKeys;
			}
		}

		final List<Object> resultKeys = new ArrayList<>();
		final Collection<JsonableFieldInfo> keysFieldInfos = new HashSet<>();

		final Collection<JsonableFieldInfo> fieldInfos = getAllFields(type);

		for (final JsonableFieldInfo fieldInfo : fieldInfos) {
			if (fieldInfo.isAnnotationPresent(UniqueObjectField.class)) {

				keysFieldInfos.add(fieldInfo);
				try {
					resultKeys.add(fieldInfo.read(json));
				} catch (final IllegalAccessException e) {
					throw new RuntimeException(
						"Field " + type.getSimpleName() + "." + fieldInfo.Field.getName() + " is not visible", e);
				} catch (final UnknownFieldTypeException e) {
					throw new RuntimeException(
						"Unknown type of field " + type.getSimpleName() + "." + fieldInfo.Field.getName(), e);
				} catch (final IllegalArgumentException e) {
					final String exceptionDetails = "Type of field "
						+ type.getSimpleName() + "." + fieldInfo.Field.getName()
						+ " [\"" + fieldInfo.Key + "\"]"
						+ " (" + fieldInfo.Field.getType() + ")"
						+ " is not match with type of JSON field"
						+ " (\"" + json.get(fieldInfo.Key) + "\")";

//					ExceptionManager.getInstance().handleException(
//						new JSONException(
//							exceptionDetails + "\n\n"
//								+ "Reading field " + type.getSimpleName() + "." + fieldInfo.Field.getName()
//								+ " [\"" + fieldInfo.Key + "\"]"
//								+ " from \"" + json.toString() + "\""
//						),
//						0, 0
//					);

					throw new JSONException(exceptionDetails);
				} catch (final JSONException e) {
//					final JSONException jsonException = new JSONException(
//						e.getMessage() + "\n\n"
//							+ "Reading field " + type.getSimpleName() + "." + fieldInfo.Field.getName()
//							+ " [\"" + fieldInfo.Key + "\"]"
//							+ " from \"" + json.toString() + "\""
//					);

//					ExceptionManager.getInstance().handleException(jsonException, 0, 0);

					throw e;
				}
			}
		}

		if (keysFieldInfos.size() == resultKeys.size()) {
			return resultKeys;
		}

		return null;
	}

	// use Jackson.read
	private static <T> void read(final JSONObject json, final T object, final boolean allFields) throws JSONException {
		Assert.assertNotNull(json);
		Assert.assertNotNull(object);

		final Class<T> type = (Class<T>) object.getClass();

		final long timestamp = System.currentTimeMillis();

		final Collection<JsonableFieldInfo> fieldInfos = allFields ? getAllFields(type) : getKeyFields(type);

		for (final JsonableFieldInfo fieldInfo : fieldInfos) {
			try {
				fieldInfo.read(json, object, allFields);
			} catch (final IllegalAccessException e) {
				throw new RuntimeException(
					"Field " + type.getSimpleName() + "." + fieldInfo.Field.getName() + " is not visible", e);
			} catch (final UnknownFieldTypeException e) {
				throw new RuntimeException(
					"Unknown type of field " + type.getSimpleName() + "." + fieldInfo.Field.getName(), e);
			} catch (final IllegalArgumentException e) {
				final String exceptionDetails = "Type of field "
					+ type.getSimpleName() + "." + fieldInfo.Field.getName()
					+ " [\"" + fieldInfo.Key + "\"]"
					+ " (" + fieldInfo.Field.getType() + ")"
					+ " is not match with type of JSON field"
					+ " (\"" + json.get(fieldInfo.Key) + "\")";

//				ExceptionManager.getInstance().handleException(
//					new JSONException(
//						exceptionDetails + "\n\n"
//							+ "Reading field " + type.getSimpleName() + "." + fieldInfo.Field.getName()
//							+ " [\"" + fieldInfo.Key + "\"]"
//							+ " from \"" + json.toString() + "\""
//					),
//					0, 0
//				);

				throw new JSONException(exceptionDetails);
			} catch (final JSONException e) {
//				final JSONException jsonException = new JSONException(
//					e.getMessage() + "\n\n"
//						+ "Reading field " + type.getSimpleName() + "." + fieldInfo.Field.getName()
//						+ " [\"" + fieldInfo.Key + "\"]"
//						+ " from \"" + json.toString() + "\""
//				);

//				ExceptionManager.getInstance().handleException(jsonException, 0, 0);

				throw e;
			}
		}

//		if (object instanceof CustomJsonable) {
//			final JsonableReader reader = new JsonableReader(json, allFields);
//
//			reader.startRead();
//
//			try {
//				((CustomJsonable) object).read(reader);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//			reader.endRead();
//		}

		if (ValueHelper.LOGGING) {
//			L.d("Time of reading object from json: " + (System.currentTimeMillis() - timestamp));
		}
	}

	// use Jackson.read
	public static <T> T read(
		final JSONObject json, final String key, final Class<T> type, final boolean allFields
	) throws JSONException
	{
		return json != null ? read(json.optJSONObject(key), type, allFields) : null;
	}

	// use Jackson.readArray
	public static <T> T[] readArray(final JSONArray jsonArray, final Class<T> type, final boolean allFields)
		throws JSONException
	{
		Assert.assertNotNull(type);

		if (jsonArray != null) {
			final int count = jsonArray.length();

			final Collection<T> objects = new ArrayList<>(count);

			for (int i = 0; i < count; i++) {
				if (!jsonArray.isNull(i)) {
					final JSONObject jsonObject = jsonArray.optJSONObject(i);

					if (jsonObject != null) {
						objects.add(read(jsonObject, type, allFields));
					} else {
						final String jsonObjectStr = jsonArray.optString(i);

						if (!TextUtils.isEmpty(jsonObjectStr)) {
							objects.add(read(jsonObjectStr, type, allFields));
						}
					}
				}
			}

			return objects.toArray((T[]) Array.newInstance(type, objects.size()));
		} else {
			return null;
		}
	}

	// use Jackson.readArray
	public static <T> T[] readArray(
		final JSONObject json, final String key, final Class<T> type, final boolean allFields
	) throws JSONException
	{
		if (json != null) {
			final JSONArray jsonArray = json.optJSONArray(key);

			if (jsonArray != null) {
				return readArray(jsonArray, type, allFields);
			} else {
				final JSONObject jsonObject = json.optJSONObject(key);

				if (jsonObject != null) {
					// TODO для чего это вообще?
//					return readDictionary(jsonObject, type, allFields);
					return null;
				}
			}
		}

		return null;
	}

	// use Jackson.readArray
	public static <T> T[] readArray(
		final String jsonArrayStr, final Class<T> type, final boolean allFields)
	{
		try {
			return TextUtils.isEmpty(jsonArrayStr) ? null : readArray(new JSONArray(jsonArrayStr), type, allFields);
		} catch (final JSONException e) {
//			L.e(e);

			return null;
		}
	}


	public static <T> T[] readDictionary(
		final JSONObject json, final Class<T> type, final boolean allFields
	) throws JSONException
	{
		Assert.assertNotNull(type);

		if (json != null) {
			final Iterator<String> keysIterator = json.keys();

			if (keysIterator != null && keysIterator.hasNext()) {
				final Collection<T> objects = new ArrayList<>();

				while (keysIterator.hasNext()) {
					final JSONObject jsonObject = json.optJSONObject(keysIterator.next());

					if (jsonObject != null) {
						objects.add(read(jsonObject, type, allFields));
					} else {
						final String jsonObjectStr = json.optString(keysIterator.next());

						if (!TextUtils.isEmpty(jsonObjectStr)) {
							objects.add(read(jsonObjectStr, type, allFields));
						}
					}
				}

				return objects.toArray((T[]) Array.newInstance(type, objects.size()));
			}
		}

		return null;
	}

	public static <T> Map<String, T> readMap(final JSONObject object) throws JSONException {
		Assert.assertNotNull(object);
		final Map<String, T> map = new HashMap<>();
		
		final Iterator<String> keysItr = object.keys();
		while(keysItr.hasNext()) {
			final String key = keysItr.next();
			final T value = (T)object.get(key);
			map.put(key, value);
		}
		return map;
	}

	public static <T> void write(final JSONObject json, final T object, final boolean allFields) throws JSONException {
		Assert.assertNotNull(json);
		Assert.assertNotNull(object);

		final Class<T> type = (Class<T>) object.getClass();

		if (allFields) {
			json.put(KEY_CLASS_NAME, type.getName());
		}

		final Collection<JsonableFieldInfo> fieldInfos = allFields ? getAllFields(type) : getKeyFields(type);

		for (final JsonableFieldInfo fieldInfo : fieldInfos) {
			try {
				fieldInfo.write(json, object, allFields);
			} catch (final IllegalAccessException e) {
				// do nothing
			} catch (final UnknownFieldTypeException e) {
//				L.e(e);
			}
		}

		if (object instanceof CustomJsonable) {
			final JsonableWriter writer = new JsonableWriter(json, allFields);

			writer.startWrite();

			((CustomJsonable) object).write(writer);

			writer.endWrite();
		}
	}

	public static <T> JSONObject write(final T object, final boolean allFields) throws JSONException {
		if (object != null) {
			final JSONObject json = new JSONObject();

			Jsoner.write(json, object, allFields);

			return json;
		} else {
			return null;
		}
	}
	
	public static <R,T> JSONObject writeMap(final Map<R,T> object, final boolean allFields) throws JSONException {
		if (object != null) {
			final JSONObject json = new JSONObject();
			
			for (final Entry<R,T> entry : object.entrySet()) {
				json.put(entry.getKey().toString(), write(entry.getValue(), allFields));
			}
			
			return json;
		} else {
			return null;
		}
	}

	public static <T> String toString(final T object) {
		final JSONObject json;

		try {
			json = write(object, true);
		} catch (final JSONException e) {
//			L.e(e);

			return null;
		}

		return json != null ? json.toString() : null;
	}

	// use JacksonJsoner.tryParseString
	public static String optString(final JSONObject json, final String key) {
		Assert.assertNotNull(json);

		final Object object = json.opt(key);

		return JSONObject.NULL.equals(object) ? null : object.toString();
	}

	// user JacksonJsoner.tryParseTimeStamp
	public static long optIviTimestamp(final JSONObject json, final String key) {
		Assert.assertNotNull(json);
		Assert.assertTrue(!TextUtils.isEmpty(key));

		final Date date = json.has(key) && !json.isNull(key) ? DateUtils.parseIviDate(optString(json, key)) : null;

		return DateUtils.getTimestamp(date);
	}

	public static long optIso8601Timestamp(final JSONObject json, final String key) {
		Assert.assertNotNull(json);
		Assert.assertTrue(!TextUtils.isEmpty(key));

		final Date date = json.has(key) && !json.isNull(key) ? DateUtils.parseIso8601Date(optString(json, key)) : null;

		return DateUtils.getTimestamp(date);
	}

//	public static long optIviTimestamp(final JsonableReader reader, final String key) {
//		Assert.assertNotNull(reader);
//		Assert.assertTrue(!TextUtils.isEmpty(key));
//
//		final Date date = !reader.isNull(key) ? DateUtils.parseIviDate(reader.readString(key)) : null;
//
//		return DateUtils.getTimestamp(date);
//	}
//
//	public static long optIso8601Timestamp(final JsonableReader reader, final String key) {
//		Assert.assertNotNull(reader);
//		Assert.assertTrue(!TextUtils.isEmpty(key));
//
//		final Date date = reader.isNull(key) ? null : DateUtils.parseIso8601Date(reader.readString(key));
//
//		return DateUtils.getTimestamp(date);
//	}

	public static <E extends Enum<E>> void putEnum(
		final JSONObject json, final String jsonKey, final E enumValue
	) throws JSONException
	{
		json.put(
			jsonKey, enumValue != null
				? (enumValue instanceof TokenizedEnum<?> ? ((TokenizedEnum<E>) enumValue).getToken() : enumValue.name())
				: ENUM_VALUE_UNKNOWN);
	}


	@Deprecated
	// use Jackson.readEnum
	public static <E extends Enum<E>> E optEnum(
		final JSONObject json, final String jsonKey, final Class<E> enumType
	) throws JSONException
	{
		return getEnum(json.optString(jsonKey), enumType);
	}

	@Deprecated
	// use Jackson.readObject
	public static <T> T optObject(
		final JSONObject json, final String key, final Class<T> type, final boolean allFields
	) throws JSONException
	{
		Assert.assertNotNull(json);
		Assert.assertTrue(!TextUtils.isEmpty(key));
		Assert.assertNotNull(type);

		final JSONObject jsonObject = json.optJSONObject(key);

		return jsonObject != null ? read(jsonObject, type, allFields) : null;
	}

	@Deprecated
	// use JacksonJsoner.readBooleanArray
	public static boolean[] optBooleanArray(final JSONObject json, final String key) {
		Assert.assertNotNull(json);
		Assert.assertTrue(!TextUtils.isEmpty(key));

		final JSONArray jsonArray = json.optJSONArray(key);

		if (jsonArray != null) {
			final int size = jsonArray.length();
			final boolean[] result = new boolean[size];

			for (int i = 0; i < size; i++) {
				result[i] = jsonArray.optBoolean(i);
			}

			return result;
		} else {
			return null;
		}
	}

	@Deprecated
	// use JacksonJsoner.readIntArray
	public static int[] optIntArray(final JSONObject json, final String key) {
		Assert.assertNotNull(json);
		Assert.assertTrue(!TextUtils.isEmpty(key));

		final JSONArray jsonArray = json.optJSONArray(key);

		if (jsonArray != null) {
			final int size = jsonArray.length();
			final int[] result = new int[size];

			for (int i = 0; i < size; i++) {
				result[i] = jsonArray.optInt(i);
			}

			return result;
		} else {
			return null;
		}
	}

	@Deprecated
	// use JacksonJsoner.readLongArray
	public static long[] optLongArray(final JSONObject json, final String key) {
		Assert.assertNotNull(json);
		Assert.assertTrue(!TextUtils.isEmpty(key));

		final JSONArray jsonArray = json.optJSONArray(key);

		if (jsonArray != null) {
			final int size = jsonArray.length();
			final long[] result = new long[size];

			for (int i = 0; i < size; i++) {
				result[i] = jsonArray.optLong(i);
			}

			return result;
		} else {
			return null;
		}
	}

	@Deprecated
	// use JacksonJsoner.readFloatArray
	public static float[] optFloatArray(final JSONObject json, final String key) {
		Assert.assertNotNull(json);
		Assert.assertTrue(!TextUtils.isEmpty(key));

		final JSONArray jsonArray = json.optJSONArray(key);

		if (jsonArray != null) {
			final int size = jsonArray.length();
			final float[] result = new float[size];

			for (int i = 0; i < size; i++) {
				result[i] = (float) jsonArray.optDouble(i);
			}

			return result;
		} else {
			return null;
		}
	}

	@Deprecated
	// use JacksonJsoner.readDoubleArray
	public static double[] optDoubleArray(final JSONObject json, final String key) {
		Assert.assertNotNull(json);
		Assert.assertTrue(!TextUtils.isEmpty(key));

		final JSONArray jsonArray = json.optJSONArray(key);

		double[] result = null;

		if (jsonArray != null) {
			final int size = jsonArray.length();
			result = new double[size];

			for (int i = 0; i < size; i++) {
				result[i] = jsonArray.optDouble(i);
			}

			return result;
		} else {
			return null;
		}
	}

	@Deprecated
	// use JacksonJsoner.readStringArray
	public static String[] optStringArray(final JSONObject json, final String key){
		Assert.assertNotNull(json);
		Assert.assertTrue(!TextUtils.isEmpty(key));

		final JSONArray jsonArray = json.optJSONArray(key);

		String[] array = null;

		if (jsonArray != null) {
			final int size = jsonArray.length();
			array = new String[size];

			for (int i = 0; i < size; i++) {
				array[i] = jsonArray.optString(i);
			}

			return array;
		} else {
			return null;
		}
	}

	@Deprecated
	// use JacksonJsoner.readEnumArray
	public static <E extends Enum<E>> E[] optEnumArray(
		final JSONObject json, final String key, final Class<E> enumType)
	{
		Assert.assertNotNull(json);
		Assert.assertTrue(!TextUtils.isEmpty(key));

		final JSONArray jsonArray = json.optJSONArray(key);

		if (jsonArray != null) {
			final int size = jsonArray.length();
			final E[] array = (E[]) Array.newInstance(enumType, size);

			for (int i = 0; i < size; i++) {
				array[i] = getEnum(jsonArray.optString(i), enumType);
			}

			return array;
		} else {
			return null;
		}
	}

	public static JSONArray toArray(final boolean[] array) {
		final JSONArray jsonArray = new JSONArray();

		if (array != null) {
			for (final boolean item : array) {
				jsonArray.put(item);
			}
		}

		return jsonArray;
	}

	public static JSONArray toArray(final int[] array) {
		final JSONArray jsonArray = new JSONArray();

		if (array != null) {
			for (final int item : array) {
				jsonArray.put(item);
			}
		}

		return jsonArray;
	}

	public static JSONArray toArray(final long[] array) {
		final JSONArray jsonArray = new JSONArray();

		if (array != null) {
			for (final long item : array) {
				jsonArray.put(item);
			}
		}

		return jsonArray;
	}

	public static JSONArray toArray(final float[] array) throws JSONException {
		final JSONArray jsonArray = new JSONArray();

		if (array != null) {
			for (final float item : array) {
				jsonArray.put(item);
			}
		}

		return jsonArray;
	}

	public static JSONArray toArray(final double[] array) throws JSONException {
		final JSONArray jsonArray = new JSONArray();

		if (array != null) {
			for (final double item : array) {
				jsonArray.put(item);
			}
		}

		return jsonArray;
	}

	public static JSONArray toArray(final String[] array) {
		final JSONArray jsonArray = new JSONArray();

		if (array != null) {
			for (final String item : array) {
				if (item != null) {
					jsonArray.put(item);
				}
			}
		}

		return jsonArray;
	}

	public static <E extends Enum<E>> JSONArray toArray(final E[] array) {
		return toArray(!ArrayUtils.isEmpty(array) ? Arrays.asList(array) : null);
	}

	public static <E extends Enum<E>> JSONArray toArray(final Iterable<E> iterable) {
		final JSONArray jsonArray = new JSONArray();

		if (iterable != null) {
			for (final E item : iterable) {
				if (item != null) {
					if (item instanceof TokenizedEnum<?>) {
						jsonArray.put(((TokenizedEnum<E>) item).getToken());
					} else {
						jsonArray.put(item.name());
					}
				}
			}
		}

		return jsonArray;
	}

	public static <T> JSONArray toArray(final T[] array, final boolean allFields) {
		return toArray(ArrayUtils.isEmpty(array) ? null : Arrays.asList(array), allFields);
	}

	public static <T> JSONArray toArray(final Iterable<T> iterable, final boolean allFields) {
		final JSONArray jsonArray = new JSONArray();

		if (iterable != null) {
			for (final T item : iterable) {
				if (item != null) {
					final JSONObject itemJson = new JSONObject();

					try {
						write(itemJson, item, allFields);
					} catch (final JSONException e) {
//						L.e(e);

						continue;
					}

					jsonArray.put(itemJson);
				}
			}
		}

		return jsonArray;
	}

	public static JSONArray toArray(final JSONObject[] array) {
		final JSONArray jsonArray = new JSONArray();

		if (array != null) {
			for (final JSONObject item : array) {
				if (item != null) {
					jsonArray.put(item);
				}
			}
		}

		return jsonArray;
	}

	public static JSONObject appendObject(final JSONObject json, final JSONObject jsonToAppend) throws JSONException {
		Assert.assertNotNull(json);
		Assert.assertNotNull(jsonToAppend);

		final Iterator<String> iterator = jsonToAppend.keys();

		while (iterator.hasNext()) {
			final String key = iterator.next();

			json.put(key, jsonToAppend.get(key));
		}

		return json;
	}

	public static void putIviDate(final JSONObject json, final String key, final long timestamp) throws JSONException {
		Assert.assertNotNull(json);
		Assert.assertTrue(!TextUtils.isEmpty(key));

		if (timestamp != 0) {
			json.put(key, DateUtils.formatIviDate(timestamp));
		} else {
			json.remove(key);
		}
	}

	public static void putIso8601Date(final JSONObject json, final String key, final long timestamp)
		throws JSONException
	{
		Assert.assertNotNull(json);
		Assert.assertTrue(!TextUtils.isEmpty(key));

		if (timestamp != 0) {
			json.put(key, DateUtils.formatIso8601Date(timestamp));
		} else {
			json.remove(key);
		}
	}

	public static <R,T> void putMap(final JSONObject json, final String key, final Map<R, T> map)
		throws JSONException
	{
		Assert.assertNotNull(json);
		Assert.assertTrue(!TextUtils.isEmpty(key));

		if (map != null) {
			final JSONObject jsonMap = new JSONObject();

			for (final Entry<R, T> entry : map.entrySet()) {
				jsonMap.put(entry.getKey().toString(), entry.getValue());
			}

			json.put(key, jsonMap);
		}
	}

	private static void log(final String...messages) {
		final StringBuilder log = new StringBuilder();
		for (final String message : messages) {
			log.append(message);
		}
		Log.d(TAG, log.toString());
	}

	private static void logTestParsers(final Object jacksonParsing, final Object jsonerParsing) {
		if (jacksonParsing != null) {
			final String jsonerString = jsonerParsing.toString();
			final String jacksonString = jacksonParsing.toString();
			final boolean equals = jacksonParsing.equals(jsonerParsing);
			log("TEST_PARSING: ",jsonerParsing.getClass().getSimpleName()," equals ",(equals+""));
			if (!equals) {
				log("TEST_PARSING JACKSON: ", jacksonString);
				log("TEST_PARSING JSONER : ", jsonerString);
			}
		} else {
			log("TEST_PARSING JACKSON ERROR "+jsonerParsing.getClass().getSimpleName());
			log("TEST_PARSING JACKSON ERROR: JSONER : ", jsonerParsing.toString());
		}
	}
}
