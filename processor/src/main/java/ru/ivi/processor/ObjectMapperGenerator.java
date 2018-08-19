package ru.ivi.processor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;


public final class ObjectMapperGenerator {
	
	private static final Map<TypeKind, String> TYPE_KIND_TO_CLASS_NAME = new EnumMap<TypeKind, String>(TypeKind.class) {{
		put(TypeKind.BOOLEAN, boolean.class.getSimpleName());
		put(TypeKind.INT, int.class.getSimpleName());
		put(TypeKind.DOUBLE, double.class.getSimpleName());
		put(TypeKind.FLOAT, float.class.getSimpleName());
		put(TypeKind.LONG, long.class.getSimpleName());
		put(TypeKind.BYTE, byte.class.getSimpleName());
	}};
	
	private static final Map<TypeKind, String> TYPE_KIND_TO_GET_METHOD = new EnumMap<TypeKind, String>(TypeKind.class) {{
		put(TypeKind.BOOLEAN, "JacksonJsoner.tryParseBoolean(json)");
		put(TypeKind.INT, "JacksonJsoner.tryParseInteger(json)");
		put(TypeKind.DOUBLE, "JacksonJsoner.tryParseDouble(json)");
		put(TypeKind.FLOAT, "JacksonJsoner.tryParseFloat(json)");
		put(TypeKind.LONG, "JacksonJsoner.tryParseLong(json)");
		put(TypeKind.BYTE, "JacksonJsoner.tryParseByte(json)");
		put(TypeKind.DECLARED, "JacksonJsoner.readObject(json,source,?.class)");
	}};
	private static final Map<TypeKind, String> PARCEL_TYPE_KIND_TO_GET_METHOD = new EnumMap<TypeKind, String>(TypeKind.class) {{
		put(TypeKind.BOOLEAN, "parcel.readByte() == (byte)1");
		put(TypeKind.INT, "parcel.readInt()");
		put(TypeKind.DOUBLE, "parcel.readDouble()");
		put(TypeKind.FLOAT, "parcel.readFloat()");
		put(TypeKind.LONG, "parcel.readLong()");
		put(TypeKind.BYTE, "parcel.readByte()");
		put(TypeKind.DECLARED, "Serializer.read(parcel, ?.class)");
	}};
	private static final Map<TypeKind, String> PARCEL_TYPE_KIND_TO_WRITE_METHOD = new EnumMap<TypeKind, String>(TypeKind.class) {{
		put(TypeKind.BOOLEAN, "parcel.writeByte(?+(byte)1:(byte)0)");
		put(TypeKind.INT, "parcel.writeInt(?)");
		put(TypeKind.DOUBLE, "parcel.writeDouble(?)");
		put(TypeKind.FLOAT, "parcel.writeFloat(?)");
		put(TypeKind.LONG, "parcel.writeLong(?)");
		put(TypeKind.BYTE, "parcel.writeByte(?)");
		put(TypeKind.DECLARED, "Serializer.write(parcel, ?, _.class)");
	}};
	
	private static final Map<String, String> TYPE_KIND_TO_ARRAY_GET_METHOD = new HashMap<String, String>() {{
		put(boolean.class.getSimpleName(), "JacksonJsoner.readBooleanArray(json)");
		put(int.class.getSimpleName(), "JacksonJsoner.readIntArray(json)");
		put(byte.class.getSimpleName(), "JacksonJsoner.readByteArray(json)");
		put(char.class.getSimpleName(), "JacksonJsoner.readCharArray(json)");
		put(long.class.getSimpleName(), "JacksonJsoner.readLongArray(json)");
		put(float.class.getSimpleName(), "JacksonJsoner.readFloatArray(json)");
		put(double.class.getSimpleName(), "JacksonJsoner.readDoubleArray(json)");
		put(Enum.class.getSimpleName(), "JacksonJsoner.readEnumArray(json,?.class)");
		put(Array.class.getSimpleName(), "JacksonJsoner.<?>readArray(json, source, ?.class).toArray(new ?[0])");
	}};
	
