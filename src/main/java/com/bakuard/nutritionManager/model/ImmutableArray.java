package com.bakuard.nutritionManager.model;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;

public final class ImmutableArray<T> {

    private final T[] VALUES;
    private final Class<T> TYPE;

    @SuppressWarnings("unchecked")
    public ImmutableArray(Class<T> type) {
        if(type != null) {
            TYPE = type;
            VALUES = (T[]) Array.newInstance(type, 0);
        } else {
            throw new IllegalArgumentException("Параметр type не должен равняться null.");
        }
    }

    @SuppressWarnings("unchecked")
    public ImmutableArray(Class<T> type, int length) {
        if(length < 0)
            throw new IllegalArgumentException("Длина массива не может быть отрицательной.");

        if(type != null) {
            TYPE = type;
            VALUES = (T[]) Array.newInstance(type, length);
        } else {
            throw new IllegalArgumentException("Параметр type не должен равняться null.");
        }
    }

    @SuppressWarnings("unchecked")
    public ImmutableArray(Class<T> type, List<T> data) {
        TYPE = type;

        if(data != null && type != null) {
            VALUES = (T[]) Array.newInstance(type, data.size());
            ListIterator<T> iterator = data.listIterator();
            while(iterator.hasNext()) {
                int index = iterator.nextIndex();
                T value = iterator.next();
                VALUES[index] = value;
            }
        } else {
            throw new IllegalArgumentException("Параметры data и type не должны равняться null.");
        }
    }

    public ImmutableArray<T> set(int index, T value) {
        if(index < 0 || index >= VALUES.length)
            throw new IndexOutOfBoundsException("length=" + VALUES.length + ", index=" + index);

        ImmutableArray<T> copyArray = new ImmutableArray<>(TYPE, VALUES.length);
        System.arraycopy(VALUES, 0, copyArray.VALUES, 0, copyArray.VALUES.length);
        copyArray.VALUES[index] = value;
        return copyArray;
    }

    public ImmutableArray<T> add(T value) {
        ImmutableArray<T> copyArray = new ImmutableArray<>(TYPE, VALUES.length + 1);
        System.arraycopy(VALUES, 0, copyArray.VALUES, 0, VALUES.length);
        copyArray.VALUES[VALUES.length] = value;
        return copyArray;
    }

    public ImmutableArray<T> remove(int index) {
        if(index < 0 || index >= VALUES.length)
            throw new IndexOutOfBoundsException("length=" + VALUES.length + ", index=" + index);

        ImmutableArray<T> copyArray = new ImmutableArray<>(TYPE, VALUES.length - 1);
        System.arraycopy(VALUES, 0, copyArray.VALUES, 0, copyArray.VALUES.length);
        if(index < copyArray.VALUES.length) {
            System.arraycopy(VALUES, index + 1, copyArray.VALUES, index, copyArray.VALUES.length - index);
        }
        return copyArray;
    }

    public ImmutableArray<T> remove(T value) {
        int index = indexOf(value);
        if(index != -1) return remove(index);
        else return this;
    }

    public T get(int index) {
        if(index < 0 || index >= VALUES.length)
            throw new IndexOutOfBoundsException("length=" + VALUES.length + ", index=" + index);

        return VALUES[index];
    }

    public int getLength() {
        return VALUES.length;
    }

    public int indexOf(T value) {
        int index = -1;
        for(int i = 0; i < VALUES.length && index == -1; i++) {
            if(Objects.equals(VALUES[i], value)) index = i;
        }
        return index;
    }

    public boolean contains(T value) {
        return indexOf(value) >= 0;
    }

    public void forEach(Consumer<T> action) {
        for(int i = 0; i < VALUES.length; ++i) action.accept(VALUES[i]);
    }

    public ImmutableArray<T> removeAll(ImmutableArray<T> other, Equivalence<T> equivalence) {
        ArrayList<T> buffer = new ArrayList<>();
        for(int i = 0; i < VALUES.length; ++i) {
            T current = VALUES[i];
            boolean contains = false;
            for(int j = 0; j < other.VALUES.length && !contains; ++j) {
                contains = equivalence.equivalent(current, other.VALUES[j]);
            }
            if(!contains) buffer.add(current);
        }
        return new ImmutableArray<>(TYPE, buffer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableArray<?> array = (ImmutableArray<?>) o;

        if(array.VALUES.length != VALUES.length) return false;
        for(int i = 0; i < VALUES.length; i++) {
            if(!Objects.equals(array.VALUES[i], VALUES[i])) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = VALUES.length;
        for(int i = 0; i < VALUES.length; ++i) result = result * 31 + Objects.hashCode(VALUES[i]);
        return result;
    }

    @Override
    public String toString() {
        return "ImmutableArray{" + Arrays.toString(VALUES) + '}';
    }



    public interface Equivalence<T> {

        public boolean equivalent(T a, T b);

    }

}
