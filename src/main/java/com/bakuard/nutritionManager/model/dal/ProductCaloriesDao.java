package com.bakuard.nutritionManager.model.dal;

import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.ProductCalories;
import com.bakuard.nutritionManager.model.ProductTag;

import java.util.HashMap;
import java.util.LinkedHashMap;

public interface ProductCaloriesDao {

    public HashMap<Integer, ProductCalories> getCaloriesByProductId(
            int productId, HashMap<Integer, ProductTag> tags) throws Exception;

    public HashMap<Integer, ProductCalories> getCaloriesByProductName(
            String productName, HashMap<Integer, ProductTag> tags) throws Exception;

    public void fillProductsSortedByNameWithCalories(
            int fromIndex, int count,
            HashMap<Integer, ProductTag> tags,
            LinkedHashMap<Integer, Product.Builder> products) throws Exception;

    public ProductCalories add(ProductCalories calories) throws Exception;

    public ProductCalories update(ProductCalories calories) throws Exception;

    public void removeAllNotLinkedCalories() throws Exception;

    public void addTag(ProductCalories calories, ProductTag tag) throws Exception;

    public void removeTag(ProductCalories calories, ProductTag tag) throws Exception;

}
