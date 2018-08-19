package android.samutils.fasterserializer.mapping;

import android.samutils.fasterserializer.mapping.value.CustomCopieable;
import android.samutils.utils.Assert;

import java.util.Collection;

public final class Copier extends ValueHelper {

	public static <T> void copy(final T to, final T from) throws IncompatibleTypesException {
		Assert.assertNotNull(to);
		Assert.assertNotNull(from);

		final Class<?> type = to.getClass();

		Assert.assertNotNull(type);

		final Collection<Jsoner.JsonableFieldInfo> fieldInfos = Jsoner.getAllFields(type);

		for (final FieldInfo fieldInfo : fieldInfos) {
			try {
				fieldInfo.Field.set(to, fieldInfo.Field.get(from));
			} catch (final IllegalAccessException e) {
				throw new RuntimeException("Field " + fieldInfo.Field.getName() + " in class "
					+ to.getClass().getSimpleName() + " is not accessible", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T copy(final T from) {
		Assert.assertNotNull(from);

		final T to = create((Class<T>) from.getClass());

		copy(to, from);

		if (to instanceof CustomCopieable<?>) {
			((CustomCopieable<T>) to).copy(from);
		}

		return to;
	}
}
