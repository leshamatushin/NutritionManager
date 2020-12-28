package com.bakuard.nutritionManager.model.dal;

import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.ProductCalories;
import com.bakuard.nutritionManager.model.ProductPrice;

import java.util.HashMap;
import java.util.LinkedHashMap;

public interface ProductDao {

    public Product getProductById(int id,
                                  HashMap<Integer, ProductPrice> prices,
                                  HashMap<Integer, ProductCalories> calories) throws Exception;

    public Product getProductByName(String productName,
                                    HashMap<Integer, ProductPrice> prices,
                                    HashMap<Integer, ProductCalories> calories) throws Exception;

    public LinkedHashMap<Integer, Product.Builder> getProductsSortedByName(int fromIndex, int count) throws Exception;

    public Product add(Product product) throws Exception;

    public Product update(Product product) throws Exception;

    public void removeProductById(int productId) throws Exception;

    public void addPrice(Product product, ProductPrice price) throws Exception;

    public void addCalories(Product product, ProductCalories calories) throws Exception;

    public void removePrice(Product product, ProductPrice price) throws Exception;

    public void removeCalories(Product product, ProductCalories calories) throws Exception;

}
