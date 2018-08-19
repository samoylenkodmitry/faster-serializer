package ru.ivi.mapping.value;

import java.util.HashMap;
import java.util.Map;

import ru.ivi.utils.ArrayUtils;


public abstract class EnumTokensMap implements IEnumTokensMap {

	private final Map<Class<? extends Enum<?>>, Map<String, ? extends Enum<?>>> mEnumTokens;

	public EnumTokensMap() {
		mEnumTokens = new HashMap<>();
		fill();
	}
	
	@Override
	public Map<String, ? extends Enum<?>> getEnumTokens(final Class<? extends Enum<?>> classType) {
		return mEnumTokens.get(classType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Enum<?>> void addEnumTokens(final Class<T> enumType) {
		Map<String, T> enumCache = (Map<String, T>) mEnumTokens.get(enumType);
		if (enumCache == null) {
			enumCache = new HashMap<>();
		}

		final T[] enumConstants = enumType.getEnumConstants();
		if (!ArrayUtils.isEmpty(enumConstants)) {
			if (TokenizedEnum.class.isAssignableFrom(enumType)) {
				for (final T enumConstant : enumConstants) {
					enumCache.put(((TokenizedEnum<?>) enumConstant).getToken(), enumConstant);
				}

				enumCache.put("", (T) ((TokenizedEnum<?>) enumConstants[0]).getDefault());
			} else {
				for (final T enumConstant : enumConstants) {
					enumCache.put(enumConstant.name(), enumConstant);
				}
			}
		}

		mEnumTokens.put(enumType, enumCache);
	}

	protected abstract void fill();
}
