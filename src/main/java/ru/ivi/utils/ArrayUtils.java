package ru.ivi.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "ForLoopReplaceableByForEach", "BooleanMethodNameMustStartWithQuestion", "WeakerAccess" })
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
	public static final Transform<Object, Object> TT_TRANSFORM = t -> t;
	
	public static <T> T[] emptyArray(final Class<T> componentType) {
		return newArray(componentType, 0);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] newArray(final Class<T> componentType, final int size) {
		ru.ivi.utils.Assert.assertNotNull(componentType);
		
		return (T[]) Array.newInstance(componentType, size);
	}
	
	@SuppressWarnings({ "unchecked" })
	public static <T> T[] concat(final T[] first, final T[] second) {
		if (first == null && second == null) {
			return null;
		} else if (isEmpty(first) && isEmpty(second)) {
			ru.ivi.utils.Assert.assertTrue(first != null || second != null);
			
			final Class<?> cls = first != null ? first.getClass().getComponentType() : second.getClass().getComponentType();
			return (T[]) emptyArray(cls);
		} else if (!isEmpty(first) && !isEmpty(second)) {
			final T[] result = (T[]) Array.newInstance(
				first.getClass().getComponentType(), first.length + second.length);
			
			System.arraycopy(first, 0, result, 0, first.length);
			System.arraycopy(second, 0, result, first.length, second.length);
			
			return result;
		} else if (second == null) {
			ru.ivi.utils.Assert.assertNotNull(first);
			
			return first.length > 0
				? Arrays.copyOf(first, first.length)
				: (T[]) emptyArray(first.getClass().getComponentType());
		} else if (first == null) {
			ru.ivi.utils.Assert.assertNotNull(second);
			
			return second.length > 0
				? Arrays.copyOf(second, second.length)
				: (T[]) emptyArray(second.getClass().getComponentType());
		} else if (isEmpty(second)) {
			ru.ivi.utils.Assert.assertNotNull(first);
			ru.ivi.utils.Assert.assertTrue(first.length > 0);
			
			return Arrays.copyOf(first, first.length);
		} else /*if (isEmpty(first))*/ {
			ru.ivi.utils.Assert.assertNotNull(second);
			ru.ivi.utils.Assert.assertTrue(second.length > 0);
			
			return Arrays.copyOf(second, second.length);
		}
	}
	
	public static <T> void swapByIndex(final T[] array, final int indexA, final int indexB) {
		if (!isEmpty(array)) {
			final T t = array[indexA];
			array[indexA] = array[indexB];
			array[indexB] = t;
		}
	}
	
	public static <T> int indexOfAcceptedNotNull(final T[] array, final Checker<T> checker){
		if (!isEmpty(array)) {
			for (int i = 0; i < array.length; i++) {
				if (array[i] != null) {
					if (checker.accept(array[i])) {
						return i;
					}
				}
			}
		}
		return -1;
	}
	
	public static <T, R> int indexOfNotNull(final T[] array, final R value, final Transform<T, R> transform) {
		if (!isEmpty(array)) {
			for (int i = 0; i < array.length; i++) {
				if (array[i] != null) {
					final R chkValue = transform.transform(array[i]);
					if (chkValue == value
						|| chkValue == null && value == null
						|| chkValue != null && chkValue.equals(value)) {
						return i;
					}
				}
			}
		}
		return -1;
	}
	
	public static <T> int indexOfAccepted(final T[] array, final Checker<T> checker){
		if (!isEmpty(array)) {
			for (int i = 0; i < array.length; i++) {
				if (checker.accept(array[i])) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public static <T> int indexOfAccepted(final T[] array, final EachChecker<T> checker){
		if (!isEmpty(array)) {
			for (int i = 0; i < array.length; i++) {
				if (checker.check(array[i], i)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public static <T> int keyOfAcceptedNotNull(final SparseArray<T> array, final Checker<T> checker) {
		if (array != null) {
			
			for (int i = 0, len = array.size(); i < len; i++) {
				final T t = array.valueAt(i);
				if (t != null) {
					if (checker.accept(t)) {
						return array.keyAt(i);
					}
				}
			}
		}
		return -1;
	}
	
	public static <T, R> int[] indexOfAccepted2(final T[] array, final Transform<T, R[]> subArrConverter, final Checker<R> checker) {
		if (!isEmpty(array)) {
			for (int i = 0, len = array.length; i < len; i++) {
				if (array[i] != null) {
					
					final R[] subArray = subArrConverter.transform(array[i]);
					if (subArray != null) {
						
						final int j = indexOfAccepted(subArray, checker);
						if (j != -1) {
							return new int[] { i, j };
						}
					}
				}
			}
		}
		return null;
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
	
	public static <T> int indexOfSubArray(final T[][] array, final T[] subArray) {
		if (!isEmpty(array) && !isEmpty(subArray)) {
			for (int i = 0; i < array.length; i++) {
				if (Arrays.equals(array[i], subArray)) {
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
	
	/**
	 * <p>
	 * Removes the element at the specified position from the specified array.
	 * All subsequent elements are shifted to the left (substracts one from
	 * their indices).
	 * </p>
	 * <p>
	 * <p>
	 * This method returns a new array with the same elements of the input array
	 * except the element on the specified position. The component type of the
	 * returned array is always the same as that of the input array.
	 * </p>
	 * <p>
	 * <p>
	 * If the input array is <code>null</code>, an IndexOutOfBoundsException
	 * will be thrown, because in that case no valid index can be specified.
	 * </p>
	 *
	 * @param array the array to remove the element from, may not be
	 *              <code>null</code>
	 * @param index the position of the element to be removed
	 * @return A new array containing the existing elements except the element
	 * at the specified position.
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >=
	 *                                   array.length), or if the array is <code>null</code>.
	 */
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
		return array == null ? 0 : array.length;
	}
	
	public static int getLength(final int[] array) {
		return array == null ? 0 : array.length;
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
	
	public static <T> boolean isDeepEmpty(final T[] array) {
		return getFirstNotNull(array) == null;
	}
	
	public static <T> boolean notEmpty(final T[] array) {
		return !isEmpty(array);
	}
	
	public static <T> boolean notEmpty(final int[] array) {
		return !isEmpty(array);
	}
	
	public static <T> boolean isEqual(final T[] array1, final T[] array2, final Comparator<T> itemComparator) {
		if (isEmpty(array1) && isEmpty(array2)) {
			//accept null's as empty arrays
			return true;
		}
		if (!isEmpty(array1) && isEmpty(array2) || (isEmpty(array1) && !isEmpty(array2))) {
			//accept null's as empty arrays
			return false;
		}
		final int len1 = array1.length;
		if (len1 != array2.length) {
			return false;
		}
		for (int i = 0; i < len1; i++) {
			final T item1 = array1[i];
			final T item2 = array2[i];
			if (item1 == null && item2 != null || item1 != null && item2 == null) {
				return false;
			}
			if (item1 != null && item2 != null) {
				if (itemComparator.compare(item1, item2) != 0) {
					return false;
				}
			}
		}
		
		return true;
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
	
	public static <T> T[] toArray(final Collection<T> collection) {
		if (collection != null && !collection.isEmpty()) {
			//noinspection unchecked
			return collection.toArray(newArray((Class<T>) collection.iterator().next().getClass(), collection.size()));
		} else {
			return null;
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
		final Collection<T> collection, final Class<T> componentType) {
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
			result[i] = array[i];
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
	 * <p>
	 * <p>This method returns a new array with the same elements of the input
	 * array except the first occurrence of the specified element. The component
	 * type of the returned array is always the same as that of the input
	 * array.</p>
	 * <p>
	 * <pre>
	 * ArrayUtils.removeElement(null, "a")            = null
	 * ArrayUtils.removeElement([], "a")              = []
	 * ArrayUtils.removeElement(["a"], "b")           = ["a"]
	 * ArrayUtils.removeElement(["a", "b"], "a")      = ["b"]
	 * ArrayUtils.removeElement(["a", "b", "a"], "a") = ["b", "a"]
	 * </pre>
	 *
	 * @param <T>     the component type of the array
	 * @param array   the array to remove the element from, may be {@code null}
	 * @param element the element to be removed
	 * @return A new array containing the existing elements except the first
	 * occurrence of the specified element.
	 * @since 2.1
	 */
	public static <T> T[] removeElement(final T[] array, final Object element) {
		final int index = indexOf(array, element);
		if (index == INDEX_NOT_FOUND) {
			return clone(array);
		}
		return remove(array, index);
	}
	
	public static Integer[] toObject(final int[] arr) {
		if (arr == null) {
			return null;
		}
		final Integer[] newArr = new Integer[arr.length];
		for (int i = 0; i < arr.length; i++) {
			newArr[i] = arr[i];
		}
		
		return newArr;
	}
	
	@SuppressWarnings("MagicNumber")
	public static <T> int hashCode(final T[] inArr, final Transform<T, Integer> eachToInt) {
		if (inArr == null) {
			return 0;
		}
		int result = 1;
		for (final T element : inArr) {
			result = 31 * result + eachToInt.transform(element);
		}
		
		return result;
	}
	
	public static <T> int hashSum(final T[] inArr, final Transform<T, Integer> eachToInt) {
		if (inArr == null) {
			return -1;
		}
		int sum = 0;
		for (int i = 0, inArrLength = inArr.length; i < inArrLength; i++) {
			sum += eachToInt.transform(inArr[i]) + i;
		}
		return sum;
	}
	
	public static <T> int sum(final T[] inArr, final Transform<T, Integer> eachToInt) {
		if (inArr == null) {
			return -1;
		}
		int sum = 0;
		for (int i = 0, inArrLength = inArr.length; i < inArrLength; i++) {
			final T from = inArr[i];
			if (from != null) {
				sum += eachToInt.transform(from);
			}
		}
		return sum;
	}
	
	public static <T> int sum(final SparseArray<T> inArr, final Transform<T, Integer> eachToInt) {
		if (inArr == null) {
			return -1;
		}
		int sum = 0;
		for (int i = 0, inArrLength = inArr.size(); i < inArrLength; i++) {
			final T from = inArr.valueAt(i);
			if (from != null) {
				sum += eachToInt.transform(from);
			}
		}
		return sum;
	}
	
	public static <T> float sumFloat(final SparseArray<T> inArr, final FloatConverter<T> eachToInt) {
		if (inArr == null) {
			return -1.0f;
		}
		float sum = 0.0f;
		for (int i = 0, inArrLength = inArr.size(); i < inArrLength; i++) {
			final T from = inArr.valueAt(i);
			if (from != null) {
				sum += eachToInt.convert(from);
			}
		}
		return sum;
	}
	
	public static <T> long sumLong(final T[] inArr, final Transform<T, Long> eachToInt) {
		if (inArr == null) {
			return -1;
		}
		long sum = 0;
		for (int i = 0, inArrLength = inArr.length; i < inArrLength; i++) {
			final T from = inArr[i];
			if (from != null) {
				sum += eachToInt.transform(from);
			}
		}
		return sum;
	}
	
	public static <T> String toString(final T[] inArr, final Transform<T, String> eachToString) {
		if (inArr == null) {
			return "[null]";
		}
		if (inArr.length == 0) {
			return "[empty]";
		}
		final StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (int i = 0, inArrLength = inArr.length; i < inArrLength; i++) {
			builder.append(eachToString.transform(inArr[i]));
			if (i != inArrLength - 1) {
				builder.append(", ");
			}
		}
		builder.append("]");
		return builder.toString();
	}
	
	@Nullable
	public static <T> T[] filter(final T[] inArr, final Checker<T> func) {
		//noinspection unchecked
		return filter(inArr, func, (Transform<T, T>) TT_TRANSFORM);
	}
	
	public static <T> T[] filterUnique(final T[] inArr, final Transform<T, Object> uniquer) {
		final Collection<Object> uniques = new HashSet<>();
		
		return filter(inArr, t -> {
			final Object uniq = uniquer.transform(t);
			@SuppressWarnings("BooleanVariableAlwaysNegated")
			
			final boolean contains = uniques.contains(uniq);
			uniques.add(uniq);
			
			return !contains;
		});
	}
	
	public static <T> boolean contains(final T[] inArr, final Checker<T> func) {
		return find(inArr, func) != null;
	}
	
	@Nullable
	public static <T> T find(final T[] inArr, final Checker<T> func) {
		if (inArr == null || inArr.length == 0) {
			return null;
		}
		
		for (int i = 0; i < inArr.length; i++) {
			if (inArr[i] != null) {
				if (func.accept(inArr[i])) {
					return inArr[i];
				}
			}
		}
		return null;
	}
	
	@Nullable
	public static <T> T find(final SparseArray<T> array, final Checker<T> checker) {
		if (array != null) {
			for (int i = 0, len = array.size(); i < len; i++) {
				final T t = array.valueAt(i);
				if (t != null) {
					if (checker.accept(t)) {
						return t;
					}
				}
			}
		}
		
		return null;
	}
	
	public static <T> boolean any(final T[] inArr, final Checker<T> func) {
		if (inArr == null || inArr.length == 0) {
			return false;
		}
		
		for (int i = 0; i < inArr.length; i++) {
			if (func.accept(inArr[i])) {
				return true;
			}
		}
		return false;
	}
	
	public static <T> boolean anySafe(final T[] inArr, final Checker<T> func) {
		if (inArr == null || inArr.length == 0) {
			return false;
		}
		
		for (int i = 0; i < inArr.length; i++) {
			if (inArr[i] != null) {
				if (func.accept(inArr[i])) {
					return true;
				}
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T, R> R[] filter(final T[] inArr, final Checker<T> func, final Transform<T, R> transform) {
		if (inArr == null || inArr.length == 0) {
			return null;
		}
		final List<Object> result = new ArrayList<>();
		for (final T anInArr : inArr) {
			if ((func == null || func.accept(anInArr)) && transform != null) {
				result.add(transform.transform(anInArr));
			}
		}
		
		if (result.isEmpty()) {
			return null;
		} else {
			return (R[]) result.toArray(newArray(result.get(0).getClass(), result.size()));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <R> R[] intsFilter(final int[] inArr, final IntTransform<R> transform) {
		if (inArr == null || inArr.length == 0) {
			return null;
		}
		final int size = inArr.length;
		if (size == 0) {
			return null;
		}
		final List<Object> result = new ArrayList<>();
		for (int i = 0; i < inArr.length; i++) {
			final R trans = transform.transform(inArr[i]);
			if (trans != null) {
				result.add(trans);
			}
		}
		
		if (result.isEmpty()) {
			return null;
		} else {
			return (R[]) result.toArray(newArray(result.get(0).getClass(), result.size()));
		}
	}
	
	@NonNull
	public static <T, R> R[] mapNonNull(final Class<R> rClass, final T[] inArr, final Transform<T, R> transform) {
		return filterNonNull(rClass, inArr, null, transform);
	}
	
	@NonNull
	public static <T> T[] filterNonNull(final Class<T> rClass, final T[] inArr, final Checker<T> func) {
		if (inArr == null) {
			return null;
		}
		if (isEmpty(inArr)) {
			return newArray(rClass, 0);
		}
		final Collection<T> result = new ArrayList<>(inArr.length);
		for (int i = 0, inArrLength = inArr.length; i < inArrLength; i++) {
			final T anInArr = inArr[i];
			if (anInArr != null) {
				if (func == null || func.accept(anInArr)) {
					result.add(anInArr);
				}
			}
		}
		
		return result.toArray(newArray(rClass, result.size()));
	}
	
	@NonNull
	public static <T, R> R[] filterNonNull(final Class<R> rClass, final T[] inArr, final Checker<T> func, final Transform<T, R> transform) {
		if (inArr == null) {
			return null;
		}
		if (isEmpty(inArr)) {
			return newArray(rClass, 0);
		}
		final Collection<R> result = new ArrayList<>(inArr.length);
		for (int i = 0, inArrLength = inArr.length; i < inArrLength; i++) {
			final T anInArr = inArr[i];
			if (transform == null) {
				if (anInArr != null) {
					//noinspection unchecked
					final R tr = (R) anInArr;
					result.add(tr);
				}
			} else if (func == null || anInArr != null && func.accept(anInArr)) {
				final R tr = transform.transform(anInArr);
				if (tr != null) {
					result.add(tr);
				}
			}
		}
		
		return result.toArray(newArray(rClass, result.size()));
	}
	
	public static <T, R> R[] filterConcatNonNull(final Class<R> rClass, final T[] inArr, final Transform<T, R[]> transform) {
		return filterConcatNonNull(rClass, inArr, null, transform);
	}
	
	@NonNull
	public static <T, R> R[] filterConcatNonNull(final Class<R> rClass, final T[] inArr, final Checker<T> func, final Transform<T, R[]> transform) {
		Assert.assertNotNull(transform);
		
		if (inArr == null) {
			return null;
		}
		if (isEmpty(inArr)) {
			return newArray(rClass, 0);
		}
		final Collection<R> result = new ArrayList<>(inArr.length);
		for (int arri = 0, inArrLength = inArr.length; arri < inArrLength; arri++) {
			final T anInArr = inArr[arri];
			if (func == null || func.accept(anInArr)) {
				final R[] tr = transform.transform(anInArr);
				if (tr != null) {
					for (int i = 0, trLength = tr.length; i < trLength; i++) {
						final R r = tr[i];
						if (r != null) {
							result.add(r);
						}
					}
				}
			}
		}
		
		return result.toArray(newArray(rClass, result.size()));
	}
	
	@Nullable
	public static <T, R> R[] flatMap(final T[] inArr, final Transform<T, R> transform) {
		return filter(inArr, null, transform);
	}
	
	public static <T, R> R[] flatMap(final Class<R> rCls, final T[] inArr, final Transform<T, R> transform) {
		if (transform == null) {
			throw new IllegalArgumentException("transform must be nonnull");
		}
		if (inArr == null) {
			return null;
		}
		final R[] result = newArray(rCls, inArr.length);
		for (int i = 0; i < inArr.length; i++) {
			final T anInArr = inArr[i];
			result[i] = transform.transform(anInArr);
		}
		
		return result;
	}
	
	public static <T, R> R[] flatMap(final Class<R> rCls, final SparseArray<T> inArr, final Transform<T, R> transform) {
		if (transform == null) {
			throw new IllegalArgumentException("transform must be nonnull");
		}
		if (inArr == null) {
			return null;
		}
		final R[] result = newArray(rCls, inArr.size());
		for (int i = 0, len = inArr.size(); i < len; i++) {
			final T anInArr = inArr.valueAt(i);
			result[i] = transform.transform(anInArr);
		}
		
		return result;
	}
	
	public static <T, R> R first(final T[] inArr, final Transform<T, R> transform) {
		if (inArr == null) {
			return null;
		}
		if (inArr.length == 0) {
			return null;
		}
		return transform.transform(inArr[0]);
	}
	
	public static <T, R> R last(final T[] inArr, final Transform<T, R> transform) {
		if (inArr == null) {
			return null;
		}
		if (inArr.length == 0) {
			return null;
		}
		return transform.transform(inArr[inArr.length - 1]);
	}
	
	public static <T, R> R anyNotNull(final T[] inArr, final Transform<T, R> transform) {
		if (inArr == null) {
			return null;
		}
		for (final T t : inArr) {
			if (transform == null) {
				if (t != null) {
					//noinspection unchecked
					return (R) t;
				}
			} else {
				final R result = transform.transform(t);
				if (result != null) {
					return result;
				}
			}
		}
		
		return null;
	}
	
	public static <T, R> R anyNotNullTransform(final T[] inArr, final Checker<T> checker, final Transform<T, R> transform) {
		Assert.assertNotNull(checker);
		Assert.assertNotNull(transform);
		if (inArr == null) {
			return null;
		}
		for (final T t : inArr) {
			if (t != null && checker.accept(t)) {
				final R result = transform.transform(t);
				if (result != null) {
					return result;
				}
			}
		}
		
		return null;
	}
	
	public static <T> T anyNotNull2(final T[][] inArr) {
		return anyNotNull2(inArr, null);
	}
	
	@SuppressWarnings("SameParameterValue")
	public static <T, R> R anyNotNull2(final T[][] inArr, final Transform<T, R> transform) {
		if (inArr == null) {
			return null;
		}
		for (final T[] anInArr : inArr) {
			if (anInArr != null) {
				for (final T t : anInArr) {
					if (transform == null) {
						if (t != null) {
							//noinspection unchecked
							return (R) t;
						}
					} else {
						final R result = transform.transform(t);
						if (result != null) {
							return result;
						}
					}
				}
			}
		}
		
		return null;
	}
	
	public static <R> void each(final R[] inArr, final EachVisitor<R> eachVisitor) {
		if (inArr == null) {
			return;
		}
		for (int i = 0; i < inArr.length; i++) {
			eachVisitor.visit(inArr[i], i);
		}
	}
	
	public static <R> void eachNonNull(final R[] inArr, final EachVisitor<R> eachVisitor) {
		if (inArr == null) {
			return;
		}
		for (int i = 0; i < inArr.length; i++) {
			if (inArr[i] != null) {
				eachVisitor.visit(inArr[i], i);
			}
		}
	}
	
	public static <R, T> void eachTransformNonNull(
		final R[] inArr,
		final Transform<R, T> transform,
		final Each<T> eachVisitor
	) {
		if (inArr == null) {
			return;
		}
		for (int i = 0; i < inArr.length; i++) {
			if (inArr[i] != null) {
				final T tr = transform.transform(inArr[i]);
				if (tr != null) {
					eachVisitor.visit(tr);
				}
			}
		}
	}
	
	public static boolean isArraysEquals(final SparseBooleanArray oneArray, final SparseBooleanArray secondArray) {
		if (oneArray == secondArray) {
			return true;
		}
		
		if (oneArray.size() != secondArray.size()) {
			return false;
		}
		
		for (int i = 0; i < oneArray.size(); i++) {
			if (oneArray.keyAt(i) != secondArray.keyAt(i)) {
				return false;
			}
			if (oneArray.valueAt(i) != secondArray.valueAt(i)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean inRange(final int[] arr, final int index) {
		return arr != null && 0 <= index && index < arr.length;
	}
	
	public static boolean inRange(final boolean[] arr, final int index) {
		return arr != null && 0 <= index && index < arr.length;
	}
	
	public static boolean inRange(final Object[] arr, final int index) {
		return arr != null && 0 <= index && index < arr.length;
	}
	
	public static <T> T get(final T[] arr, final int index) {
		return inRange(arr, index) ? arr[index] : null;
	}
	
	public static <T, R> R get(final T[] arr, final int index, final Transform<T, R> fromElem) {
		return inRange(arr, index) ? fromElem.transform(arr[index]) : null;
	}
	
	public static int[] getSubArrayForPage(final int[] array, final int page, final int maxPageSize) {
		if (isEmpty(array) || maxPageSize < 1 || page < 0) {
			return null;
		}
		final int from = page * maxPageSize;
		if (from > array.length) {
			return null;
		}
		int to = from + maxPageSize - 1;
		final boolean lastPageHasLessThanMaxPageSize = array.length - from < maxPageSize;
		if (lastPageHasLessThanMaxPageSize) {
			to = array.length - 1;
		}
		return Arrays.copyOfRange(array, from, to + 1);
	}
	
	public static <T> T[] getSubArray(final T[] array, final int positionFrom, final int positionTo) {
		if (isEmpty(array) || positionFrom > positionTo || positionFrom < 0 || positionTo >= array.length) {
			return null;
		}
		return Arrays.copyOfRange(array, positionFrom, positionTo);
	}
	
	public static boolean containsValue(final int[] array, final int value) {
		if (array == null) {
			return false;
		}
		for (final int item : array) {
			if (item == value) {
				return true;
			}
		}
		return false;
	}
	
	public static <T> boolean containsInstance(final T[] array, final T object) {
		if (array == null) {
			return false;
		}
		for (final T item : array) {
			if (item == object) {
				return true;
			}
		}
		return false;
	}
	
	public static String toString(final Object... objects) {
		if (objects == null) {
			
			return "null";
		}
		
		final int maxInd = objects.length - 1;
		if (maxInd == -1) {
			
			return "[]";
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		
		for (int i = 0; ; i++) {
			final Object object = objects[i];
			
			final String sObj = StringUtils.tryToString(object);
			
			sb.append(sObj);
			
			if (i == maxInd) {
				sb.append("]");
				
				break;
			}
			
			sb.append(", ");
		}
		
		return sb.toString();
	}
	
	/**
	 * Shifts items in array for shiftTo elements
	 * <p>
	 * [a, b, c, d, e] << 0  =  [a, b, c, d, e]
	 * <p>
	 * [a, b, c, d, e] << 1  =  [b, c, d, e, a]
	 * <p>
	 * [a, b, c, d, e] << 2  =  [c, d, e, a, b]
	 * <p>
	 * [a, b, c, d, e] << 3  =  [d, e, a, b, c]
	 * <p>
	 * [a, b, c, d, e] << 4  =  [e, a, b, c, d]
	 * <p>
	 * [a, b, c, d, e] << 5  =  [a, b, c, d, e]
	 * <p>
	 * * can be used for shiftRight when shiftTo < 0
	 * <p>
	 *     O(N*shiftTo)
	 */
	public static <T> T[] shiftLeftInPlace(final T[] arr, final int shiftTo) {
		if (arr == null || arr.length == 0) {
			return arr;
		}
		
		final int shifts = Math.abs(shiftTo) % arr.length;
		if (shiftTo > 0) {
			for (int i = 0; i < shifts; i++) {
				shiftLeftByOne(arr);
			}
		} else {
			for (int i = 0; i < shifts; i++) {
				shiftRightByOne(arr);
			}
		}
		
		return arr;
	}
	
	public static <T> T[] toValuesArray(final SparseArray<T> sparse, final T[] arr) {
		for (int i = 0; i < sparse.size(); i++) {
			arr[i] = sparse.valueAt(i);
		}
		return arr;
	}
	
	public static <T> T[] toValuesArray(final Map<?, T> map, final T[] arr) {
		int i = 0;
		for (final T t : map.values()) {
			arr[i++] = t;
		}
		return arr;
	}
	
	public static <T> T[] arrayOf(final T element, final int count) {
		//noinspection unchecked
		final T[] arr = newArray((Class<T>) element.getClass(), count);
		Arrays.fill(arr, element);
		
		return arr;
	}
	
	public static <T> int sizeOf(final SparseArray<T[]> contents) {
		int size = 0;
		for (int i = 0; i < contents.size(); i++) {
			final T[] valueAt = contents.valueAt(i);
			if (valueAt != null) {
				size += valueAt.length;
			}
		}
		return size;
	}
	
	public static <T> int sizeOfArrayWithList(final SparseArray<List<T>> contents) {
		int size = 0;
		for (int i = 0; i < contents.size(); i++) {
			final List<T> valueAt = contents.valueAt(i);
			if (valueAt != null) {
				size += valueAt.size();
			}
		}
		return size;
	}
	
	@Nullable
	public static <T> T getForAbsolutePosition(final SparseArray<T[]> contents, final int position) {
		int pos = 0;
		for (int i = 0; i < contents.size(); i++) {
			final T[] page = contents.valueAt(i);
			if (page != null) {
				for (final T value : page) {
					if (pos == position) {
						return value;
					}
					
					pos++;
				}
			}
		}
		return null;
	}
	
	@Nullable
	public static <T> T getForAbsolutePositionInArrayWithList(final SparseArray<List<T>> contents, final int position) {
		int pos = 0;
		for (int i = 0; i < contents.size(); i++) {
			final List<T> page = contents.valueAt(i);
			if (page != null) {
				for (final T value : page) {
					if (pos == position) {
						return value;
					}
					
					pos++;
				}
			}
		}
		return null;
	}
	
	public static <T> void removeFromArrayWithList(final SparseArray<List<T>> contents, final T value) {
		for (int i = 0; i < contents.size(); i++) {
			final List<T> page = contents.valueAt(i);
			if (page != null) {
				for (int j = 0; j < page.size(); j++) {
					if (value.equals(page.get(j))) {
						page.remove(j);
						break;
					}
				}
			}
		}
	}
	
	public static <T> int sizeOf(final T[][] arr) {
		int size = 0;
		for (final T[] ts : arr) {
			if (ts != null) {
				size += ts.length;
			}
		}
		return size;
	}
	
	public static <T> T[] collapse(final T[][] arr, final Class<T> cls) {
		final int size = sizeOf(arr);
		final T[] result = newArray(cls, size);
		int ind = 0;
		for (final T[] ts : arr) {
			if (ts != null) {
				for (final T t : ts) {
					result[ind++] = t;
				}
			}
		}
		return result;
	}
	
	public static <T> T getFirstNotNull(final T[] arr) {
		if (notEmpty(arr)) {
			for (final T t : arr) {
				if (t != null) {
					return t;
				}
			}
		}
		return null;
	}
	
	public static <T> T getFirstNotNullDown(final T[] arr, final int upIndex) {
		if (notEmpty(arr)) {
			for (int i = Math.min(upIndex, arr.length - 1); i >= 0; i--) {
				final T t = arr[i];
				if (t != null) {
					return t;
				}
			}
		}
		return null;
	}
	
	public static <T> T getFirstNotNull(final T[][] arrs) {
		if (notEmpty(arrs)) {
			for (final T[] arr : arrs) {
				if (notEmpty(arr)) {
					for (final T t : arr) {
						if (t != null) {
							return t;
						}
					}
				}
			}
		}
		return null;
	}
	
	public static <T>void removePositionAndShift(final SparseArray<T> arr, final int position) {
		final int last = arr.size() - 1;
		for (int i = position; i < last; i++) {
			final T value = arr.get(i + 1);
			if (value != null) {
				arr.put(i, value);
			}
		}
		arr.delete(arr.size() - 1);
	}
	
	private static <T> void shiftLeftByOne(final T[] arr) {
		if (arr == null || arr.length == 0) {
			return;
		}
		
		final T tmp = arr[0];
		//noinspection ManualArrayCopy
		for (int i = 0; i < arr.length - 1; i++) {
			arr[i] = arr[i + 1];
		}
		arr[arr.length - 1] = tmp;
	}
	
	private static <T> void shiftRightByOne(final T[] arr) {
		if (arr == null || arr.length == 0) {
			return;
		}
		
		final T tmp = arr[arr.length - 1];
		//noinspection ManualArrayCopy
		for (int i = arr.length - 1; i > 0; i--) {
			arr[i] = arr[i - 1];
		}
		arr[0] = tmp;
	}
	
	public static <T> T[] of(final Class<T> cls, final int count, final EachCreator<T> creator) {
		final T[] arr = newArray(cls, count);
		for (int i = 0; i < count; i++) {
			arr[i] = creator.create(i);
		}
		return arr;
	}
	
	public static <T> T[] of(final Class<T> cls, final Object... items) {
		final int count = items.length;
		final T[] arr = newArray(cls, count);
		for (int i = 0; i < count; i++) {
			//noinspection unchecked
			arr[i] = (T) items[i];
		}
		return arr;
	}
}