	private static final Map<String, String> PARCEL_TYPE_KIND_TO_ARRAY_GET_METHOD = new HashMap<String, String>() {{
		put(boolean.class.getSimpleName(), "Serializer.readBooleanArray(parcel)");
		put("java.lang.String", "Serializer.readStringArray(parcel)");
		put(int.class.getSimpleName(), "Serializer.readIntArray(parcel)");
		put(byte.class.getSimpleName(), "Serializer.readByteArray(parcel)");
		put(char.class.getSimpleName(), "Serializer.readCharArray(parcel)");
		put(long.class.getSimpleName(), "Serializer.readLongArray(parcel)");
		put(float.class.getSimpleName(), "Serializer.readFloatArray(parcel)");
		put(double.class.getSimpleName(), "Serializer.readDoubleArray(parcel)");
		put(Enum.class.getSimpleName(), "Serializer.readEnumArray(parcel, ?.class)");
		put(Array.class.getSimpleName(), "Serializer.<?>readArray(parcel, ?.class)");
	}};
	
	private static final Map<String, String> PARCEL_TYPE_KIND_TO_ARRAY_WRITE_METHOD = new HashMap<String, String>() {{
		put(boolean.class.getSimpleName(), "Serializer.writeBooleanArray(parcel, ?)");
		put("java.lang.String", "Serializer.writeStringArray(parcel, ?)");
		put(int.class.getSimpleName(), "Serializer.writeIntArray(parcel, ?)");
		put(byte.class.getSimpleName(), "Serializer.writeByteArray(parcel, ?)");
		put(char.class.getSimpleName(), "Serializer.writeCharArray(parcel, ?)");
		put(long.class.getSimpleName(), "Serializer.writeLongArray(parcel, ?)");
		put(float.class.getSimpleName(), "Serializer.writeFloatArray(parcel, ?)");
		put(double.class.getSimpleName(), "Serializer.writeDoubleArray(parcel, ?)");
		put(Enum.class.getSimpleName(), "Serializer.writeEnumArray(parcel, %, ?.class)");
		put(Array.class.getSimpleName(), "Serializer.writeArray(parcel, %, ?.class)");
	}};
	
	private static final Map<TypeKind, String> TYPE_KIND_TO_FIELD_INFO_CLASS = new EnumMap<TypeKind, String>(TypeKind.class) {{
		put(TypeKind.BOOLEAN, "FieldInfoBoolean");
		put(TypeKind.INT, "FieldInfoInt");
		put(TypeKind.DOUBLE, "FieldInfoDouble");
		put(TypeKind.FLOAT, "FieldInfoFloat");
		put(TypeKind.LONG, "FieldInfoLong");
		put(TypeKind.BYTE, "FieldInfoByte");
		put(TypeKind.ARRAY, "FieldInfo");
		put(TypeKind.DECLARED, "FieldInfo");
	}};
	
	private static final String ABSTRACT_FILE_NAME = "ObjectMap";
	private static final String VALUE_MAP_FILE_NAME = "ValueMapFiller";
	private static final String ENUM_TOKENS_FILE_NAME = "EnumTokensMapFiller";
	private static final String UNIQUE_FIELDS_FILE_NAME = "UniqueFieldsMapFiller";
	
