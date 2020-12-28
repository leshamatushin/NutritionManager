package com.bakuard.nutritionManager.model;

import java.util.*;
import java.util.function.Consumer;

public final class ImmutableMap<K, V> implements Iterable<ImmutableMap.Node<K, V>> {

    private static final float LOAD_FACTOR = 0.75F;
    private static final int MAX_CAPACITY = 1073741824;

    private final int SIZE;
    private final Node<K, V>[] TABLE;

    @SuppressWarnings("unchecked")
    public ImmutableMap() {
        SIZE = 0;
        TABLE = new Node[16];
    }

    @SuppressWarnings("unchecked")
    public ImmutableMap(Map<K, V> map) {
        SIZE = map.size();
        TABLE = new Node[calculateCapacity(SIZE)];

        for(Map.Entry<K, V> entry : map.entrySet()) {
            int bucketIndex = keyToBucketIndex(entry.getKey(), TABLE.length);
            TABLE[bucketIndex] = new Node<>(entry.getKey(), entry.getValue(), TABLE[bucketIndex]);
        }
    }

    private ImmutableMap(Node<K, V>[] table, int size) {
        SIZE = size;
        TABLE = table;
    }

    public ImmutableMap<K, V> put(K key, V value) {
        ImmutableMap<K, V> newMap = this;
        Node<K, V> node = getNode(key, TABLE);
        if(node == null) {
            newMap = new ImmutableMap<>(
                    copyTable(calculateCapacity(SIZE + 1)), SIZE + 1);
            int bucketIndex = keyToBucketIndex(key, newMap.TABLE.length);
            newMap.TABLE[bucketIndex] = new Node<>(key, value, newMap.TABLE[bucketIndex]);
            return newMap;
        } else if(!node.VALUE.equals(value)){
            Node<K, V>[] newTable = new Node[TABLE.length];
            forEach((Node<K, V> currentNode) -> {
                int bucketIndex = keyToBucketIndex(currentNode.KEY, newTable.length);
                if(!Objects.equals(key, currentNode.KEY)) {
                    newTable[bucketIndex] = new Node<>(currentNode.KEY, currentNode.VALUE, newTable[bucketIndex]);
                } else {
                    newTable[bucketIndex] = new Node<>(currentNode.KEY, value, newTable[bucketIndex]);
                }
            });
            newMap = new ImmutableMap<>(newTable, SIZE);
        }
        return newMap;
    }

    public V get(K key) {
        Node<K, V> node = getNode(key, TABLE);
        return node == null ? null : node.VALUE;
    }

    public ImmutableMap<K, V> remove(K key) {
        ImmutableMap<K, V> newMap = this;
        if(containsKey(key)) {
            Node<K, V>[] newTable = new Node[TABLE.length];
            forEach((Node<K, V> currentNode) -> {
                if(!Objects.equals(key, currentNode.KEY)) {
                    int bucketIndex = keyToBucketIndex(currentNode.KEY, newTable.length);
                    newTable[bucketIndex] = new Node<>(currentNode.KEY, currentNode.VALUE, newTable[bucketIndex]);
                }
            });
            newMap = new ImmutableMap<>(newTable, SIZE - 1);
        }
        return newMap;
    }

    public int getSize() {
        return SIZE;
    }

    public boolean isEmpty() {
        return SIZE == 0;
    }

    public boolean containsKey(K key) {
        return getNode(key, TABLE) != null;
    }

    public ImmutableSet<K> getKeys() {
        K[] keys = (K[])(new Object[SIZE]);
        for(int i = 0, arrayIndex = 0; i < TABLE.length; i++) {
            Node<K, V> currentNode = TABLE[i];
            while(currentNode != null) {
                keys[arrayIndex++] = currentNode.KEY;
                currentNode = currentNode.next;
            }
        }
        return new ImmutableSet<>(keys);
    }

    public void fillArrayWithValues(V[] array) {
        if(array.length < SIZE) {
            throw new IllegalArgumentException(
                    "Размер передаваемого массива не может быть меньше значения возвращаемого " +
                    "getSize(). Значение getSize() = " + SIZE + ", array.length = " + array.length);
        }

        for(int i = 0, arrayIndex = 0; i < TABLE.length; i++) {
            Node<K, V> currentNode = TABLE[i];
            while(currentNode != null) {
                array[arrayIndex++] = currentNode.VALUE;
                currentNode = currentNode.next;
            }
        }
    }

