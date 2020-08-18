Partial copyright by Sergei Borisov odysseos.ithakis@gmail.com

# faster-serializer

https://android-arsenal.com/details/1/6511

Android library for faster POJO de|serialization and Json parsing

How to use library 'faster-serializer' (https://github.com/s-a--m/Fasterserializerdemo)

1. In module-app gradle after 'apply plugin...' insert:

```gradle
repositories {
	maven {
		url 'https://dl.bintray.com/dmitrysamoylenko/fasterserializer/'
	}
}
```

2. In android section insert annotation processor info:
```gradle
android {
	packagingOptions {
		exclude 'META-INF/license.txt'
		exclude 'META-INF/LICENSE'
	}
...
	defaultConfig {
...
		javaCompileOptions {
			annotationProcessorOptions {
				className 'ru.ivi.processor.AnnotationProcessor'
			}
		}

	}
```
3. In dependencies:
```gradle
dependencies {
...
	compile 'android.samutils:faster-serializer:+'// 1.0.1 for current time
	compile 'android.samutils:processorannotations:+'
	annotationProcessor 'android.samutils:processor:+'

}
```

Usage:

1. Create POJO

```java
public class MyPojo {

  @Value
  public String someString;
  @Value
  public int someInt;
  @Value
  public MyPojo someOtherData;

}
```

Note: each annotated field must be non-final public

2. Initialize Serializer before usage:

```java
		Serializer.initialize();
```

3. Use public method of Serializer:

```java
		MyPojo testPojo = new MyPojo();
		testPojo.someString = "some string";
		testPojo.someInt = 1337;

		Log.d("serializer test", "input pojo to json: "+Jsoner.toString(testPojo));

		//test write
		byte[] bytesOut = Serializer.toBytes(testPojo);

		Log.d("serializer test", "bytes count: "+bytesOut.length);

		//test read
		MyPojo testPojoOut = Serializer.read(bytesOut);

		Log.d("serializer test", "out pojo: "+ Jsoner.toString(testPojoOut));
```

```log
05-08 19:04:17.164 917-917/? D/serializer test: input pojo to json: {"__class__":"android.samutils.fasterserializerdemo.MyPojo","someInt":1337,"someString":"some string"}

05-08 19:04:17.166 917-917/? D/serializer test: bytes count: 144

05-08 19:04:17.166 917-917/? D/serializer test: out pojo: {"__class__":"android.samutils.fasterserializerdemo.MyPojo","someInt":1337,"someString":"some string"}
```
