package com.bakuard.nutritionManager.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Objects;

public final class Product {

    private static int lastId;


    private final int ID;
    private final String NAME;
    private final String UNIT;
    private final ImmutableMap<Integer, ProductTag> ALL_TAGS;
    private final ImmutableMap<Integer, ProductPrice> PRICES;
    private final ImmutableMap<Integer, ProductCalories> CALORIES;

    public Product() {
        this(--lastId,
                "Новый продукт",
                "киллограмм",
                new ImmutableMap<>(),
                new ImmutableMap<>(),
                new ImmutableMap<>());
    }

    private Product(int id,
                    String name,
                    String unit,
                    ImmutableMap<Integer, ProductPrice> prices,
                    ImmutableMap<Integer, ProductCalories> calories) {
        if(name == null) {
            throw new NullPointerException("Имя продукта не может иметь значение null.");
        } else if(name.isEmpty()) {
            throw new IllegalArgumentException("Имя продукта не может быть пустой строкой.");
        } else if(name.isBlank()) {
            throw new IllegalArgumentException("Имя продукта должно содержать отображаемые символы.");
        } else if(prices == null) {
            throw new NullPointerException("Значения для списка цен не может быть null.");
        } else if(calories == null) {
            throw new NullPointerException("Значения для списка каллорий не может быть null.");
        } else if(unit == null) {
            throw new NullPointerException("Имя ед. измерения кол-ва продукта не может иметь значение null.");
        } else if(unit.isEmpty()) {
            throw new IllegalArgumentException("Имя ед. измерения кол-ва продукта не может быть пустой строкой.");
        } else if(unit.isBlank()) {
            throw new IllegalArgumentException("Имя ед. измерения кол-ва продукта должна содержать отображаемые символы.");
        }

        ID = id;
        NAME = name;
        UNIT = unit;
        PRICES = prices;
        CALORIES = calories;

        HashMap<Integer, ProductTag> allTags = new HashMap<>();
        for(ImmutableMap.Node<Integer, ProductPrice> priceNode : PRICES) {
            priceNode.getValue().getTags().forEach(
                    (ImmutableMap.Node<Integer, ProductTag> node) -> allTags.put(node.getKey(), node.getValue())
            );
        }
        for(ImmutableMap.Node<Integer, ProductCalories> caloriesNode : CALORIES) {
            caloriesNode.getValue().getTags().forEach(
                    (ImmutableMap.Node<Integer, ProductTag> node) -> allTags.put(node.getKey(), node.getValue())
            );
        }
        ALL_TAGS = new ImmutableMap<>(allTags);
    }

    private Product(int id,
                    String name,
                    String unit,
                    ImmutableMap<Integer, ProductTag> allTags,
                    ImmutableMap<Integer, ProductPrice> prices,
                    ImmutableMap<Integer, ProductCalories> calories) {
        if(name == null) {
            throw new NullPointerException("Имя продукта не может иметь значение null.");
        } else if(name.isEmpty()) {
            throw new IllegalArgumentException("Имя продукта не может быть пустой строкой.");
        } else if(name.isBlank()) {
            throw new IllegalArgumentException("Имя продукта должно содержать отображаемые символы.");
        } else if(allTags == null) {
            throw new NullPointerException("Значения для списка всех тегов не может быть null.");
        } else if(prices == null) {
            throw new NullPointerException("Значения для списка цен не может быть null.");
        } else if(calories == null) {
            throw new NullPointerException("Значения для списка каллорий не может быть null.");
        } else if(unit == null) {
            throw new NullPointerException("Имя ед. измерения кол-ва продукта не может иметь значение null.");
        } else if(unit.isEmpty()) {
            throw new IllegalArgumentException("Имя ед. измерения кол-ва продукта не может быть пустой строкой.");
        } else if(unit.isBlank()) {
            throw new IllegalArgumentException("Имя ед. измерения кол-ва продукта должна содержать отображаемые символы.");
        }

        ID = id;
        NAME = name;
        UNIT = unit;
        ALL_TAGS = allTags;
        PRICES = prices;
        CALORIES = calories;
    }

    public int getId() {
        return ID;
    }

    public String getName() {
        return NAME;
    }

    public String getUnit() {
        return UNIT;
    }

    public ImmutableMap<Integer, ProductTag> getAllTags() {
        return ALL_TAGS;
    }

    public ImmutableMap<Integer, ProductPrice> getPrices() {
        return PRICES;
    }

