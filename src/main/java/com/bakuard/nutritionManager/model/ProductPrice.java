package com.bakuard.nutritionManager.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;

public final class ProductPrice {

    private static int lastId;


    private final int ID;
    private final BigDecimal PRICE;
    private final ImmutableMap<Integer, ProductTag> TAGS;

    public ProductPrice() {
        this(--lastId, BigDecimal.ZERO, new ImmutableMap<>());
    }

    private ProductPrice(int id,
                         BigDecimal price,
                         ImmutableMap<Integer, ProductTag> tags) {
        if(price == null) throw new NullPointerException("Значение для цены не может быть null.");
        else if(tags == null) throw new NullPointerException("Значение для тегов не может быть null.");

        ID = id;
        PRICE = price;
        TAGS = tags;
    }

    public int getId() {
        return ID;
    }

    public BigDecimal getPrice() {
        return PRICE;
    }

    public ImmutableMap<Integer, ProductTag> getTags() {
        return TAGS;
    }

    public boolean isNew() {
        return ID < 0;
    }

    public ProductPrice setId(int id) {
        return new ProductPrice(
                id,
                PRICE,
                TAGS
        );
    }

    public ProductPrice setPrice(String price) {
        if(price == null) {
            throw new NullPointerException("Цена не может иметь значение null.");
        } else if(price.isEmpty()) {
            throw new IllegalArgumentException("Цена не может быть пустой строкой.");
        } else if(price.isBlank()) {
            throw new IllegalArgumentException("Цена должна содержать отображаемые символы.");
        }

        try {
            return new ProductPrice(
                    ID,
                    new BigDecimal(price),
                    TAGS
            );
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Значение задаваемое для цены должно быть числом.", e);
        }
    }

    public ProductPrice setTags(ImmutableMap<Integer, ProductTag> tags) {
        return new ProductPrice(
                ID,
                PRICE,
                tags
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductPrice that = (ProductPrice) o;
        return ID == that.ID &&
                PRICE.equals(that.PRICE) &&
                TAGS.equals(that.TAGS);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, PRICE, TAGS);
    }

    @Override
    public String toString() {
        return "ProductPrice{" +
                "ID=" + ID +
                ", PRICE=" + PRICE +
                ", TAGS=" + TAGS +
                '}';
    }


    public static final class Builder {

        private int id;
        private BigDecimal price;
        private HashMap<Integer, ProductTag> tags;

        public Builder() {
            tags = new HashMap<>();
        }

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder setTags(HashMap<Integer, ProductTag> tags) {
            this.tags = tags;
            return this;
        }

        public int getId() {
            return id;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public HashMap<Integer, ProductTag> getTags() {
            return tags;
        }

        public ProductPrice build() {
            return new ProductPrice(id, price, new ImmutableMap<>(tags));
        }

    }

}
