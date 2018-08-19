package ru.ivi.mapping;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ru.ivi.mapping.value.IEnumTokensMap;
import ru.ivi.mapping.value.IUniqueFieldsMap;
import ru.ivi.mapping.value.IValueMap;
import ru.ivi.mapping.value.ResponseData;
import ru.ivi.mapping.value.TokenizedEnum;
import ru.ivi.mapping.value.UniqueObject;
import ru.ivi.utils.ArrayUtils;
import ru.ivi.utils.DateUtils;
import ru.ivi.utils.ParseUtils;
import ru.ivi.utils.ReflectUtils;


@SuppressWarnings("unused")
public final class JacksonJsoner {
	
	private static final ObjectMapper JACKSON_OBJECT_MAPPER = new ObjectMapper();
	private static final JsonFactory JACKSON_JSON_FACTORY = new JsonFactory();
	
	private static IValueMap sValueMap = null;
	private static IEnumTokensMap sEnumTokensMap = null;
	private static IUniqueFieldsMap sUniqueFieldsMap = null;
	
	public static final String TAG = JacksonJsoner.class.getSimpleName();
	
	public static final String RESULT = "result";
	public static final String ERROR = "error";
	
	private static final int DEFAULT_INT_VALUE = 0;
	private static final long DEFAULT_LONG_VALUE = 0L;
	private static final byte DEFAULT_BYTE_VALUE = 0;
	private static final float DEFAULT_FLOAT_VALUE = 0F;
	private static final double DEFAULT_DOUBLE_VALUE = 0D;
	private static final boolean DEFAULT_BOOLEAN_VALUE = false;
	
	private static final Collection<Class<?>> PRIMITIVE_WRAPPER_CLASSES = new HashSet<Class<?>>() {{
		add(Boolean.class);
		add(Integer.class);
		add(Float.class);
		add(Double.class);
		add(Long.class);
		add(Byte.class);
	}};
	
	private static final Collection<Class<?>> PRIMITIVE_CLASSES = new HashSet<Class<?>>() {{
		add(boolean.class);
		add(int.class);
		add(float.class);
		add(byte.class);
		add(double.class);
		add(long.class);
		add(char.class);
	}};
	
	public static void setValueMap(final IValueMap valueMap) {
		sValueMap = valueMap;
	}
	
	public static void setEnumTokenMap(final IEnumTokensMap enumTokensMap) {
		sEnumTokensMap = enumTokensMap;
	}
	
	public static void setUniqueFieldsMap(final IUniqueFieldsMap uniqueFieldsMap) {
		sUniqueFieldsMap = uniqueFieldsMap;
	}
	
	public abstract static class FieldInfoInt<Object> implements IFieldInfo<Object> {
	
	}
	
	public abstract static class FieldInfoLong<Object> implements IFieldInfo<Object> {
	
	}
	
	public abstract static class FieldInfoFloat<Object> implements IFieldInfo<Object> {
	
	}
	
	public abstract static class FieldInfoDouble<Object> implements IFieldInfo<Object> {
	
	}
	
	@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
	public abstract static class FieldInfoBoolean<Object> implements IFieldInfo<Object> {
	
	}
	
	public abstract static class FieldInfoByte<Object> implements IFieldInfo<Object> {
	
	}
	
	public abstract static class FieldInfo<Object, FieldType> implements IFieldInfo<Object> {
	
	}
	
