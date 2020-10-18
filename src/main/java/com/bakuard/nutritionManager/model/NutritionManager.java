package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.model.dal.*;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

public final class NutritionManager {

    private DataSource dataSource;
    private ProductDao productDao;
    private ProductPriceDao productPriceDao;
    private ProductCaloriesDao productCaloriesDao;
    private ProductTagDao productTagDao;

    private final MathContext MATH_CONTEXT;

    private final Logger DEBUG_LOGGER;

    public NutritionManager(Logger debugLogger) {
        MATH_CONTEXT = new MathContext(16, RoundingMode.CEILING);
        DEBUG_LOGGER = debugLogger;
    }

    public void connect() throws Exception {
        dataSource = new DataSource();
        productTagDao = new ProductTagDaoImpl(dataSource);
        productPriceDao = new ProductPriceDaoImpl(dataSource);
        productCaloriesDao = new ProductCaloriesDaoImpl(dataSource);
        productDao = new ProductDaoImpl(dataSource);
    }

    public void disconnect() throws Exception {
        dataSource.close();
    }


    public Product save(Product oldProduct, Product newProduct) throws Exception {
        try {
            Product.Builder builder = new Product.Builder();

            if(newProduct.isNew()) {
                newProduct = productDao.add(newProduct);
                builder.setName(newProduct.getName());
                builder.setUnit(newProduct.getUnit());
                builder.setId(newProduct.getId());

                for(ImmutableMap.Node<Integer, ProductPrice> priceNode : newProduct.getPrices()) {
                    ProductPrice newPrice = priceNode.getValue();
                    newPrice = save(null, newPrice);
                    productDao.addPrice(newProduct, newPrice);
                    builder.getPrices().put(newPrice.getId(), newPrice);
                }

                for(ImmutableMap.Node<Integer, ProductCalories> caloriesNode : newProduct.getCalories()) {
                    ProductCalories newCalories = caloriesNode.getValue();
                    newCalories = save(null, newCalories);
                    productDao.addCalories(newProduct, newCalories);
                    builder.getCalories().put(newCalories.getId(), newCalories);
                }

                newProduct = builder.build();
                dataSource.commit();
            } else if (!newProduct.equals(oldProduct)) {
                newProduct = productDao.update(newProduct);
                builder.setName(newProduct.getName());
                builder.setUnit(newProduct.getUnit());
                builder.setId(newProduct.getId());

                for(ImmutableMap.Node<Integer, ProductPrice> priceNode : newProduct.getPrices()) {
                    ProductPrice newPrice = priceNode.getValue();
                    ProductPrice oldPrice = oldProduct.getPrices().get(newPrice.getId());
                    if(oldPrice == null) {
                        newPrice = save(newPrice, newPrice);
                        productDao.addPrice(newProduct, newPrice);
                    } else if(!oldPrice.equals(newPrice)) {
                        newPrice = save(oldPrice, newPrice);
                    }
                    builder.getPrices().put(newPrice.getId(), newPrice);
                }

                for(ImmutableMap.Node<Integer, ProductCalories> caloriesNode : newProduct.getCalories()) {
                    ProductCalories newCalories = caloriesNode.getValue();
                    ProductCalories oldCalories = oldProduct.getCalories().get(newCalories.getId());
                    if(oldCalories == null) {
                        newCalories = save(newCalories, newCalories);
                        productDao.addCalories(newProduct, newCalories);
                    } else if(!oldCalories.equals(newCalories)) {
                        newCalories = save(oldCalories, newCalories);
                    }
                    builder.getCalories().put(newCalories.getId(), newCalories);
                }

                for(ImmutableMap.Node<Integer, ProductPrice> priceNode : oldProduct.getPrices()) {
                    ProductPrice oldPrice = priceNode.getValue();
                    if(!newProduct.getPrices().containsKey(oldPrice.getId())) {
                        productDao.removePrice(newProduct, oldPrice);
                    }
                }

                for(ImmutableMap.Node<Integer, ProductCalories> caloriesNode : oldProduct.getCalories()) {
                    ProductCalories oldCalories = caloriesNode.getValue();
                    if(!newProduct.getCalories().containsKey(oldCalories.getId())) {
                        productDao.removeCalories(newProduct, oldCalories);
                    }
                }

                productPriceDao.removeAllNotLinkedPrices();
                productCaloriesDao.removeAllNotLinkedCalories();
                productTagDao.removeAllNotLinkedTags();

                newProduct = builder.build();
                dataSource.commit();
            }
        } catch (Exception e) {
            dataSource.rollback();
            throw e;
        }

        return newProduct;
    }

    public void removeProduct(int productId) throws Exception {
        try {
            productDao.removeProductById(productId);
            productPriceDao.removeAllNotLinkedPrices();
            productCaloriesDao.removeAllNotLinkedCalories();
            productTagDao.removeAllNotLinkedTags();

            dataSource.commit();
        } catch(Exception e) {
            dataSource.rollback();
            throw e;
        }
    }

    public Product getProductById(int id) throws Exception {
        HashMap<Integer, ProductTag> tags = productTagDao.getTagsByProductId(id);

        Product product = productDao.getProductById(
                id,
                productPriceDao.getPricesByProductId(id, tags),
                productCaloriesDao.getCaloriesByProductId(id, tags)
        );

        return product;
    }

    public Product getProductByName(String name) throws Exception {
        HashMap<Integer, ProductTag> tags = productTagDao.getTagsByProductName(name);

        Product product = productDao.getProductByName(
                name,
                productPriceDao.getPricesByProductName(name, tags),
                productCaloriesDao.getCaloriesByProductName(name, tags)
        );

        return product;
    }

