package com.bakuard.nutritionManager.model.dal;

import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.ProductPrice;
import com.bakuard.nutritionManager.model.ProductTag;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ProductPriceDaoImpl implements ProductPriceDao {

    private PreparedStatement readPricesByProductId;
    private PreparedStatement readPricesByProductName;
    private PreparedStatement readPricesByProductsSortedByName;
    private PreparedStatement addPrice;
    private PreparedStatement updatePrice;
    private PreparedStatement removeAllNotLinkedPrices;
    private PreparedStatement addTag;
    private PreparedStatement removeTag;

    public ProductPriceDaoImpl(DataSource dataSource) throws Exception {
        readPricesByProductId = dataSource.getConnection().prepareStatement(
                "SELECT ProductPrices.*, ProductPricesToTags.tagId " +
                        "FROM ProductPrices " +
                        "JOIN ProductsToPrices " +
                        "ON ProductPrices.id = ProductsToPrices.priceId AND ProductsToPrices.productId = ? " +
                        "LEFT JOIN ProductPricesToTags " +
                        "ON ProductPricesToTags.priceId = ProductsToPrices.priceId " +
                        "ORDER BY ProductPrices.id;"
        );
        dataSource.addStatementForClose(readPricesByProductId);

        readPricesByProductName = dataSource.getConnection().prepareStatement(
                "SELECT ProductPrices.*, ProductPricesToTags.tagId " +
                        "FROM ProductPrices " +
                        "JOIN ProductsToPrices " +
                        "ON ProductsToPrices.priceId = ProductPrices.id " +
                        "JOIN Products " +
                        "ON ProductsToPrices.productId = Products.id AND Products.name = ? " +
                        "LEFT JOIN ProductPricesToTags " +
                        "ON ProductsToPrices.priceId = ProductPricesToTags.priceId " +
                        "ORDER BY ProductPrices.id;"
        );
        dataSource.addStatementForClose(readPricesByProductName);

        readPricesByProductsSortedByName = dataSource.getConnection().prepareStatement(
                "SELECT ProductPrices.*, ProductsToPrices.productId, ProductPricesToTags.tagId " +
                        "FROM ProductsToPrices " +
                        "JOIN (SELECT Products.id FROM Products ORDER BY Products.name LIMIT ? OFFSET ?) AS Pr " +
                        "ON ProductsToPrices.productId = Pr.id " +
                        "JOIN ProductPrices " +
                        "ON ProductsToPrices.priceId = ProductPrices.id " +
                        "LEFT JOIN ProductPricesToTags " +
                        "ON ProductPricesToTags.priceId = ProductsToPrices.priceId;"
        );
        dataSource.addStatementForClose(readPricesByProductsSortedByName);

        addPrice = dataSource.getConnection().prepareStatement(
                "INSERT INTO ProductPrices(price) VALUES (?);",
                PreparedStatement.RETURN_GENERATED_KEYS
        );
        dataSource.addStatementForClose(addPrice);

        updatePrice = dataSource.getConnection().prepareStatement(
                "UPDATE ProductPrices SET price=? WHERE id=?;"
        );
        dataSource.addStatementForClose(updatePrice);

        removeAllNotLinkedPrices = dataSource.getConnection().prepareStatement(
                "DELETE FROM ProductPrices WHERE ProductPrices.id NOT IN " +
                        "(SELECT priceId FROM ProductsToPrices GROUP BY priceId);"
        );
        dataSource.addStatementForClose(removeAllNotLinkedPrices);

        addTag = dataSource.getConnection().prepareStatement(
                "INSERT INTO ProductPricesToTags(priceId, tagId) VALUES (?, ?);"
        );
        dataSource.addStatementForClose(addTag);

        removeTag = dataSource.getConnection().prepareStatement(
                "DELETE FROM ProductPricesToTags WHERE priceId=? AND tagId=?;"
        );
        dataSource.addStatementForClose(removeTag);
    }

    @Override
    public HashMap<Integer, ProductPrice> getPricesByProductId(
            int productId,
            HashMap<Integer, ProductTag> tags) throws Exception {
        HashMap<Integer, ProductPrice> result = new HashMap<>();

        readPricesByProductId.setInt(1, productId);
        try(ResultSet pricesReader = readPricesByProductId.executeQuery()) {
            ProductPrice.Builder builder = null;
            while(pricesReader.next()) {
                int priceId = pricesReader.getInt("id");
                if(builder == null) {
                    builder = new ProductPrice.Builder();
                    builder.setId(priceId);
                    builder.setPrice(pricesReader.getBigDecimal("price"));
                } else if(priceId != builder.getId()) {
                    result.put(builder.getId(), builder.build());
                    builder = new ProductPrice.Builder();
                    builder.setId(priceId);
                    builder.setPrice(pricesReader.getBigDecimal("price"));
                }
                ProductTag tag = tags.get(pricesReader.getInt("tagId"));
                if(tag != null) builder.getTags().put(tag.getId(), tag);
            }
            if(builder != null) result.put(builder.getId(), builder.build());
        }

        return result;
    }

    @Override
    public HashMap<Integer, ProductPrice> getPricesByProductName(
            String productName,
            HashMap<Integer, ProductTag> tags) throws Exception {
        HashMap<Integer, ProductPrice> result = new HashMap<>();

        readPricesByProductName.setString(1, productName);
        try(ResultSet pricesReader = readPricesByProductName.executeQuery()) {
            ProductPrice.Builder builder = null;
            while(pricesReader.next()) {
                int priceId = pricesReader.getInt("id");
                if(builder == null) {
                    builder = new ProductPrice.Builder();
                    builder.setId(priceId);
                    builder.setPrice(pricesReader.getBigDecimal("price"));
                } else if(priceId != builder.getId()) {
                    result.put(builder.getId(), builder.build());
                    builder = new ProductPrice.Builder();
                    builder.setId(priceId);
                    builder.setPrice(pricesReader.getBigDecimal("price"));
                }
                ProductTag tag = tags.get(pricesReader.getInt("tagId"));
                if(tag != null) builder.getTags().put(tag.getId(), tag);
            }
            if(builder != null) result.put(builder.getId(), builder.build());
        }

        return result;
    }

    @Override
    public void fillProductsSortedByNameWithPrice(
                                    int fromIndex, int count, HashMap<Integer, ProductTag> tags,
                                    LinkedHashMap<Integer, Product.Builder> products) throws Exception {
        readPricesByProductsSortedByName.setInt(1, count);
        readPricesByProductsSortedByName.setInt(2, fromIndex);
        try(ResultSet pricesReader = readPricesByProductsSortedByName.executeQuery()) {
            ProductPrice.Builder priceBuilder = null;
            Product.Builder productBuilder = null;
            while(pricesReader.next()) {
                int priceId = pricesReader.getInt("id");
                if(priceBuilder == null || priceBuilder.getId() != priceId) {
                    if(priceBuilder != null) productBuilder.getPrices().put(priceBuilder.getId(), priceBuilder.build());
                    productBuilder = products.get(pricesReader.getInt("productId"));
                    priceBuilder = new ProductPrice.Builder().
                            setId(priceId).
                            setPrice(pricesReader.getBigDecimal("price"));
                }
                ProductTag tag = tags.get(pricesReader.getInt("tagId"));
                if(tag != null) priceBuilder.getTags().put(tag.getId(), tag);
            }
            if(priceBuilder != null) productBuilder.getPrices().put(priceBuilder.getId(), priceBuilder.build());
        }
    }

    @Override
    public ProductPrice add(ProductPrice price) throws Exception {
        addPrice.setBigDecimal(1, price.getPrice());
        addPrice.executeUpdate();

        ResultSet generatedIdReader = addPrice.getGeneratedKeys();
        generatedIdReader.next();
        int generatedId = generatedIdReader.getInt("id");
        generatedIdReader.close();

        return price.setId(generatedId);
    }

    @Override
    public ProductPrice update(ProductPrice price) throws Exception {
        updatePrice.setBigDecimal(1, price.getPrice());
        updatePrice.setInt(2, price.getId());
        updatePrice.executeUpdate();
        return price;
    }

    @Override
    public void removeAllNotLinkedPrices() throws Exception {
        removeAllNotLinkedPrices.executeUpdate();
    }

    @Override
    public void addTag(ProductPrice price, ProductTag tag) throws Exception {
        addTag.setInt(1, price.getId());
        addTag.setInt(2, tag.getId());
        addTag.executeUpdate();
    }

    @Override
    public void removeTag(ProductPrice price, ProductTag tag) throws Exception {
        removeTag.setInt(1, price.getId());
        removeTag.setInt(2, tag.getId());
        removeTag.executeUpdate();
    }

}
