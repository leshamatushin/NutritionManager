package com.bakuard.nutritionManager.model.dal;

import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.ProductCalories;
import com.bakuard.nutritionManager.model.ProductTag;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ProductCaloriesDaoImpl implements ProductCaloriesDao {

    private PreparedStatement readCaloriesByProductId;
    private PreparedStatement readCaloriesByProductName;
    private PreparedStatement readCaloriesByProductsSortedByName;
    private PreparedStatement addCalories;
    private PreparedStatement updateCalories;
    private PreparedStatement removeAllNotLinkedCalories;
    private PreparedStatement addTag;
    private PreparedStatement removeTag;

    public ProductCaloriesDaoImpl(DataSource dataSource) throws Exception {
        readCaloriesByProductId = dataSource.getConnection().prepareStatement(
                "SELECT ProductCalories.*, ProductCaloriesToTags.tagId " +
                        "FROM ProductCalories " +
                        "JOIN ProductsToCalories " +
                        "ON ProductCalories.id = ProductsToCalories.caloriesId AND ProductsToCalories.productId = ? " +
                        "LEFT JOIN ProductCaloriesToTags " +
                        "ON ProductCaloriesToTags.caloriesId = ProductsToCalories.caloriesId " +
                        "ORDER BY ProductCalories.id;"
        );
        dataSource.addStatementForClose(readCaloriesByProductId);

        readCaloriesByProductName = dataSource.getConnection().prepareStatement(
                "SELECT ProductCalories.*, ProductCaloriesToTags.tagId " +
                        "FROM ProductCalories " +
                        "JOIN ProductsToCalories " +
                        "ON ProductsToCalories.caloriesId = ProductCalories.id " +
                        "JOIN Products " +
                        "ON ProductsToCalories.productId = Products.id AND Products.name = ? " +
                        "LEFT JOIN ProductCaloriesToTags " +
                        "ON ProductsToCalories.caloriesId = ProductCaloriesToTags.caloriesId " +
                        "ORDER BY ProductCalories.id;"
        );
        dataSource.addStatementForClose(readCaloriesByProductName);

        readCaloriesByProductsSortedByName = dataSource.getConnection().prepareStatement(
                "SELECT ProductCalories.*, ProductsToCalories.productId, ProductCaloriesToTags.tagId " +
                        "FROM ProductsToCalories " +
                        "JOIN (SELECT Products.id FROM Products ORDER BY Products.name LIMIT ? OFFSET ?) AS Pr " +
                        "ON ProductsToCalories.productId = Pr.id " +
                        "JOIN ProductCalories " +
                        "ON ProductsToCalories.caloriesId = ProductCalories.id " +
                        "LEFT JOIN ProductCaloriesToTags " +
                        "ON ProductCaloriesToTags.caloriesId = ProductsToCalories.caloriesId;"
        );
        dataSource.addStatementForClose(readCaloriesByProductsSortedByName);

        addCalories = dataSource.getConnection().prepareStatement(
                "INSERT INTO ProductCalories(calories) VALUES (?);",
                PreparedStatement.RETURN_GENERATED_KEYS
        );
        dataSource.addStatementForClose(addCalories);

        updateCalories = dataSource.getConnection().prepareStatement(
                "UPDATE ProductCalories SET calories=? WHERE id=?;"
        );
        dataSource.addStatementForClose(updateCalories);

        removeAllNotLinkedCalories = dataSource.getConnection().prepareStatement(
                "DELETE FROM ProductCalories WHERE ProductCalories.id NOT IN " +
                        "(SELECT caloriesId FROM ProductsToCalories GROUP BY caloriesId);"
        );
        dataSource.addStatementForClose(removeAllNotLinkedCalories);

        addTag = dataSource.getConnection().prepareStatement(
                "INSERT INTO ProductCaloriesToTags(caloriesId, tagId) VALUES (?, ?)"
        );
        dataSource.addStatementForClose(addTag);

        removeTag = dataSource.getConnection().prepareStatement(
                "DELETE FROM ProductCaloriesToTags WHERE caloriesId=? AND tagId=?;"
        );
        dataSource.addStatementForClose(removeTag);
    }

    @Override
    public HashMap<Integer, ProductCalories> getCaloriesByProductId(
            int productId,
            HashMap<Integer, ProductTag> tags) throws Exception {
        HashMap<Integer, ProductCalories> result = new HashMap<>();

        readCaloriesByProductId.setInt(1, productId);
        try(ResultSet pricesReader = readCaloriesByProductId.executeQuery()) {
            ProductCalories.Builder builder = null;
            while(pricesReader.next()) {
                int priceId = pricesReader.getInt("id");
                if(builder == null) {
                    builder = new ProductCalories.Builder();
                    builder.setId(priceId);
                    builder.setCalories(pricesReader.getBigDecimal("calories"));
                } else if(priceId != builder.getId()) {
                    result.put(builder.getId(), builder.build());
                    builder = new ProductCalories.Builder();
                    builder.setId(priceId);
                    builder.setCalories(pricesReader.getBigDecimal("calories"));
                }
                ProductTag tag = tags.get(pricesReader.getInt("tagId"));
                if(tag != null) builder.getTags().put(tag.getId(), tag);
            }
            if(builder != null) result.put(builder.getId(), builder.build());
        }

        return result;
    }

    @Override
    public HashMap<Integer, ProductCalories> getCaloriesByProductName(
            String productName,
            HashMap<Integer, ProductTag> tags) throws Exception {
        HashMap<Integer, ProductCalories> result = new HashMap<>();

        readCaloriesByProductName.setString(1, productName);
        try(ResultSet pricesReader = readCaloriesByProductId.executeQuery()) {
            ProductCalories.Builder builder = null;
            while(pricesReader.next()) {
                int priceId = pricesReader.getInt("id");
                if(builder == null) {
                    builder = new ProductCalories.Builder();
                    builder.setId(priceId);
                    builder.setCalories(pricesReader.getBigDecimal("calories"));
                } else if(priceId != builder.getId()) {
                    result.put(builder.getId(), builder.build());
                    builder = new ProductCalories.Builder();
                    builder.setId(priceId);
                    builder.setCalories(pricesReader.getBigDecimal("calories"));
                }
                ProductTag tag = tags.get(pricesReader.getInt("tagId"));
                if(tag != null) builder.getTags().put(tag.getId(), tag);
            }
            if(builder != null) result.put(builder.getId(), builder.build());
        }

        return result;
    }

    @Override
    public void fillProductsSortedByNameWithCalories(int fromIndex, int count,
                                                     HashMap<Integer, ProductTag> tags,
                                                     LinkedHashMap<Integer, Product.Builder> products) throws Exception {
        readCaloriesByProductsSortedByName.setInt(1, count);
        readCaloriesByProductsSortedByName.setInt(2, fromIndex);
        try(ResultSet pricesReader = readCaloriesByProductsSortedByName.executeQuery()) {
            ProductCalories.Builder caloriesBuilder = null;
            Product.Builder productBuilder = null;
            while(pricesReader.next()) {
                int caloriesId = pricesReader.getInt("id");
                if(caloriesBuilder == null || caloriesId != caloriesBuilder.getId()) {
                    if(caloriesBuilder != null) productBuilder.getCalories().put(caloriesBuilder.getId(), caloriesBuilder.build());
                    productBuilder = products.get(pricesReader.getInt("productId"));
                    caloriesBuilder = new ProductCalories.Builder()
                            .setId(caloriesId)
                            .setCalories(pricesReader.getBigDecimal("calories"));
                }
                ProductTag tag = tags.get(pricesReader.getInt("tagId"));
                if(tag != null) caloriesBuilder.getTags().put(tag.getId(), tag);
            }
            if(caloriesBuilder != null) productBuilder.getCalories().put(caloriesBuilder.getId(), caloriesBuilder.build());
        }
    }

    @Override
    public ProductCalories add(ProductCalories calories) throws Exception {
        addCalories.setBigDecimal(1, calories.getCalories());
        addCalories.executeUpdate();

        ResultSet generatedIdReader = addCalories.getGeneratedKeys();
        generatedIdReader.next();
        int generatedId = generatedIdReader.getInt("id");
        generatedIdReader.close();

        return calories.setId(generatedId);
    }

    @Override
    public ProductCalories update(ProductCalories calories) throws Exception {
        updateCalories.setBigDecimal(1, calories.getCalories());
        updateCalories.setInt(2, calories.getId());
        updateCalories.executeUpdate();
        return calories;
    }

    @Override
    public void removeAllNotLinkedCalories() throws Exception {
        removeAllNotLinkedCalories.executeUpdate();
    }

    @Override
    public void addTag(ProductCalories calories, ProductTag tag) throws Exception {
        addTag.setInt(1, calories.getId());
        addTag.setInt(2, tag.getId());
        addTag.executeUpdate();
    }

    @Override
    public void removeTag(ProductCalories calories, ProductTag tag) throws Exception {
        removeTag.setInt(1, calories.getId());
        removeTag.setInt(2, tag.getId());
        removeTag.executeUpdate();
    }

}