    public List<Product> getProductsSortedByName(int fromIndex, int count) throws Exception {
        if(fromIndex < 0)
            throw new IllegalArgumentException("Начальный индекс должен быть больше или равен нулю.");
        else if(count < 0)
            throw new IllegalArgumentException("Кол-во элементов должно быть больше или равно нулю.");
        else if(count == 0) return new ArrayList<>();

        HashMap<Integer, ProductTag> tags = productTagDao.getTagsForProductsSortedByName(fromIndex, count);
        LinkedHashMap<Integer, Product.Builder> productBuilders = productDao.getProductsSortedByName(fromIndex, count);
        productPriceDao.fillProductsSortedByNameWithPrice(fromIndex, count, tags, productBuilders);
        productCaloriesDao.fillProductsSortedByNameWithCalories(fromIndex, count, tags, productBuilders);

        ArrayList<Product> products = new ArrayList<>();
        for(Product.Builder builder : productBuilders.values()) products.add(builder.build());

        return products;
    }


    public List<String> getAllTagNames() throws Exception {
        return productTagDao.getAllTagsName();
    }

    public List<ProductTag> getTagsByName(String name) throws Exception {
        return productTagDao.getTagsByName(name);
    }


    public MathContext getMathContext() {
        return MATH_CONTEXT;
    }


    private ProductPrice save(ProductPrice oldPrice, ProductPrice newPrice) throws Exception {
        if(newPrice.isNew()) {
            ProductPrice.Builder builder = new ProductPrice.Builder();

            newPrice = productPriceDao.add(newPrice);
            builder.setPrice(newPrice.getPrice());
            builder.setId(newPrice.getId());

            for(ImmutableMap.Node<Integer, ProductTag> node : newPrice.getTags()) {
                ProductTag newTag = node.getValue();
                newTag = save(newTag);
                productPriceDao.addTag(newPrice, newTag);
                builder.getTags().put(newTag.getId(), newTag);
            }

            return builder.build();
        } else if (!newPrice.equals(oldPrice)) {
            ProductPrice.Builder builder = new ProductPrice.Builder();

            newPrice = productPriceDao.update(newPrice);
            builder.setPrice(newPrice.getPrice());
            builder.setId(newPrice.getId());

            for(ImmutableMap.Node<Integer, ProductTag> node : newPrice.getTags()) {
                ProductTag newTag = node.getValue();
                ProductTag oldTag = oldPrice.getTags().get(newTag.getId());
                if(oldTag == null) {
                    newTag = save(newTag);
                    productPriceDao.addTag(newPrice, newTag);
                } else if(!oldTag.equals(newTag)) {
                    productPriceDao.removeTag(newPrice, oldTag);
                    newTag = save(newTag);
                    productPriceDao.addTag(newPrice, newTag);
                }
                builder.getTags().put(newTag.getId(), newTag);
            }

            for(ImmutableMap.Node<Integer, ProductTag> node : oldPrice.getTags()) {
                ProductTag oldTag = node.getValue();
                if(!newPrice.getTags().containsKey(oldTag.getId())) {
                    productPriceDao.removeTag(newPrice, oldTag);
                }
            }

            return builder.build();
        } else {
            return newPrice;
        }
    }

    private ProductCalories save(ProductCalories oldCalories, ProductCalories newCalories) throws Exception {
        if(newCalories.isNew()) {
            ProductCalories.Builder builder = new ProductCalories.Builder();
            newCalories = productCaloriesDao.add(newCalories);
            builder.setCalories(newCalories.getCalories());
            builder.setId(newCalories.getId());

            for(ImmutableMap.Node<Integer, ProductTag> node : newCalories.getTags()) {
                ProductTag newTag = node.getValue();
                newTag = save(newTag);
                productCaloriesDao.addTag(newCalories, newTag);
                builder.getTags().put(newTag.getId(), newTag);
            }

            return builder.build();
        } else if(!newCalories.equals(oldCalories)) {
            ProductCalories.Builder builder = new ProductCalories.Builder();
            newCalories = productCaloriesDao.update(newCalories);
            builder.setCalories(newCalories.getCalories());
            builder.setId(newCalories.getId());

            for(ImmutableMap.Node<Integer, ProductTag> node : newCalories.getTags()) {
                ProductTag newTag = node.getValue();
                ProductTag oldTag = oldCalories.getTags().get(newTag.getId());
                if(oldTag == null) {
                    newTag = save(newTag);
                    productCaloriesDao.addTag(newCalories, newTag);
                } else if(!oldTag.equals(newTag)) {
                    productCaloriesDao.removeTag(newCalories, oldTag);
                    newTag = save(newTag);
                    productCaloriesDao.addTag(newCalories, newTag);
                }
                builder.getTags().put(newTag.getId(), newTag);
            }

            for(ImmutableMap.Node<Integer, ProductTag> node : oldCalories.getTags()) {
                ProductTag oldTag = node.getValue();
                if(!newCalories.getTags().containsKey(oldTag.getId())) {
                    productCaloriesDao.removeTag(newCalories, oldTag);
                }
            }

            return builder.build();
        } else {
            return newCalories;
        }
    }

    private ProductTag save(ProductTag tag) throws Exception {
        ProductTag duplicateTag = productTagDao.getTagByNameAndValue(tag.getName(), tag.getValue());
        if(duplicateTag != null) {
            tag = duplicateTag;
        } else {
            /*
            * Значение тега не изменяется, поскольку на него могут ссылаться из разных мест.
            * Вместо этого создадим новый тег с нужным значением.
            * Тег со старым значением будет удален при вызове метода ProductTagDao::removeAllNotLinkedTags,
            * если на него не ссылается ни одна запись из других таблиц.
             */
            tag = productTagDao.add(tag);
        }

        return tag;
    }

}