	@NonNull
	public static <Result, Error> Pair<Result, Error> readObjectOrError(@Nullable final ResponseData responseData,
		final Class<Result> resultClass, final Class<Error> errorClass) throws IOException {
		if (PRIMITIVE_CLASSES.contains(resultClass) || PRIMITIVE_CLASSES.contains(errorClass)) {
			throw new IllegalArgumentException("Parse class can not be primitive!");
		}
		
		final Pair<Result, Error> resultErrorPair;
		
		if (responseData != null && responseData.getData() != null) {
			final Result result;
			final JsonNode jsonNode = JACKSON_OBJECT_MAPPER.readTree(responseData.getData());
			
			if (resultClass == String.class) {
				//noinspection unchecked
				result = (Result) jsonNode.asText();
			} else {
				JsonParser parser = null;
				try {
					parser = jsonNode.traverse();
					if (PRIMITIVE_WRAPPER_CLASSES.contains(resultClass)) {
						result = readPrimitive(resultClass, parser);
					} else {
						result = readObject(parser, jsonNode, resultClass);
					}
				} finally {
					if (parser != null) {
						parser.close();
					}
				}
			}
			
			if (result == null) {
				final Error error;
				if (jsonNode.hasNonNull(ERROR)) {
					final JsonNode resultNode = jsonNode.get(ERROR);
					JsonParser parser = null;
					try {
						parser = resultNode.traverse();
						error = readObject(parser, resultNode, errorClass);
					} finally {
						if (parser != null) {
							parser.close();
						}
					}
				} else {
					error = null;
				}
				resultErrorPair = new Pair<>(result, error);
			} else {
				resultErrorPair = new Pair<>(result, null);
			}
		} else {
			resultErrorPair = new Pair<>(null, null);
		}
		
		return resultErrorPair;
	}
	
	@NonNull
	public static <Result, Error> Pair<Result, Error> readResultObjectOrError(@Nullable final ResponseData responseData,
		final Class<Result> resultClass, final Class<Error> errorClass) throws IOException {
		if (PRIMITIVE_CLASSES.contains(resultClass) || PRIMITIVE_CLASSES.contains(errorClass)) {
			throw new IllegalArgumentException("Parse class can not be primitive!");
		}
		
		final Pair<Result, Error> resultErrorPair;
		
		if (responseData != null && responseData.getData() != null) {
			final Result result;
			final JsonNode jsonNode = JACKSON_OBJECT_MAPPER.readTree(responseData.getData());
			
			if (jsonNode.hasNonNull(JacksonJsoner.RESULT)) {
				final JsonNode resultNode = jsonNode.get(JacksonJsoner.RESULT);
				if (resultClass == String.class) {
					//noinspection unchecked
					result = (Result) resultNode.asText();
				} else {
					JsonParser parser = null;
					try {
						parser = resultNode.traverse();
						if (PRIMITIVE_WRAPPER_CLASSES.contains(resultClass)) {
							result = readPrimitive(resultClass, parser);
						} else {
							result = readObject(parser, resultNode, resultClass);
						}
					} finally {
						if (parser != null) {
							parser.close();
						}
					}
				}
			} else {
				result = null;
			}
			
			if (result == null) {
				final Error error;
				if (jsonNode.hasNonNull(ERROR)) {
					final JsonNode resultNode = jsonNode.get(ERROR);
					JsonParser parser = null;
					try {
						parser = resultNode.traverse();
						error = readObject(parser, resultNode, errorClass);
					} finally {
						if (parser != null) {
							parser.close();
						}
					}
				} else {
					error = null;
				}
				resultErrorPair = new Pair<>(result, error);
			} else {
				resultErrorPair = new Pair<>(result, null);
			}
		} else {
			resultErrorPair = new Pair<>(null, null);
		}
		
		return resultErrorPair;
	}
	
	@NonNull
	public static <Result, Error> Pair<Result[], Error> readResultArrayOrError(@Nullable final ResponseData responseData,
		final Class<Result> resultClass, final Class<Error> errorClass) throws IOException {
		if (PRIMITIVE_CLASSES.contains(resultClass) || PRIMITIVE_CLASSES.contains(errorClass)) {
			throw new IllegalArgumentException("Parse class can not be primitive!");
		}
		
		final Pair<Result[], Error> resultErrorPair;
		
		if (responseData != null && responseData.getData() != null) {
			final JsonNode jsonNode = JACKSON_OBJECT_MAPPER.readTree(responseData.getData());
			
			final Result[] result;
			if (jsonNode.hasNonNull(RESULT)) {
				final JsonNode resultNode = jsonNode.get(RESULT);
				JsonParser parser = null;
				try {
					//noinspection resource
					parser = resultNode.traverse();
					
					result = ArrayUtils.toArray(readArray(parser, resultNode, resultClass), resultClass);
				} finally {
					if (parser != null) {
						parser.close();
					}
				}
			} else {
				result = null;
			}
			
			if (result == null) {
				final Error error;
				if (jsonNode.hasNonNull(ERROR)) {
					final JsonNode resultNode = jsonNode.get(ERROR);
					JsonParser parser = null;
					try {
						parser = resultNode.traverse();
						error = readObject(parser, resultNode, errorClass);
					} finally {
						if (parser != null) {
							parser.close();
						}
					}
					
				} else {
					error = null;
				}
				resultErrorPair = new Pair<>(null, error);
			} else {
				
				resultErrorPair = new Pair<>(result, null);
			}
		} else {
			resultErrorPair = new Pair<>(null, null);
		}
		
		return resultErrorPair;
	}
	
