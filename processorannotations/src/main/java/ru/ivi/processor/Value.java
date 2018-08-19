package ru.ivi.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {

	String jsonKey() default EMPTY_KEY;
	boolean serverField() default false;
	
	Class[] alternatives() default EMPTY_CLASS.class;

	boolean containsValues() default false;

	boolean fieldIsEnum() default false;

	boolean uniqueField() default false;
	
	boolean skipReadWrite() default false;

	String EMPTY_KEY = "";
	class EMPTY_CLASS{}

}