	public ObjectMapperGenerator(final Collection<? extends Element> elements, final ProcessingEnvironment processingEnvironment) {
		
		System.out.println("Value: " + elements);
		if (elements != null && !elements.isEmpty()) {
			
			final Map<String, Set<Element>> dataClasses = new HashMap<>();
			final Collection<String> enumClasses = new ArrayList<>();
			final Map<String, List<Element>> uniqueFields = new HashMap<>();
			
			for (final Element element : elements) {

//				if (EXCLUDE_TYPE_KINDS.contains(element.asType().getKind())) {
//					continue;
//				}
				
				final String className = element.getEnclosingElement().asType().toString().replaceAll("<.*?>", "");
				final Value value = element.getAnnotation(Value.class);
				
				// add unique fields
				if (value.uniqueField()) {
					List<Element> unique = uniqueFields.get(className);
					if (unique == null) {
						unique = new ArrayList<>();
					}
					unique.add(element);
					uniqueFields.put(className, unique);
				}
				
				// skip non json fields
				if (value == null || value.skipReadWrite()) {
					continue;
				}
				
				// add json fields
				final Set<Element> data = dataClasses.computeIfAbsent(className, (s) -> new HashSet<>());
				data.add(element);
				
				// add enum tokens
				final String elementName = element.asType().toString().replace("[", "").replace("]", "");
				if (value.fieldIsEnum()) {
					enumClasses.add(elementName);
				}
			}
			
			// create ObjectMap classes
			
			final Map<String, String> objectMapFiles = new HashMap<>();
			
			for (final String className : dataClasses.keySet()) {
				final Set<Element> fields = dataClasses.get(className);
				final String fileName = generateObjectMap(className, fields, processingEnvironment);
				
				objectMapFiles.put(className, fileName);
			}
			
			// add ValueMapFiller class
			
			final Collection<String> imports1 = new HashSet<>();
			imports1.add("ru.ivi.mapping.value.ValueMap");
			imports1.add("java.util.HashMap");
			imports1.add("ru.ivi.mapping.ObjectMap");
			imports1.add("ru.ivi.mapping.IFieldInfo");
			
			final StringBuilder builderValueMapFiller = new StringBuilder();
			
			builderValueMapFiller
				.append("\t@Override\n")
				.append("\tpublic void fill(final HashMap<Class<?>, ObjectMap<String, IFieldInfo>> mValues) {\n");
			
			for (final String className : objectMapFiles.keySet()) {
				builderValueMapFiller.append("\t\tmValues.put(").append(className).append(".class, new ").append(objectMapFiles.get(className)).append("());\n");
			}
			
			builderValueMapFiller
				.append("\t}\n");
			
			FileGeneratorHelper.writeFile(imports1, builderValueMapFiller.toString(),
				VALUE_MAP_FILE_NAME, "ValueMap", processingEnvironment.getFiler());
			
			// add EnumTokensMap class
			
			final Collection<String> imports2 = new HashSet<>();
			imports2.add("ru.ivi.mapping.value.EnumTokensMap");
			
			final StringBuilder builderEnumTokensMapFiller = new StringBuilder();
			
			builderEnumTokensMapFiller
				.append("\t@Override\n")
				.append("\tpublic void fill() {\n");
			
			for (final String className : enumClasses) {
				builderEnumTokensMapFiller.append("\t\taddEnumTokens(").append(className).append(".class);\n");
			}
			
			builderEnumTokensMapFiller
				.append("\t}\n");
			
			FileGeneratorHelper.writeFile(imports2, builderEnumTokensMapFiller.toString(),
				ENUM_TOKENS_FILE_NAME, "EnumTokensMap", processingEnvironment.getFiler());
			
			// add UniqueFieldsMap
			
			final Collection<String> imports3 = new HashSet<>();
			imports3.add("ru.ivi.mapping.value.UniqueFieldsMap");
			imports3.add("ru.ivi.mapping.value.UniqueKey");
			
			final StringBuilder builderUniqueFieldsMapFiller = new StringBuilder();
			
			builderUniqueFieldsMapFiller
				.append("\t@Override\n")
				.append("\tpublic void fill() {\n");
			
			for (final String classWithUniqueFields : uniqueFields.keySet()) {
				final List<Element> fields = uniqueFields.get(classWithUniqueFields);
				
				Collections.sort(fields, new Comparator<Element>() {
					
					@Override
					public int compare(final Element element, final Element t1) {
						return element.getSimpleName().toString().compareTo(t1.getSimpleName().toString());
					}
				});
				
				final StringBuilder stringBuilder = new StringBuilder();
				for (final Element element : fields) {
					stringBuilder
						.append("object.")
						.append(element.getSimpleName())
						.append("+");
				}
				final String[] classNameSplit = classWithUniqueFields.split("\\.");
				final String simpleClassName = classNameSplit[classNameSplit.length-1];
				stringBuilder
					.append("\"")
					.append(simpleClassName)
					.append("\"");
				
				builderUniqueFieldsMapFiller
					.append("\t\taddUniqueKey(").append(classWithUniqueFields).append(".class, new UniqueKey<").append(classWithUniqueFields).append(">() {\n")
					.append("\t\t\t@Override\n")
					.append("\t\t\tpublic String getUniqueKey(final ").append(classWithUniqueFields).append(" object) {\n")
					.append("\t\t\t\treturn ").append(stringBuilder.toString()).append(";\n")
					.append("\t\t\t}\n")
					.append("\t\t});\n");
			}
			
			builderUniqueFieldsMapFiller
				.append("\t}\n");
			
			FileGeneratorHelper.writeFile(imports3, builderUniqueFieldsMapFiller.toString(),
				UNIQUE_FIELDS_FILE_NAME, "UniqueFieldsMap", processingEnvironment.getFiler());
		}
	}
	
