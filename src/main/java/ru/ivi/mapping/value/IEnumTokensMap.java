package ru.ivi.mapping.value;


import java.util.Map;

public interface IEnumTokensMap {

	Map<String, ? extends Enum<?>> getEnumTokens(final Class<? extends Enum<?>> classType);

	<T extends Enum<?>> void addEnumTokens(final Class<T> enumType);
}