	public static <T> T read(@Nullable final ResponseData responseData, final Class<T> type) throws IOException {
		if (responseData == null || responseData.getData() == null) {
			return null;
		}
		
		T result;
		JsonParser parser = null;
		try {
			final JsonNode jsonNode = JACKSON_OBJECT_MAPPER.readTree(responseData.getData());
			//noinspection resource
			parser = jsonNode.traverse();
			
			result = readObject(parser, jsonNode, type);
		} finally {
			if (parser != null) {
				parser.close();
			}
		}
		
		
		return result;
	}
	
	public static <T> T read(final String jsonStr, final Class<T> type) throws IOException {
		if (TextUtils.isEmpty(jsonStr)) {
			return null;
		}
		
		T result = null;
		final JsonNode jsonNode = JACKSON_OBJECT_MAPPER.readTree(jsonStr);
		JsonParser parser = null;
		try {
			parser = jsonNode.traverse();
			
			result = readObject(parser, jsonNode, type);
		} finally {
			if (parser != null) {
				parser.close();
			}
		}
		
		return result;
	}
	
	public static <T> T readObject(final JsonParser parser, final JsonNode sourceNode, final Class<T> classType) throws IOException {
		if (BuildConfig.DEBUG) {
			ru.ivi.utils.Assert.assertTrue("do not parse json in ui thread", Looper.getMainLooper() != Looper.myLooper());
		}
		moveToToken(parser);
		
		if (parser.getCurrentToken() == JsonToken.VALUE_NULL) {
			return null;
		}
		
		if (TokenizedEnum.class.isAssignableFrom(classType)) {
			final String valueAsString = parser.getValueAsString();
			
			return readEnum(valueAsString, classType);
		}
		
		final ObjectMap<String, IFieldInfo> objectMap = sValueMap.getObjectMap(classType);
		
		final T result;
		if (objectMap == null) {
			result = classType == Object.class ? null : ReflectUtils.createReflect(classType);
		} else {
			result = objectMap.create(classType);
		}
		final Map<String, IFieldInfo> fieldInfoMap = objectMap == null ? null : objectMap.getFields();
		if (fieldInfoMap != null && !fieldInfoMap.isEmpty()) {
			for (
				JsonToken token = parser.nextToken();
				token != null && token != JsonToken.END_OBJECT;
				token = parser.nextToken()
				) {
				
				if (token == JsonToken.FIELD_NAME) {
					final String fieldName = parser.getCurrentName();
					//noinspection AssignmentToForLoopParameter
					token = parser.nextToken();
					
					final IFieldInfo fieldInfo = fieldInfoMap.get(fieldName);
					if (fieldInfo != null) {
						//noinspection unchecked
						fieldInfo.read(result, parser, sourceNode == null ? null : sourceNode.get(fieldName));
					} else if (token == JsonToken.START_OBJECT) {
						skipObjectField(parser);
					} else if (token == JsonToken.START_ARRAY) {
						skipArrayField(parser);
					}
				}
			}
		}
		
		if (result instanceof CustomJsonable && sourceNode != null) {
			((CustomJsonable) result).read(new JsonableReader(sourceNode));
		}
		
		if (result instanceof CustomAfterRead) {
			((CustomAfterRead) result).afterRead();
		}
		
		if (result instanceof UniqueObject) {
			final String uniqueKeyName = sUniqueFieldsMap.getUniqueKey(result, classType);
			if (!TextUtils.isEmpty(uniqueKeyName)) {
				Serializer.UNIQUE_OBJECTS_POOL.put(uniqueKeyName, result);
			}
		}
		
		if (fieldInfoMap == null || fieldInfoMap.isEmpty()) {
			if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
				skipObjectField(parser);
			} else if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
				skipArrayField(parser);
			}
		}
		
		return result;
	}
	
	private static void moveToToken(final JsonParser parser) throws IOException {
		if (parser.getCurrentToken() == null) {
			parser.nextToken();
		}
	}
	
	private static void skipObjectField(final JsonParser parser) throws IOException {
		moveToToken(parser);
		
		int started = 0;
		int ended = 0;
		for (
			JsonToken token = parser.nextToken();
			token != null && (token != JsonToken.END_OBJECT || started > 0 && started != ended);
			token = parser.nextToken()
			) {
			if (token == JsonToken.START_OBJECT) {
				started++;
			} else if (token == JsonToken.END_OBJECT) {
				ended++;
			}
		}
	}
	
	private static void skipArrayField(final JsonParser parser) throws IOException {
		moveToToken(parser);
		
		int started = 0;
		int ended = 0;
		for (
			JsonToken token = parser.nextToken();
			token != null && (token != JsonToken.END_ARRAY || started > 0 && started != ended);
			token = parser.nextToken()
			) {
			if (token == JsonToken.START_ARRAY) {
				started++;
			} else if (token == JsonToken.END_ARRAY) {
				ended++;
			}
		}
	}
	
	public static <T> T[] readArray(final String jsonStr, final Class<T> classType) throws IOException {
		final T[] result;
		final JsonParser parser;
		
		if (!TextUtils.isEmpty(jsonStr)) {
			final JsonNode jsonNode = JACKSON_OBJECT_MAPPER.readTree(jsonStr);
			
			//noinspection resource
			parser = jsonNode.traverse();
			
			result = ArrayUtils.toArray(readArray(parser, jsonNode, classType), classType);
		} else {
			parser = null;
			result = null;
		}
		
		if (parser != null) {
			parser.close();
		}
		
		return result;
	}
	
	
	public static <T> T[] readArray(@Nullable final ResponseData responseData, final Class<T> type) throws IOException {
		if (responseData == null || responseData.getData() == null) {
			return null;
		}
		
		T[] result = null;
		JsonParser parser = null;
		try {
			final JsonNode jsonNode = JACKSON_OBJECT_MAPPER.readTree(responseData.getData());
			//noinspection resource
			parser = jsonNode.traverse();
			result = ArrayUtils.toArray(readArray(parser, jsonNode, type), type);
		} finally {
			if (parser != null) {
				parser.close();
			}
		}
		
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "TypeMayBeWeakened" })
	public static <T> Collection<T> readArray(final JsonParser parser, final JsonNode sourceNode, final Class<T> classType) throws IOException {
		moveToToken(parser);
		
		final boolean isOneObjectArray = parser.getCurrentToken() != JsonToken.START_ARRAY;
		
		final Collection<T> result = new ArrayList<>();
		if (parser.getCurrentToken() != JsonToken.VALUE_NULL) {
			for (
				JsonToken token = parser.getCurrentToken();
				token != null && token != JsonToken.END_ARRAY;
				token = parser.nextToken()
				) {
				if (token == JsonToken.VALUE_STRING) {
					if (classType == String.class) {
						result.add((T) parser.getValueAsString());
					}
				} else if (token == JsonToken.VALUE_NUMBER_INT) {
					if (classType == Integer.class) {
						result.add((T) parser.getNumberValue());
					}
				} else if (token == JsonToken.START_OBJECT) {
					result.add(readObject(parser, sourceNode == null ? null : sourceNode.get(result.size()), classType));
				}
				if (isOneObjectArray) {
					break;
				}
			}
		}
		
		return result;
	}
	
	public static <E extends Enum<E>> E[] readEnumArray(final JsonParser parser, final Class<E> classType) throws IOException {
		moveToToken(parser);
		final boolean isOneObjectArray = parser.getCurrentToken() != JsonToken.START_ARRAY;
		
		if (parser.getCurrentToken() != JsonToken.VALUE_NULL) {
			final Collection<E> result = new ArrayList<>();
			for (
				JsonToken token = parser.getCurrentToken();
				token != null && token != JsonToken.END_ARRAY;
				token = parser.nextToken()
				) {
				
				if (token == JsonToken.VALUE_STRING) {
					result.add(readEnum(parser, classType));
				}
				if (isOneObjectArray) {
					break;
				}
			}
			
			//noinspection unchecked
			return result.toArray((E[]) Array.newInstance(classType, result.size()));
		} else {
			//noinspection unchecked
			return (E[]) Array.newInstance(classType, 0);
		}
	}
	
	
	public static int[] readIntArray(final JsonParser parser) throws IOException {
		moveToToken(parser);
		final boolean isOneObjectArray = parser.getCurrentToken() != JsonToken.START_ARRAY;
		
		if (parser.getCurrentToken() != JsonToken.VALUE_NULL) {
			final List<Integer> result = new ArrayList<>();
			
			for (
				JsonToken token = parser.getCurrentToken();
				token != null && token != JsonToken.END_ARRAY;
				token = parser.nextToken()
				) {
				if (token == JsonToken.VALUE_NUMBER_INT) {
					result.add(tryParseInteger(parser));
				}
				if (isOneObjectArray) {
					break;
				}
			}
			
			return ArrayUtils.toPrimitive(result);
		} else {
			return ArrayUtils.EMPTY_INT_ARRAY;
		}
	}
	
	public static byte[] readByteArray(final JsonParser parser) throws IOException {
		moveToToken(parser);
		final boolean isOneObjectArray = parser.getCurrentToken() != JsonToken.START_ARRAY;
		
		if (parser.getCurrentToken() != JsonToken.VALUE_NULL) {
			final List<Byte> result = new ArrayList<>();
			
			for (
				JsonToken token = parser.getCurrentToken();
				token != null && token != JsonToken.END_ARRAY;
				token = parser.nextToken()
				) {
				if (token == JsonToken.VALUE_NUMBER_INT) {
					result.add(tryParseByte(parser));
				}
				if (isOneObjectArray) {
					break;
				}
			}
			
			return ArrayUtils.toPrimitiveByte(result);
		} else {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
	}
	
	public static char[] readCharArray(final JsonParser parser) throws IOException {
		moveToToken(parser);
		final boolean isOneObjectArray = parser.getCurrentToken() != JsonToken.START_ARRAY;
		
		if (parser.getCurrentToken() != JsonToken.VALUE_NULL) {
			final List<Integer> result = new ArrayList<>();
			
			for (
				JsonToken token = parser.getCurrentToken();
				token != null && token != JsonToken.END_ARRAY;
				token = parser.nextToken()
				) {
				if (token == JsonToken.VALUE_NUMBER_INT) {
					result.add(tryParseInteger(parser));
				}
				if (isOneObjectArray) {
					break;
				}
			}
			
			return ArrayUtils.toPrimitiveChar(result);
		} else {
			return ArrayUtils.EMPTY_CHAR_ARRAY;
		}
	}
	
	public static int[] readIntArray(final String jsonStr) throws IOException {
		if (!TextUtils.isEmpty(jsonStr)) {
			return readIntArray(JACKSON_JSON_FACTORY.createParser(jsonStr));
		} else {
			return null;
		}
	}
	
	public static long[] readLongArray(final JsonParser parser) throws IOException {
		moveToToken(parser);
		final boolean isOneObjectArray = parser.getCurrentToken() != JsonToken.START_ARRAY;
		
		if (parser.getCurrentToken() != JsonToken.VALUE_NULL) {
			final List<Long> result = new ArrayList<>();
			
			for (
				JsonToken token = parser.getCurrentToken();
				token != null && token != JsonToken.END_ARRAY;
				token = parser.nextToken()
				) {
				if (token == JsonToken.VALUE_NUMBER_INT) {
					result.add(tryParseLong(parser));
				}
				if (isOneObjectArray) {
					break;
				}
			}
			
			return ArrayUtils.toPrimitive(result.toArray(new Long[result.size()]));
		} else {
			return ArrayUtils.EMPTY_LONG_ARRAY;
		}
	}
	
	public static long[] readLongArray(final String jsonStr) throws IOException {
		if (!TextUtils.isEmpty(jsonStr)) {
			return readLongArray(JACKSON_JSON_FACTORY.createParser(jsonStr));
		} else {
			return null;
		}
	}
	
	public static float[] readFloatArray(final JsonParser parser) throws IOException {
		moveToToken(parser);
		final boolean isOneObjectArray = parser.getCurrentToken() != JsonToken.START_ARRAY;
		
		if (parser.getCurrentToken() != JsonToken.VALUE_NULL) {
			final List<Float> result = new ArrayList<>();
			
			for (
				JsonToken token = parser.getCurrentToken();
				token != null && token != JsonToken.END_ARRAY;
				token = parser.nextToken()
				) {
				if (token == JsonToken.VALUE_NUMBER_FLOAT) {
					result.add(tryParseFloat(parser));
				}
				if (isOneObjectArray) {
					break;
				}
			}
			
			return ArrayUtils.toPrimitive(result.toArray(new Float[result.size()]));
		} else {
			return ArrayUtils.EMPTY_FLOAT_ARRAY;
		}
	}
	
	public static float[] readFloatArray(final String jsonStr) throws IOException {
		if (!TextUtils.isEmpty(jsonStr)) {
			final JsonParser parser = JACKSON_JSON_FACTORY.createParser(jsonStr);
			return readFloatArray(parser);
		} else {
			return null;
		}
	}
	
	public static double[] readDoubleArray(final JsonParser parser) throws IOException {
		moveToToken(parser);
		final boolean isOneObjectArray = parser.getCurrentToken() != JsonToken.START_ARRAY;
		
		if (parser.getCurrentToken() != JsonToken.VALUE_NULL) {
			final List<Double> result = new ArrayList<>();
			
			for (
				JsonToken token = parser.getCurrentToken();
				token != null && token != JsonToken.END_ARRAY;
				token = parser.nextToken()
				) {
				if (token == JsonToken.VALUE_NUMBER_FLOAT) {
					result.add(tryParseDouble(parser));
				}
				if (isOneObjectArray) {
					break;
				}
			}
			
			return ArrayUtils.toPrimitive(result.toArray(new Double[result.size()]));
		} else {
			return ArrayUtils.EMPTY_DOUBLE_ARRAY;
		}
	}
	
	public static double[] readDoubleArray(final String jsonStr) throws IOException {
		if (!TextUtils.isEmpty(jsonStr)) {
			final JsonParser parser = JACKSON_JSON_FACTORY.createParser(jsonStr);
			return readDoubleArray(parser);
		} else {
			return null;
		}
	}
	
	public static boolean[] readBooleanArray(final JsonParser parser) throws IOException {
		moveToToken(parser);
		final boolean isOneObjectArray = parser.getCurrentToken() != JsonToken.START_ARRAY;
		
		if (parser.getCurrentToken() != JsonToken.VALUE_NULL) {
			final List<Boolean> result = new ArrayList<>();
			
			for (
				JsonToken token = parser.getCurrentToken();
				token != null && token != JsonToken.END_ARRAY;
				token = parser.nextToken()
				) {
				if (token == JsonToken.VALUE_FALSE) {
					result.add(Boolean.FALSE);
				} else if (token == JsonToken.VALUE_TRUE) {
					result.add(Boolean.TRUE);
				} else if (token == JsonToken.VALUE_NUMBER_INT) {
					result.add(tryParseBoolean(parser));
				}
				if (isOneObjectArray) {
					break;
				}
			}
			
			return ArrayUtils.toPrimitive(result.toArray(new Boolean[result.size()]));
		} else {
			return ArrayUtils.EMPTY_BOOLEAN_ARRAY;
		}
	}
	
	public static boolean[] readBooleanArray(final String jsonStr) throws IOException {
		if (!TextUtils.isEmpty(jsonStr)) {
			JsonParser parser = null;
			try {
				parser = JACKSON_JSON_FACTORY.createParser(jsonStr);
				return readBooleanArray(parser);
			} finally {
				if (parser != null) {
					parser.close();
				}
			}
		} else {
			return null;
		}
	}
	
	public static String[] readStringArray(final String json) throws IOException {
		return readArray(json, String.class);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T readEnum(final String value, final Class<T> enumType) throws IOException {
		final Map enumTokensMap = sEnumTokensMap.getEnumTokens((Class<? extends Enum<?>>) enumType);
		return enumTokensMap == null ? null : (T) enumTokensMap.get(value);
	}
	
	public static <T> T readEnum(final JsonParser parser, final Class<T> enumType) throws IOException {
		return readEnum(parser.getValueAsString(), enumType);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T readPrimitive(final Class<?> primitiveType, final JsonParser parser) throws IOException {
		moveToToken(parser);
		
		if (primitiveType == Boolean.class) {
			return (T) Boolean.valueOf(tryParseBoolean(parser));
		} else if (primitiveType == Integer.class) {
			return (T) Integer.valueOf(tryParseInteger(parser));
		} else if (primitiveType == Float.class) {
			return (T) Float.valueOf(tryParseFloat(parser));
		} else if (primitiveType == Double.class) {
			return (T) Double.valueOf(tryParseDouble(parser));
		} else if (primitiveType == Long.class) {
			return (T) Long.valueOf(tryParseLong(parser));
		} else {
			return null;
		}
	}
	
	public static String tryParseString(final JsonParser parser) throws IOException {
		return parser.getValueAsString();
	}
	
	public static int tryParseInteger(final JsonParser parser) throws IOException {
		try {
			return parser.getValueAsInt();
		} catch (final JsonParseException exception) {
			final String value = parser.getValueAsString();
			return ParseUtils.tryParseInt(value, DEFAULT_INT_VALUE);
		}
	}
	
	public static byte tryParseByte(final JsonParser parser) throws IOException {
		try {
			return (byte) parser.getValueAsInt();
		} catch (final JsonParseException exception) {
			final String value = parser.getValueAsString();
			return (byte) ParseUtils.tryParseInt(value, DEFAULT_INT_VALUE);
		}
	}
	
	public static float tryParseFloat(final JsonParser parser) throws IOException {
		try {
			return parser.getFloatValue();
		} catch (final JsonParseException exception) {
			final String value = parser.getValueAsString();
			return ParseUtils.tryParseFloat(value, DEFAULT_FLOAT_VALUE);
		}
	}
	
	private static double tryParseDouble(final JsonParser parser) throws IOException {
		try {
			return parser.getDoubleValue();
		} catch (final JsonParseException exception) {
			final String value = parser.getValueAsString();
			return ParseUtils.tryParseDouble(value, DEFAULT_DOUBLE_VALUE);
		}
	}
	
	public static long tryParseLong(final JsonParser jsonParser) throws IOException {
		try {
			return jsonParser.getLongValue();
		} catch (final JsonParseException exception) {
			final String value = jsonParser.getValueAsString();
			return ParseUtils.tryParseLong(value, DEFAULT_LONG_VALUE);
		}
	}
	
	@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
	public static boolean tryParseBoolean(final JsonParser parser) throws IOException {
		try {
			return parser.getBooleanValue();
		} catch (final JsonParseException exception) {
			return tryParseFloat(parser) != DEFAULT_FLOAT_VALUE;
		}
	}
	
	public static long tryParseTimeStamp(final String time) {
		final Date date = time != null ? DateUtils.parseIviDate(time) : null;
		
		return DateUtils.getTimestamp(date);
	}
	
	public static long tryParseIso8601Timestamp(final String time) {
		final Date date = time == null ? null : DateUtils.parseIso8601Date(time);
		
		return DateUtils.getTimestamp(date);
	}
	
}
