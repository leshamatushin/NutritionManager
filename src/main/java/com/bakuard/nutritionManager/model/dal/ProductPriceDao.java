package com.bakuard.nutritionManager.model.dal;

import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.ProductPrice;
import com.bakuard.nutritionManager.model.ProductTag;

import java.util.HashMap;
import java.util.LinkedHashMap;

public interface ProductPriceDao {

    public HashMap<Integer, ProductPrice> getPricesByProductId(
            int productId,
            HashMap<Integer, ProductTag> tags) throws Exception;

    public HashMap<Integer, ProductPrice> getPricesByProductName(
            String productName,
            HashMap<Integer, ProductTag> tags) throws Exception;

    public void fillProductsSortedByNameWithPrice(
            int fromIndex, int count,
            HashMap<Integer, ProductTag> tags,
            LinkedHashMap<Integer, Product.Builder> products) throws Exception;

    public ProductPrice add(ProductPrice price) throws Exception;

    public ProductPrice update(ProductPrice price) throws Exception;

    public void removeAllNotLinkedPrices() throws Exception;

    public void addTag(ProductPrice price, ProductTag tag) throws Exception;

    public void removeTag(ProductPrice price, ProductTag tag) throws Exception;

}