	private static String generateObjectMap(final String className, final Iterable<Element> fields, final ProcessingEnvironment processingEnvironment) {
		
		final Collection<String> imports = new ArrayList<>();
		imports.add(java.io.IOException.class.getName());
		imports.add(java.util.Map.class.getName());
		imports.add(java.util.Arrays.class.getName());
		imports.add("ru.ivi.mapping.ObjectMap");
		imports.add("ru.ivi.mapping.IFieldInfo");
		imports.add("ru.ivi.mapping.JacksonJsoner.FieldInfo");
		imports.add("ru.ivi.mapping.JacksonJsoner.FieldInfoInt");
		imports.add("ru.ivi.mapping.JacksonJsoner.FieldInfoLong");
		imports.add("ru.ivi.mapping.JacksonJsoner.FieldInfoDouble");
		imports.add("ru.ivi.mapping.JacksonJsoner.FieldInfoFloat");
		imports.add("ru.ivi.mapping.JacksonJsoner.FieldInfoBoolean");
		imports.add("ru.ivi.mapping.JacksonJsoner.FieldInfoByte");
		imports.add("ru.ivi.mapping.JacksonJsoner");
		imports.add("ru.ivi.mapping.Serializer");
		imports.add("ru.ivi.mapping.Copier");
		imports.add("ru.ivi.utils.ArrayUtils");
		imports.add("java.util.Collection");
		imports.add("java.util.Map");
		imports.add("com.fasterxml.jackson.core.JsonParser");
		imports.add("com.fasterxml.jackson.databind.JsonNode");
		imports.add("ru.ivi.mapping.Parcel");
		
		
		final StringBuilder builder = new StringBuilder()
			.append("\t@Override public <T> T create(final Class<T> cls) { return (T) new ").append(className).append("(); }\n\n ")
			.append("\t@Override public <T> T[] createArray(final int count) { return (T[]) new ").append(className).append("[count]; }\n\n ")
			.append("\t@Override\n")
			.append("\tpublic void fill(final Map mMap) {\n");
		
		final Map<String, String> allFields = new HashMap<>();
		
		final Map<String, String> versions = new HashMap<>();
		for (final Element element : fields) {
			
			final TypeKind typeKind = element.asType().getKind();
			
			final String fieldName = element.getSimpleName().toString();
			final String fieldType = getFieldType(element);
			final String className1 = element.getEnclosingElement().asType().toString().replaceAll("<.*?>", "");
			final String fieldInfoClassName = getFieldFieldInfoClassName(typeKind, className1, fieldType);
			final String readJsonMethod = "obj." + fieldName + " = " + getGetJsonMethod(element)
				.replace("?", ";if(obj." + fieldName + " != null) obj." + fieldName + " = obj." + fieldName + ".intern()")
				+ ";";
			final String readParcelMethod = "obj." + fieldName + " = " + getGetParcelMethod(element)
				.replace("?", ";if(obj." + fieldName + " != null) obj." + fieldName + " = obj." + fieldName + ".intern()")
				+ ";";
			final String writeParcelMethod = getWriteParcel(element)
				.replace("?", "obj." + fieldName)
				.replace("%", "obj." + fieldName)
				.replace("+","?")+";";
			final String cloneMethod = "result." + fieldName + " = " + getCloneMethod(element) + ";";
			final String valueKey = element.getAnnotation(Value.class).jsonKey();
			final String jsonKey = valueKey == null || valueKey.length() == 0 ? fieldName : valueKey;
			
			final String addFieldCall = (String.format(Locale.getDefault(),
				"\n\t\tmMap.put(\"%1$s\", %2$s{\n" +
					"\t\t\t\t\n" +
					"\t\t\t\t@Override\n" +
					"\t\t\t\tpublic void read(final %3$s obj, final JsonParser json, final JsonNode source) throws IOException {\n" +
					"\t\t\t\t\t\t%4$s\n" +
					"\t\t\t\t}\n" +
					"\t\t\t\t\n" +
					"\t\t\t\t@Override\n" +
					"\t\t\t\tpublic void read(final %3$s obj, final Parcel parcel) {\n" +
					"\t\t\t\t\t\t%5$s\n" +
					"\t\t\t\t}\n" +
					"\t\t\t\t\n" +
					"\t\t\t\t@Override\n" +
					"\t\t\t\tpublic void write(final %3$s obj, final Parcel parcel) {\n" +
					"\t\t\t\t\t\t%6$s\n" +
					"\t\t\t\t}\n" +
					"\t\t\t\t@Override\n" +
					"\t\t\t\tpublic void clone(final %3$s result, final %3$s objToClone) {\n" +
					"\t\t\t\t\t\t%7$s\n" +
					"\t\t\t\t}\n" +
					"\t\t\t\t@Override\n" +
					"\t\t\t\tpublic String getName()  {\n" +
					"\t\t\t\t\t\treturn \"%8$s\";\n" +
					"\t\t\t\t}\n" +
					"\t\t});\n\n",
				jsonKey, fieldInfoClassName, className1, readJsonMethod, readParcelMethod, writeParcelMethod, cloneMethod, className1+"."+jsonKey));
			
			allFields.put(jsonKey, addFieldCall);
			versions.put(jsonKey, fieldType);
		}
		final List<String> allKeys = new ArrayList<>(allFields.keySet());
		Collections.sort(allKeys);
		final StringBuilder versionBuilder = new StringBuilder();
		for (int i = 0; i < allKeys.size(); i++) {
			builder.append(allFields.get(allKeys.get(i)));
			versionBuilder.append(versions.get(allKeys.get(i)));
		}
		
		builder
			.append("\t}\n");
		builder
			.append("\t@Override public int getCurrentVersion() { return ").append(versionBuilder.toString().hashCode()).append("; }\n\n ");
		
		final String[] packages = className.split("\\.");
		final String fileName = packages[packages.length - 1] + ABSTRACT_FILE_NAME;
		
		final String extendsFileName = ABSTRACT_FILE_NAME + "<String, IFieldInfo>";
		
		FileGeneratorHelper.writeFile(imports, builder.toString(), fileName, extendsFileName, processingEnvironment.getFiler());
		
		return fileName;
	}
	
