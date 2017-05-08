package android.samutils.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ArrayUtils {

	public static final long[] EMPTY_LONG_ARRAY = new long[0];
	public static final int[] EMPTY_INT_ARRAY = new int[0];
	public static final short[] EMPTY_SHORT_ARRAY = new short[0];
	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
	public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
	public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
	public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
	public static final char[] EMPTY_CHAR_ARRAY = new char[0];
	public static final String[] EMPTY_STRING_ARRAY = new String[0];
	
	public static final int INDEX_NOT_FOUND = -1;

	public static <T> T[] emptyArray(final Class<T> componentType) {
		return newArray(componentType, 0);
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] newArray(final Class<T> componentType, final int size) {
		Assert.assertNotNull(componentType);

		return (T[]) Array.newInstance(componentType, size);
	}

	@SuppressWarnings({ "unchecked" })
	public static <T> T[] concat(final T[] first, final T[] second) {
		if (first == null && second == null) {
			return null;
		} else if (isEmpty(first) && isEmpty(second)) {
			Assert.assertTrue(first != null || second != null);
			
			final Class<?> firstClass = first.getClass().getComponentType();
			final Class<?> secondClass = second.getClass().getComponentType();
			final Class<?> cls = first != null ? firstClass : secondClass;
			return (T[]) emptyArray(cls);
		} else if (!isEmpty(first) && !isEmpty(second)) {
			final T[] result = (T[]) Array.newInstance(
				first.getClass().getComponentType(), first.length + second.length);

			System.arraycopy(first, 0, result, 0, first.length);
			System.arraycopy(second, 0, result, first.length, second.length);

			return result;
		} else if (second == null) {
			Assert.assertNotNull(first);

			return first.length > 0
				? Arrays.copyOf(first, first.length)
				: (T[]) emptyArray(first.getClass().getComponentType());
		} else if (first == null) {
			Assert.assertNotNull(second);

			return second.length > 0
				? Arrays.copyOf(second, second.length)
				: (T[]) emptyArray(second.getClass().getComponentType());
		} else if (isEmpty(second)) {
			Assert.assertNotNull(first);
			Assert.assertTrue(first.length > 0);

			return Arrays.copyOf(first, first.length);
		} else /*if (isEmpty(first))*/ {
			Assert.assertNotNull(second);
			Assert.assertTrue(second.length > 0);

			return Arrays.copyOf(second, second.length);
		}
	}

	public static int indexOf(final boolean[] array, final boolean value) {
		if (!isEmpty(array)) {
			for (int i = 0; i < array.length; i++) {
				if (value == array[i]) {
					return i;
				}
			}
		}

		return -1;
	}

	public static int indexOf(final int[] array, final int value) {
		if (!isEmpty(array)) {
			for (int i = 0; i < array.length; i++) {
				if (value == array[i]) {
					return i;
				}
			}
		}

		return -1;
	}

	public static int indexOf(final long[] array, final long value) {
		if (!isEmpty(array)) {
			for (int i = 0; i < array.length; i++) {
				if (value == array[i]) {
					return i;
				}
			}
		}

		return -1;
	}

	public static int indexOf(final float[] array, final float value) {
		if (!isEmpty(array)) {
			for (int i = 0; i < array.length; i++) {
				if (value == array[i]) {
					return i;
				}
			}
		}

		return -1;
	}

	public static int indexOf(final double[] array, final double value) {
		if (!isEmpty(array)) {
			for (int i = 0; i < array.length; i++) {
				if (value == array[i]) {
					return i;
				}
			}
		}

		return -1;
	}

	public static <T> int indexOf(final T[] array, final T value) {
		if (!isEmpty(array)) {
			for (int i = 0; i < array.length; i++) {
				if (value == array[i] || (value != null && value.equals(array[i]))) {
					return i;
				}
			}
		}

		return -1;
	}

	public static int indexOfSubArray(final int[][] array, final int[] subArray) {
		if (!isEmpty(array) && !isEmpty(subArray)) {
			for (int i = 0; i < array.length; i++) {
				if (Arrays.equals(array[i], subArray)) {
					return i;
				}
			}
		}

		return -1;
	}

	public static <T extends Enum<?>> int indexOf(final T[] array, final T value) {
		if (!isEmpty(array) && value != null) {
			for (int i = 0; i < array.length; i++) {
				if (value == array[i]) {
					return i;
				}
			}
		}

		return -1;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] remove(final T[] array, final int index) {
		final int length = getLength(array);

		if (length == 0 || index < 0 || index >= length) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
		}

		final T[] result = newArray((Class<T>) array.getClass().getComponentType(), length - 1);

		System.arraycopy(array, 0, result, 0, index);

		if (index < length - 1) {
			System.arraycopy(array, index + 1, result, index, length - index - 1);
		}

		return result;
	}

	public static <T> int getLength(final T[] array) {
		return array != null ? array.length : 0;
	}

	public static boolean isEmpty(final boolean[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(final byte[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(final int[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(final long[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(final float[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(final double[] array) {
		return array == null || array.length == 0;
	}

	public static <T> boolean isEmpty(final T[] array) {
		return array == null || array.length == 0;
	}

	public static <T> List<T> asModifiableList(final T[] array) {
		if (!isEmpty(array)) {
			final List<T> result = new ArrayList<>(array.length);

			Collections.addAll(result, array);

			return result;
		} else {
			return new ArrayList<T>();
		}
	}

	public static <T> T[] toArray(final Collection<T> collection, final Class<T> componentType) {
		if (collection != null) {
			return collection.toArray(newArray(componentType, collection.size()));
		} else {
			return null;
		}
	}

	public static <T extends Comparable<T>> T[] toArrayAndSort(
		final Collection<T> collection, final Class<T> componentType)
	{
		final T[] array = toArray(collection, componentType);

		if (!isEmpty(array)) {
			Arrays.sort(array);
		}

		return array;
	}

	public static char[] toPrimitive(final Character[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_CHAR_ARRAY;
		}
		final char[] result = new char[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].charValue();
		}
		return result;
	}

	public static long[] toPrimitive(final Long[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_LONG_ARRAY;
		}
		final long[] result = new long[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].longValue();
		}
		return result;
	}

	public static int[] toPrimitive(final Integer[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_INT_ARRAY;
		}
		final int[] result = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].intValue();
		}
		return result;
	}
	
	public static int[] toPrimitive(final List<Integer> array) {
		if (array == null) {
			return null;
		}
		if (array.isEmpty()) {
			return EMPTY_INT_ARRAY;
		}
		final int[] result = new int[array.size()];
		for (int i = 0; i < array.size(); i++) {
			result[i] = array.get(i).intValue();
		}
		return result;
	}
	
	public static byte[] toPrimitiveByte(final List<Byte> array) {
		if (array == null) {
			return null;
		}
		if (array.isEmpty()) {
			return EMPTY_BYTE_ARRAY;
		}
		final byte[] result = new byte[array.size()];
		for (int i = 0; i < array.size(); i++) {
			result[i] = array.get(i).byteValue();
		}
		return result;
	}
	
	public static char[] toPrimitiveChar(final List<Integer> array) {
		if (array == null) {
			return null;
		}
		if (array.isEmpty()) {
			return EMPTY_CHAR_ARRAY;
		}
		final char[] result = new char[array.size()];
		for (int i = 0; i < array.size(); i++) {
			result[i] = (char) array.get(i).byteValue();
		}
		return result;
	}
	
	public static short[] toPrimitive(final Short[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_SHORT_ARRAY;
		}
		final short[] result = new short[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].shortValue();
		}
		return result;
	}

	public static byte[] toPrimitive(final Byte[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_BYTE_ARRAY;
		}
		final byte[] result = new byte[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].byteValue();
		}
		return result;
	}

	public static double[] toPrimitive(final Double[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_DOUBLE_ARRAY;
		}
		final double[] result = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].doubleValue();
		}
		return result;
	}

	public static float[] toPrimitive(final Float[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_FLOAT_ARRAY;
		}
		final float[] result = new float[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].floatValue();
		}
		return result;
	}

	public static boolean[] toPrimitive(final Boolean[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_BOOLEAN_ARRAY;
		}
		final boolean[] result = new boolean[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].booleanValue();
		}
		return result;
	}
	
	public static <T> T[] clone(final T[] array) {
		if (array == null) {
			return null;
		}
		return array.clone();
	}
	
	/**
	 * <p>Removes the first occurrence of the specified element from the
	 * specified array. All subsequent elements are shifted to the left
	 * (subtracts one from their indices). If the array doesn't contains
	 * such an element, no elements are removed from the array.</p>
	 *
	 * <p>This method returns a new array with the same elements of the input
	 * array except the first occurrence of the specified element. The component
	 * type of the returned array is always the same as that of the input
	 * array.</p>
	 *
	 * <pre>
	 * ArrayUtils.removeElement(null, "a")            = null
	 * ArrayUtils.removeElement([], "a")              = []
	 * ArrayUtils.removeElement(["a"], "b")           = ["a"]
	 * ArrayUtils.removeElement(["a", "b"], "a")      = ["b"]
	 * ArrayUtils.removeElement(["a", "b", "a"], "a") = ["b", "a"]
	 * </pre>
	 *
	 * @param <T> the component type of the array
	 * @param array  the array to remove the element from, may be {@code null}
	 * @param element  the element to be removed
	 * @return A new array containing the existing elements except the first
	 *         occurrence of the specified element.
	 * @since 2.1
	 */
	public static <T> T[] removeElement(final T[] array, final Object element) {
		final int index = indexOf(array, element);
		if (index == INDEX_NOT_FOUND) {
			return clone(array);
		}
		return remove(array, index);
	}
	
}
