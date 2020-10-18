package com.bakuard.nutritionManager.model.dal;

import com.bakuard.nutritionManager.model.ProductTag;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProductTagDaoImpl implements ProductTagDao {

    private PreparedStatement readTagsByProductId;
    private PreparedStatement readTagsByProductName;
    private PreparedStatement readTagsByProductsSortedByName;
    private PreparedStatement readAllTagNames;
    private PreparedStatement readTagsByName;
    private PreparedStatement readTagByNameAndValue;
    private PreparedStatement addTag;
    private PreparedStatement updateTag;
    private PreparedStatement removeAllNotLinkedTags;

    public ProductTagDaoImpl(DataSource dataSource) throws Exception {
        readTagsByProductId = dataSource.getConnection().prepareStatement(
                "(SELECT ProductTags.* FROM ProductTags " +
                        "JOIN ProductPricesToTags ON ProductPricesToTags.tagId = ProductTags.id " +
                        "JOIN ProductsToPrices ON ProductsToPrices.priceId = ProductPricesToTags.priceId " +
                        "JOIN Products ON products.id = ?)" +
                        "UNION " +
                        "(SELECT ProductTags.* FROM ProductTags " +
                        "JOIN ProductCaloriesToTags ON ProductCaloriesToTags.tagId = ProductTags.id " +
                        "JOIN ProductsToCalories ON ProductsToCalories.caloriesId = ProductCaloriesToTags.caloriesId " +
                        "JOIN Products ON products.id = ?);"
        );
        dataSource.addStatementForClose(readTagsByProductId);

        readTagsByProductName = dataSource.getConnection().prepareStatement(
                "(SELECT ProductTags.* FROM ProductTags " +
                        "JOIN ProductPricesToTags ON ProductPricesToTags.tagId = ProductTags.id " +
                        "JOIN ProductsToPrices ON ProductsToPrices.priceId = ProductPricesToTags.priceId " +
                        "JOIN Products ON products.name = ?)" +
                        "UNION " +
                        "(SELECT ProductTags.* FROM ProductTags " +
                        "JOIN ProductCaloriesToTags ON ProductCaloriesToTags.tagId = ProductTags.id " +
                        "JOIN ProductsToCalories ON ProductsToCalories.caloriesId = ProductCaloriesToTags.caloriesId " +
                        "JOIN Products ON products.name = ?);"
        );
        dataSource.addStatementForClose(readTagsByProductName);

        readTagsByProductsSortedByName = dataSource.getConnection().prepareStatement(
                "(SELECT ProductTags.* FROM ProductTags " +
                        "JOIN ProductPricesToTags ON ProductPricesToTags.tagId = ProductTags.id " +
                        "JOIN ProductsToPrices ON ProductsToPrices.priceId = ProductPricesToTags.priceId " +
                        "JOIN (SELECT Products.id FROM Products ORDER BY Products.name LIMIT ? OFFSET ?) AS Pr " +
                        "ON Pr.id = ProductsToPrices.productId) " +
                        "UNION " +
                        "(SELECT ProductTags.* FROM ProductTags " +
                        "JOIN ProductCaloriesToTags ON ProductCaloriesToTags.tagId = ProductTags.id " +
                        "JOIN ProductsToCalories ON ProductsToCalories.caloriesId = ProductCaloriesToTags.caloriesId " +
                        "JOIN (SELECT Products.id FROM Products ORDER BY Products.name LIMIT ? OFFSET ?) AS Pr " +
                        "ON Pr.id = ProductsToCalories.productId);"
        );
        dataSource.addStatementForClose(readTagsByProductsSortedByName);

        readAllTagNames = dataSource.getConnection().prepareStatement(
                "SELECT tagName FROM ProductTags GROUP BY tagName;"
        );
        dataSource.addStatementForClose(readAllTagNames);

        readTagsByName = dataSource.getConnection().prepareStatement(
                "SELECT * FROM ProductTags WHERE ProductTags.tagName = ?;"
        );
        dataSource.addStatementForClose(readTagsByName);

        readTagByNameAndValue = dataSource.getConnection().prepareStatement(
                "SELECT * FROM ProductTags WHERE tagName=? AND tagValue=?;"
        );
        dataSource.addStatementForClose(readTagByNameAndValue);

        addTag = dataSource.getConnection().prepareStatement(
                "INSERT INTO ProductTags(tagName, tagValue) VALUES (?, ?);",
                PreparedStatement.RETURN_GENERATED_KEYS
        );
        dataSource.addStatementForClose(addTag);

        updateTag = dataSource.getConnection().prepareStatement(
                "UPDATE ProductTags SET tagName=?, tagValue=? WHERE id=?;"
        );
        dataSource.addStatementForClose(updateTag);

        removeAllNotLinkedTags = dataSource.getConnection().prepareStatement(
                "DELETE FROM ProductTags WHERE ProductTags.id NOT IN " +
                        "((SELECT tagId FROM ProductPricesToTags GROUP BY tagId)" +
                        "UNION " +
                        "(SELECT tagId FROM ProductCaloriesToTags GROUP BY tagId));"
        );
        dataSource.addStatementForClose(removeAllNotLinkedTags);
    }

    @Override
    public HashMap<Integer, ProductTag> getTagsByProductId(int productId) throws Exception {
        HashMap<Integer, ProductTag> tags = new HashMap<>();

        readTagsByProductId.setInt(1, productId);
        readTagsByProductId.setInt(2, productId);
        try(ResultSet tagsReader = readTagsByProductId.executeQuery()) {
            while(tagsReader.next()) {
                ProductTag tag = new ProductTag(
                        tagsReader.getInt("id"),
                        tagsReader.getString("tagName"),
                        tagsReader.getString("tagValue")
                );
                tags.put(tag.getId(), tag);
            }
        }

        return tags;
    }

    @Override
    public HashMap<Integer, ProductTag> getTagsByProductName(String productName) throws Exception {
        HashMap<Integer, ProductTag> tags = new HashMap<>();

        readTagsByProductName.setString(1, productName);
        readTagsByProductName.setString(2, productName);
        try(ResultSet tagsReader = readTagsByProductName.executeQuery()) {
            while(tagsReader.next()) {
                ProductTag tag = new ProductTag(
                        tagsReader.getInt("id"),
                        tagsReader.getString("tagName"),
                        tagsReader.getString("tagValue")
                );
                tags.put(tag.getId(), tag);
            }
        }

        return tags;
    }

    @Override
    public HashMap<Integer, ProductTag> getTagsForProductsSortedByName(int fromIndex, int count) throws Exception {
        HashMap<Integer, ProductTag> tags = new HashMap<>();

        readTagsByProductsSortedByName.setInt(1, count);
        readTagsByProductsSortedByName.setInt(2, fromIndex);
        readTagsByProductsSortedByName.setInt(3, count);
        readTagsByProductsSortedByName.setInt(4, fromIndex);
        try(ResultSet tagsReader = readTagsByProductsSortedByName.executeQuery()) {
            while(tagsReader.next()) {
                ProductTag tag = new ProductTag(
                        tagsReader.getInt("id"),
                        tagsReader.getString("tagName"),
                        tagsReader.getString("tagValue")
                );
                tags.put(tag.getId(), tag);
            }
        }

        return tags;
    }

    @Override
    public List<String> getAllTagsName() throws Exception {
        ArrayList<String> allTagsName = new ArrayList<>();

        ResultSet allTagsNameReader = readAllTagNames.executeQuery();
        while(allTagsNameReader.next()) allTagsName.add(allTagsNameReader.getString("tagName"));
        allTagsNameReader.close();

        return allTagsName;
    }

    @Override
    public List<ProductTag> getTagsByName(String name) throws Exception {
        ArrayList<ProductTag> tagsByName = new ArrayList<>();

        readTagsByName.setString(1, name);
        ResultSet tagsByNameReader = readTagsByName.executeQuery();
        while(tagsByNameReader.next()) {
            tagsByName.add(
                    new ProductTag(
                            tagsByNameReader.getInt("id"),
                            tagsByNameReader.getString("tagName"),
                            tagsByNameReader.getString("tagValue")
                    )
            );
        }

        return tagsByName;
    }

    @Override
    public ProductTag getTagByNameAndValue(String name, String value) throws Exception {
        readTagByNameAndValue.setString(1, name);
        readTagByNameAndValue.setString(2, value);
        ProductTag tag = null;

        try(ResultSet tagReader = readTagByNameAndValue.executeQuery()) {
            if(tagReader.next()) {
                tag = new ProductTag(
                        tagReader.getInt("id"),
                        tagReader.getString("tagName"),
                        tagReader.getString("tagValue")
                );
            }
        }

        return tag;
    }

    @Override
    public ProductTag add(ProductTag tag) throws Exception {
        try {
            addTag.setString(1, tag.getName());
            addTag.setString(2, tag.getValue());
            addTag.executeUpdate();

            ResultSet result = addTag.getGeneratedKeys();
            result.next();
            int generatedId = result.getInt("id");
            result.close();

            return tag.setId(generatedId);
        } catch(SQLException e) {
            if(e.getErrorCode() == PostgresErrorsCode.UNIQUE_VIOLATION.CODE) {
                throw new Exception("Тег " + tag + " уже присутствует в БД.", e);
            }
            throw e;
        }
    }

    @Override
    public ProductTag update(ProductTag tag) throws Exception {
        try {
            updateTag.setString(1, tag.getName());
            updateTag.setString(2, tag.getValue());
            updateTag.setInt(3, tag.getId());
            updateTag.executeUpdate();
            return tag;
        } catch(SQLException e) {
            if(e.getErrorCode() == PostgresErrorsCode.UNIQUE_VIOLATION.CODE) {
                throw new Exception("Тег " + tag + " уже присутствует в БД.", e);
            }
            throw e;
        }
    }

    @Override
    public void removeAllNotLinkedTags() throws Exception {
        removeAllNotLinkedTags.executeUpdate();
    }

}