	private static String getFieldFieldInfoClassName(final TypeKind fieldTypeKind, final String className, final String fieldType) {
		
		final StringBuilder builder = new StringBuilder("new ")
			.append(TYPE_KIND_TO_FIELD_INFO_CLASS.get(fieldTypeKind))
			.append("<")
			.append(className);
		
		if (!fieldTypeKind.isPrimitive()) {
			builder.append(", ").append(fieldType);
		}
		
		builder.append(">()");
		
		return builder.toString();
	}
	
	private static String getFieldType(final Element field) {
		final TypeKind fieldTypeKind = field.asType().getKind();
		
		if (!fieldTypeKind.isPrimitive()) {
			return field.asType().toString();
		} else {
			return TYPE_KIND_TO_CLASS_NAME.get(fieldTypeKind);
		}
	}
	
	private static boolean isPrimitive(final String type) {
		return type.equals(boolean.class.toString())
			|| type.equals(byte.class.toString())
			|| type.equals(short.class.toString())
			|| type.equals(int.class.toString())
			|| type.equals(long.class.toString())
			|| type.equals(char.class.toString())
			|| type.equals(float.class.toString())
			|| type.equals(double.class.toString())
			|| type.equals(String.class.toString());
	}
	
	private static String getCloneMethod(final Element field) {
		final String fieldName = field.getSimpleName().toString();
		final String objectField = "objToClone." + fieldName;
		String type = field.asType().toString();
		if (type.equals(java.lang.String.class.getName())) {
			return objectField;
		}
		final TypeKind typeKind = field.asType().getKind();
		if (typeKind.isPrimitive()) {
			return objectField;
		}
		if (typeKind == TypeKind.ARRAY) {
			type = type.replace("[", "").replace("]", "");
			if (isPrimitive(type)) {
				return objectField + " == null ? null : Arrays.copyOf(" + objectField + ", " + objectField + ".length)";
			} else {
				return "Copier.cloneArray(" + objectField + ", " + type.concat(".class") + ")";
			}
		}
		if (field.getAnnotation(Value.class).fieldIsEnum()) {
			return "objToClone." + fieldName;
		} else {
			return "Copier.cloneObject(" + objectField + ", " + type.concat(".class") + ")";
		}
	}
	
