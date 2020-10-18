package com.bakuard.nutritionManager.model;

import java.util.Objects;

public final class ProductTag {

    private static int lastId;


    private final int ID;
    private final String NAME;
    private final String VALUE;

    public ProductTag(int id,
                      String name,
                      String value) {
        if(name == null) {
            throw new NullPointerException("Имя тега не может иметь значение null.");
        } else if(name.isEmpty()) {
            throw new IllegalArgumentException("Имя тега не может быть пустой строкой.");
        } else if(name.isBlank()) {
            throw new IllegalArgumentException("Имя тега должно содержать отображаемые символы.");
        } else if(value == null) {
            throw new NullPointerException("Значение тега не может иметь значение null.");
        } else if(value.isEmpty()) {
            throw new IllegalArgumentException("Значение тега не может быть пустой строкой.");
        } else if(value.isBlank()) {
            throw new IllegalArgumentException("Значение тега должно содержать отображаемые символы.");
        }

        ID = id;
        NAME = name.strip();
        VALUE = value.strip();
    }

    public ProductTag() {
        this(--lastId, "Имя тега", "Значение тега");
    }

    public int getId() {
        return ID;
    }

    public String getName() {
        return NAME;
    }

    public String getValue() {
        return VALUE;
    }

    public boolean isNew() {
        return ID < 0;
    }

    public ProductTag setId(int id) {
        return new ProductTag(
                id,
                NAME, VALUE
        );
    }

    public ProductTag setName(String name) {
        return new ProductTag(
                ID,
                name,
                VALUE
        );
    }

    public ProductTag setValue(String value) {
        return new ProductTag(
                ID,
                NAME,
                value
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductTag that = (ProductTag) o;
        return ID == that.ID &&
                NAME.equals(that.NAME) &&
                VALUE.equals(that.VALUE);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, NAME, VALUE);
    }

    @Override
    public String toString() {
        return "ProductTag{" +
                "ID=" + ID +
                ", NAME='" + NAME + '\'' +
                ", VALUE='" + VALUE + '\'' +
                '}';
    }

}