    @Override
    public Iterator<Node<K, V>> iterator() {
        return new Iterator<>() {
            private int currentIndex;
            private Node<K, V> currentNode;

            {
                while(currentNode == null && currentIndex < TABLE.length) {
                    currentNode = TABLE[currentIndex++];
                }
            }

            @Override
            public boolean hasNext() {
                return currentNode != null;
            }

            @Override
            public Node<K, V> next() {
                if(currentNode == null) {
                    throw new NoSuchElementException();
                } else {
                    Node<K, V> result = currentNode;
                    currentNode = currentNode.next;
                    while(currentNode == null && currentIndex < TABLE.length) {
                        currentNode = TABLE[currentIndex++];
                    }
                    return result;
                }
            }
        };
    }

    @Override
    public void forEach(Consumer<? super Node<K, V>> action) {
        for(int i = 0; i < TABLE.length; i++) {
            Node<K, V> currentNode = TABLE[i];
            while(currentNode != null) {
                action.accept(currentNode);
                currentNode = currentNode.next;
            }
        }
    }

    @Override
    public int hashCode() {
        int result = 31;
        result = result * 31 + SIZE;
        for(int i = 0; i < TABLE.length; i++) {
            Node<K, V> currentNode = TABLE[i];
            while(currentNode != null) {
                result += currentNode.hashCode();
                currentNode = currentNode.next;
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableMap<K, V> that = (ImmutableMap<K, V>) o;

        if(SIZE != that.SIZE) return false;

        boolean result = true;
        for(int i = 0; i < TABLE.length && result; i++) {
            Node<K, V> currentNode = TABLE[i];
            while(currentNode != null && result) {
                result = currentNode.equals(that.getNode(currentNode.KEY, that.TABLE));
                currentNode = currentNode.next;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder items = new StringBuilder();
        forEach((Node<K, V> node) -> items.append(node).append(','));
        if(items.length() > 0) items.deleteCharAt(items.length() - 1);
        return "ImmutableMap{" +
                "SIZE=" + SIZE +
                ", TABLE=[" + items + ']' +
                '}';
    }


    @SuppressWarnings("unchecked")
    private Node<K, V>[] copyTable(int newCapacity) {
        Node<K, V>[] newTable = new Node[newCapacity];

        forEach((Node<K, V> node) -> {
            int bucketIndex = keyToBucketIndex(node.KEY, newTable.length);
            newTable[bucketIndex] = new Node<>(node.KEY, node.VALUE, newTable[bucketIndex]);
        });

        return newTable;
    }

    private Node<K, V> getNode(K key, Node<K, V>[] table) {
        Node<K, V> currentNode = table[keyToBucketIndex(key, table.length)];
        while(currentNode != null && !Objects.equals(currentNode.KEY, key)) currentNode = currentNode.next;
        return currentNode;
    }

    private int calculateCapacity(int newSize) {
        int newCapacity = (int)(newSize/LOAD_FACTOR);

        if(newCapacity > MAX_CAPACITY) newCapacity = MAX_CAPACITY;
        else if(newCapacity < 16) newCapacity = 16;
        else if((newCapacity & (newCapacity - 1)) != 0) newCapacity = roundUpToPowerOfTwo(newCapacity);

        return newCapacity;
    }

    private int roundUpToPowerOfTwo(int size) {
        size |= size >> 1;
        size |= size >> 2;
        size |= size >> 4;
        size |= size >> 8;
        size |= size >> 16;
        return ++size;
    }

    private int keyToBucketIndex(K key, int lengthTable) {
        return (lengthTable - 1) & hash(key);
    }

    private int hash(K key) {
        int hash = Objects.hashCode(key);
        return hash ^ hash >>> 16;
    }


    public static final class Node<K, V> {

        final K KEY;
        final V VALUE;
        Node<K, V> next;

        Node(K key, V value, Node<K, V> next) {
            KEY = key;
            VALUE = value;
            this.next = next;
        }

        public K getKey() {
            return KEY;
        }

        public V getValue() {
            return VALUE;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node<?, ?> node = (Node<?, ?>) o;
            return Objects.equals(KEY, node.KEY) &&
                    Objects.equals(VALUE, node.VALUE);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(KEY) ^ Objects.hashCode(VALUE);
        }

        @Override
        public String toString() {
            return "Node{" +
                    "KEY=" + KEY +
                    ", VALUE=" + VALUE +
                    '}';
        }

    }

}