    public ImmutableMap<Integer, ProductCalories> getCalories() {
        return CALORIES;
    }

    public BigDecimal getAveragePrice(MathContext mathContext, ProductTag... tags) {
        if(PRICES.getSize() == 0) return BigDecimal.ZERO;

        ImmutableSet<ProductTag> tagsPattern = new ImmutableSet<>(tags);
        BigDecimal averagePrice = BigDecimal.ZERO;

        for(ImmutableMap.Node<Integer, ProductPrice> node : PRICES) {
            ProductPrice price = node.getValue();
            ProductTag[] pricesTags = new ProductTag[price.getTags().getSize()];
            price.getTags().fillArrayWithValues(pricesTags);
            if(new ImmutableSet<>(pricesTags).containsSet(tagsPattern)) {
                averagePrice = averagePrice.add(price.getPrice());
            }
        }

        return averagePrice.divide(new BigDecimal(PRICES.getSize()), mathContext);
    }

    public BigDecimal getAverageCalories(MathContext mathContext, ProductTag... tags) {
        if(CALORIES.getSize() == 0) return BigDecimal.ZERO;

        ImmutableSet<ProductTag> tagsPattern = new ImmutableSet<>(tags);
        BigDecimal averageCalories = BigDecimal.ZERO;

        for(ImmutableMap.Node<Integer, ProductCalories> node : CALORIES) {
            ProductCalories calories = node.getValue();
            ProductTag[] pricesTags = new ProductTag[calories.getTags().getSize()];
            calories.getTags().fillArrayWithValues(pricesTags);
            if(new ImmutableSet<>(pricesTags).containsSet(tagsPattern)) {
                averageCalories = averageCalories.add(calories.getCalories());
            }
        }

        return averageCalories.divide(new BigDecimal(CALORIES.getSize()), mathContext);
    }

    public boolean isNew() {
        return ID < 0;
    }

    public Product setId(int id) {
        return new Product(
                id,
                NAME,
                UNIT,
                ALL_TAGS,
                PRICES,
                CALORIES
        );
    }

    public Product setName(String name) {
        return new Product(
                ID,
                name,
                UNIT,
                ALL_TAGS,
                PRICES,
                CALORIES
        );
    }

    public Product setUnit(String unit) {
        return new Product(
                ID,
                NAME,
                unit,
                ALL_TAGS,
                PRICES,
                CALORIES
        );
    }

    public Product setPrices(ImmutableMap<Integer, ProductPrice> prices) {
        return new Product(
                ID,
                NAME,
                UNIT,
                prices,
                CALORIES
        );
    }

    public Product setCalories(ImmutableMap<Integer, ProductCalories> calories) {
        return new Product(
                ID,
                NAME,
                UNIT,
                PRICES,
                calories
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return ID == product.ID &&
                NAME.equals(product.NAME) &&
                UNIT.equals(product.UNIT) &&
                ALL_TAGS.equals(product.ALL_TAGS) &&
                PRICES.equals(product.PRICES) &&
                CALORIES.equals(product.CALORIES);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, NAME, UNIT, ALL_TAGS, PRICES, CALORIES);
    }

    @Override
    public String toString() {
        return "Product{" +
                "ID=" + ID +
                ", NAME='" + NAME + '\'' +
                ", UNIT='" + UNIT + '\'' +
                ", ALL_TAGS=" + ALL_TAGS +
                ", PRICES=" + PRICES +
                ", CALORIES=" + CALORIES +
                '}';
    }


    public static final class Builder {

        private int id;
        private String name;
        private String unit;
        private HashMap<Integer, ProductPrice> prices;
        private HashMap<Integer, ProductCalories> calories;

        public Builder() {
            prices = new HashMap<>();
            calories = new HashMap<>();
        }

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setUnit(String unit) {
            this.unit = unit;
            return this;
        }

        public Builder setPrices(HashMap<Integer, ProductPrice> prices) {
            this.prices = prices;
            return this;
        }

        public Builder setCalories(HashMap<Integer, ProductCalories> calories) {
            this.calories = calories;
            return this;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public HashMap<Integer, ProductPrice> getPrices() {
            return prices;
        }

        public HashMap<Integer, ProductCalories> getCalories() {
            return calories;
        }

        public Product build() {
            return new Product(
                    id,
                    name,
                    unit,
                    new ImmutableMap<>(prices),
                    new ImmutableMap<>(calories)
            );
        }

    }

}
