package android.samutils.fasterserializer.mapping.value;

public interface TokenizedEnum<E extends Enum<E>> {
	String getToken();

	E getDefault();
}