	private static String getGetJsonMethod(final Element field) {
		String type = field.asType().toString();
		final TypeKind typeKind = field.asType().getKind();
		if (type.equals(java.lang.String.class.getName())) {
			return "json.getValueAsString()?";
		}
		if (field.getAnnotation(Value.class).fieldIsEnum()) {
			if (typeKind == TypeKind.ARRAY) {
				type = type.replace("[", "").replace("]", "");
				return TYPE_KIND_TO_ARRAY_GET_METHOD.get(Enum.class.getSimpleName()).replace("?", type);
			} else {
				return "JacksonJsoner.readEnum(json.getValueAsString(), ?.class);".replace("?", type);
			}
		} else {
			if (typeKind == TypeKind.ARRAY) {
				type = type.replace("[", "").replace("]", "");
				if (TYPE_KIND_TO_ARRAY_GET_METHOD.containsKey(type)) {
					return TYPE_KIND_TO_ARRAY_GET_METHOD.get(type);
				} else {
					return TYPE_KIND_TO_ARRAY_GET_METHOD.get(Array.class.getSimpleName()).replace("?", type);
				}
			} else {
				return TYPE_KIND_TO_GET_METHOD.get(typeKind).replace("?", type);
			}
		}
	}
	
	private static String getGetParcelMethod(final Element field) {
		String type = field.asType().toString();
		final TypeKind typeKind = field.asType().getKind();
		if (type.equals(java.lang.String.class.getName())) {
			return "parcel.readString()?";
		}
		if (field.getAnnotation(Value.class).fieldIsEnum()) {
			if (typeKind == TypeKind.ARRAY) {
				type = type.replace("[", "").replace("]", "");
				return PARCEL_TYPE_KIND_TO_ARRAY_GET_METHOD.get(Enum.class.getSimpleName()).replace("?", type);
			} else {
				return "Serializer.readEnum(parcel, ?.class)".replace("?", type);
			}
		} else {
			if (typeKind == TypeKind.ARRAY) {
				type = type.replace("[", "").replace("]", "");
				if (PARCEL_TYPE_KIND_TO_ARRAY_GET_METHOD.containsKey(type)) {
					return PARCEL_TYPE_KIND_TO_ARRAY_GET_METHOD.get(type);
				} else {
					return PARCEL_TYPE_KIND_TO_ARRAY_GET_METHOD.get(Array.class.getSimpleName()).replace("?", type);
				}
			} else {
				return PARCEL_TYPE_KIND_TO_GET_METHOD.get(typeKind).replace("?", type);
			}
		}
	}
	
	private static String getWriteParcel(final Element field) {
		String type = field.asType().toString();
		final TypeKind typeKind = field.asType().getKind();
		if (type.equals(java.lang.String.class.getName())) {
			return "parcel.writeString(?)";
		}
		if (field.getAnnotation(Value.class).fieldIsEnum()) {
			if (typeKind == TypeKind.ARRAY) {
				type = type.replace("[", "").replace("]", "");
				return PARCEL_TYPE_KIND_TO_ARRAY_WRITE_METHOD.get(Enum.class.getSimpleName()).replace("?", type);
			} else {
				return "Serializer.writeEnum(parcel, ?)";
			}
		} else {
			if (typeKind == TypeKind.ARRAY) {
				type = type.replace("[", "").replace("]", "");
				if (PARCEL_TYPE_KIND_TO_ARRAY_WRITE_METHOD.containsKey(type)) {
					return PARCEL_TYPE_KIND_TO_ARRAY_WRITE_METHOD.get(type);
				} else {
					return PARCEL_TYPE_KIND_TO_ARRAY_WRITE_METHOD.get(Array.class.getSimpleName()).replace("?", type);
				}
			} else {
				return PARCEL_TYPE_KIND_TO_WRITE_METHOD.get(typeKind).replace("_", type);
			}
		}
	}
}
