package ru.ivi.processor;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;

public final class FileGeneratorHelper {

	protected static void writeFile(final Iterable<String> imports, final String fileBody,
		final String fileName, final String extendsFileName, final Filer filer)
	{
		final StringBuilder builder = new StringBuilder("package ru.ivi.processor;\n\n");

		for (final String importString : imports) {
			builder.append("import ").append(importString).append(";\n");
		}

		builder
			.append("\n")
			.append("public final class ").append(fileName);

		if (extendsFileName != null) {
			builder
				.append(" extends ")
				.append(extendsFileName);
		}

		builder
			.append(" {\n\n") // open class
			.append(fileBody).append("\n")
			.append("}\n"); // close class

		try { // write the file
			final JavaFileObject source = filer.createSourceFile("ru.ivi.processor." + fileName);

			final Writer writer = source.openWriter();
			writer.write(builder.toString());
			writer.flush();
			writer.close();
		} catch (final IOException e) {
			// Note: calling e.printStackTrace() will print IO errors
			// that occur from the file already existing after its first run, this is normal
		}
	}

	protected static void writeFile(final Iterable<String> imports, final String fileBody,
		final String fileName, final Filer filer)
	{
		writeFile(imports, fileBody, fileName, null, filer);
	}
}
