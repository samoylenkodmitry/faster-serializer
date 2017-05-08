package android.samutils.fasterserializer.processor;

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
				if (value == null) {
					continue;
				}
				
				// add json fields
				Set<Element> data = dataClasses.get(className);
				if (data == null) {
					data = new HashSet<>();
					dataClasses.put(className, data);
				}
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
			imports1.add("android.samutils.fasterserializer.mapping.value.ValueMap");
			
			final StringBuilder builderValueMapFiller = new StringBuilder();
			
			builderValueMapFiller
				.append("\t@Override\n")
				.append("\tpublic void fill() {\n");
			
			for (final String className : objectMapFiles.keySet()) {
				builderValueMapFiller.append("\t\tmValues.put(").append(className).append(".class, new ").append(objectMapFiles.get(className)).append("());\n");
			}
			
			builderValueMapFiller
				.append("\t}\n");
			
			if (!dataClasses.isEmpty()) {
				FileGeneratorHelper.writeFile(imports1, builderValueMapFiller.toString(),
					VALUE_MAP_FILE_NAME, "ValueMap", processingEnvironment.getFiler());
			}
			
			// add EnumTokensMap class
			
			final Collection<String> imports2 = new HashSet<>();
			imports2.add("android.samutils.fasterserializer.mapping.value.EnumTokensMap");
			
			final StringBuilder builderEnumTokensMapFiller = new StringBuilder();
			
			builderEnumTokensMapFiller
				.append("\t@Override\n")
				.append("\tpublic void fill() {\n");
			
			for (final String className : enumClasses) {
				builderEnumTokensMapFiller.append("\t\taddEnumTokens(").append(className).append(".class);\n");
			}
			
			builderEnumTokensMapFiller
				.append("\t}\n");
			
			if (!enumClasses.isEmpty()) {
				FileGeneratorHelper.writeFile(imports2, builderEnumTokensMapFiller.toString(),
					ENUM_TOKENS_FILE_NAME, "EnumTokensMap", processingEnvironment.getFiler());
			}
			
			// add UniqueFieldsMap
			
			final Collection<String> imports3 = new HashSet<>();
			imports3.add("android.samutils.fasterserializer.mapping.value.UniqueFieldsMap");
			imports3.add("android.samutils.fasterserializer.mapping.value.UniqueKey");
			
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
						.append("+\"_\"+");
				}
				stringBuilder
					.append("\"")
					.append(classWithUniqueFields)
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
			
			if (!enumClasses.isEmpty()) {
				FileGeneratorHelper.writeFile(imports3, builderUniqueFieldsMapFiller.toString(),
					UNIQUE_FIELDS_FILE_NAME, "UniqueFieldsMap", processingEnvironment.getFiler());
			}
		}
	}
	
	private static String generateObjectMap(final String className, final Iterable<Element> fields, final ProcessingEnvironment processingEnvironment) {
		
		final Collection<String> imports = new ArrayList<>();
		imports.add(java.io.IOException.class.getName());
		imports.add(java.util.Map.class.getName());
		imports.add("android.samutils.fasterserializer.mapping.JacksonJsoner.ObjectMap");
		imports.add("android.samutils.fasterserializer.mapping.JacksonJsoner.IFieldInfo");
		imports.add("android.samutils.fasterserializer.mapping.JacksonJsoner.FieldInfo");
		imports.add("android.samutils.fasterserializer.mapping.JacksonJsoner.FieldInfoInt");
		imports.add("android.samutils.fasterserializer.mapping.JacksonJsoner.FieldInfoLong");
		imports.add("android.samutils.fasterserializer.mapping.JacksonJsoner.FieldInfoDouble");
		imports.add("android.samutils.fasterserializer.mapping.JacksonJsoner.FieldInfoFloat");
		imports.add("android.samutils.fasterserializer.mapping.JacksonJsoner.FieldInfoBoolean");
		imports.add("android.samutils.fasterserializer.mapping.JacksonJsoner.FieldInfoByte");
		imports.add("android.samutils.fasterserializer.mapping.JacksonJsoner");
		imports.add("android.samutils.fasterserializer.mapping.Serializer");
		imports.add("android.samutils.utils.ArrayUtils");
		imports.add("java.util.Collection");
		
		imports.add("com.fasterxml.jackson.core.JsonParser");
		imports.add("com.fasterxml.jackson.databind.JsonNode");
		imports.add("android.os.Parcel");
		
		
		final StringBuilder builder = new StringBuilder();
		
		builder
			.append(" @Override public <T> T create(final Class<T> cls) { return (T) new ").append(className).append("(); }\n\n ")
			.append(" @Override public <T> T[] createArray(final int count) { return (T[]) new ").append(className).append("[count]; }\n\n ")
			.append("\t@Override\n")
			.append("\tpublic void fill() {\n");
		
		Map<String, String> allFields = new HashMap<>();
		
		Map<String, String>versions = new HashMap<>();
		for (final Element element : fields) {
			
			final TypeKind typeKind = element.asType().getKind();
			
			final String fieldName = element.getSimpleName().toString();
			final String fieldType = getFieldType(element);
			final String className1 = element.getEnclosingElement().asType().toString().replaceAll("<.*?>", "");
			final String fieldInfoClassName = getFieldFieldInfoClassName(typeKind, className1, fieldType);
			final String readJsonMethod = "obj." + fieldName + " = " + getGetJsonMethod(element) + ";";
			final String readParcelMethod = "obj." + fieldName + " = " + getGetParcelMethod(element) + ";";
			final String writeParcelMethod = getWriteParcel(element)
				.replace("?", "obj." + fieldName)
				.replace("%", "obj." + fieldName)
				.replace("+","?")+";";
			final String valueKey = element.getAnnotation(Value.class).jsonKey();
			final String jsonKey = valueKey == null || valueKey.length() == 0 ? fieldName : valueKey;
			
			String addFieldCall = (String.format(Locale.getDefault(),
				"\n\t\taddField(\"%1s\", %2s{\n" +
					"\t\t\t\t\n" +
					"\t\t\t\t@Override\n" +
					"\t\t\t\tpublic void read(final %3s obj, final JsonParser json, final JsonNode source) throws IOException {\n" +
					"\t\t\t\t\t\t%4s\n" +
					"\t\t\t\t}\n" +
					"\t\t\t\t\n" +
					"\t\t\t\t@Override\n" +
					"\t\t\t\tpublic void read(final %5s obj, final Parcel parcel) {\n" +
					"\t\t\t\t\t\t%6s\n" +
					"\t\t\t\t}\n" +
					"\t\t\t\t\n" +
					"\t\t\t\t@Override\n" +
					"\t\t\t\tpublic void write(final %7s obj, final Parcel parcel) {\n" +
					"\t\t\t\t\t\t%8s\n" +
					"\t\t\t\t}\n" +
					"\t\t\t\t@Override\n" +
					"\t\t\t\tpublic String getName()  {\n" +
					"\t\t\t\t\t\treturn \"%9s\";\n" +
					"\t\t\t\t}\n" +
					
					"\t\t});\n\n",
				jsonKey, fieldInfoClassName, className1, readJsonMethod, className1, readParcelMethod, className1, writeParcelMethod,className1+"."+jsonKey));
			
			allFields.put(jsonKey, addFieldCall);
			versions.put(jsonKey, fieldType);
		}
		final List<String> allKeys = new ArrayList<>(allFields.keySet());
		Collections.sort(allKeys);
		StringBuilder versionBuilder = new StringBuilder();
		for (int i = 0; i < allKeys.size(); i++) {
			builder.append(allFields.get(allKeys.get(i)));
			versionBuilder.append(versions.get(allKeys.get(i)));
		}
		
		builder
			.append("\t}\n");
		builder
			.append(" @Override public int getCurrentVersion() { return ").append(versionBuilder.toString().hashCode()).append("; }\n\n ");
		
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
	
	private static String getGetJsonMethod(final Element field) {
		String type = field.asType().toString();
		final TypeKind typeKind = field.asType().getKind();
		if (type.equals(java.lang.String.class.getName())) {
			return "json.getValueAsString()";
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
			return "parcel.readString()";
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
