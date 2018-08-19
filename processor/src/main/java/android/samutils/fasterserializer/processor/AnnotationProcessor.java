package android.samutils.fasterserializer.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;


@SupportedAnnotationTypes({"android.samutils.fasterserializer.processor.Value"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class AnnotationProcessor extends AbstractProcessor {

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment) {
		
		System.out.println(":AnnotationProcessor");

		new android.samutils.fasterserializer.processor.ObjectMapperGenerator(roundEnvironment.getElementsAnnotatedWith(Value.class), processingEnv);
		
		return true;
	}
}
