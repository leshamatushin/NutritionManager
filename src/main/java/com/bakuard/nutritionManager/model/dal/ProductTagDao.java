package com.bakuard.nutritionManager.model.dal;

import com.bakuard.nutritionManager.model.ProductTag;

import java.util.HashMap;
import java.util.List;

public interface ProductTagDao {

    public HashMap<Integer, ProductTag> getTagsByProductId(int productId) throws Exception;

    public HashMap<Integer, ProductTag> getTagsByProductName(String productName) throws Exception;

    public HashMap<Integer, ProductTag> getTagsForProductsSortedByName(int fromIndex, int count) throws Exception;

    public List<String> getAllTagsName() throws Exception;

    public List<ProductTag> getTagsByName(String name) throws Exception;

    public ProductTag getTagByNameAndValue(String name, String value) throws Exception;

    public ProductTag add(ProductTag tag) throws Exception;

    public ProductTag update(ProductTag tag) throws Exception;

    public void removeAllNotLinkedTags() throws Exception;

}
