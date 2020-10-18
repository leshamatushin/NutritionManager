package com.bakuard.nutritionManager.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;

public final class ProductCalories {

    private static int lastId;


    private final int ID;
    private final BigDecimal CALORIES;
    private final ImmutableMap<Integer, ProductTag> TAGS;

    public ProductCalories() {
        this(--lastId, BigDecimal.ZERO, new ImmutableMap<>());
    }

    private ProductCalories(int id,
                            BigDecimal calories,
                            ImmutableMap<Integer, ProductTag> tags) {
        if(calories == null) throw new NullPointerException("Значение для каллорий не может быть null.");
        else if(tags == null) throw new NullPointerException("Значение для тегов не может быть null.");

        ID = id;
        CALORIES = calories;
        TAGS = tags;
    }

    public int getId() {
        return ID;
    }

    public BigDecimal getCalories() {
        return CALORIES;
    }

    public ImmutableMap<Integer, ProductTag> getTags() {
        return TAGS;
    }

    public boolean isNew() {
        return ID < 0;
    }

    public ProductCalories setId(int id) {
        return new ProductCalories(
                id,
                CALORIES,
                TAGS
        );
    }

    public ProductCalories setPrice(String calories) {
        if(calories == null) {
            throw new NullPointerException("Значение каллорий не может иметь значение null.");
        } else if(calories.isEmpty()) {
            throw new IllegalArgumentException("Значение каллорий не может быть пустой строкой.");
        } else if(calories.isBlank()) {
            throw new IllegalArgumentException("Значение каллорий должно содержать отображаемые символы.");
        }

        try {
            return new ProductCalories(
                    ID,
                    new BigDecimal(calories),
                    TAGS
            );
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Значение задаваемое для калорий должно быть числом.", e);
        }
    }

    public ProductCalories setTags(ImmutableMap<Integer, ProductTag> tags) {
        return new ProductCalories(
                ID,
                CALORIES,
                tags
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCalories that = (ProductCalories) o;
        return ID == that.ID &&
                CALORIES.equals(that.CALORIES) &&
                TAGS.equals(that.TAGS);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, CALORIES, TAGS);
    }

    @Override
    public String toString() {
        return "ProductCalories{" +
                "ID=" + ID +
                ", CALORIES=" + CALORIES +
                ", TAGS=" + TAGS +
                '}';
    }


    public static final class Builder {

        private int id;
        private BigDecimal calories;
        private HashMap<Integer, ProductTag> tags;

        public Builder() {
            tags = new HashMap<>();
        }

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setCalories(BigDecimal calories) {
            this.calories = calories;
            return this;
        }

        public Builder setTags(HashMap<Integer, ProductTag> tags) {
            this.tags = tags;
            return this;
        }

        public int getId() {
            return id;
        }

        public BigDecimal getCalories() {
            return calories;
        }

        public HashMap<Integer, ProductTag> getTags() {
            return tags;
        }

        public ProductCalories build() {
            return new ProductCalories(id, calories, new ImmutableMap<>(tags));
        }

    }

}
