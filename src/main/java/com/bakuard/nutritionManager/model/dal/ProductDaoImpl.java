package com.bakuard.nutritionManager.model.dal;

import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.ProductCalories;
import com.bakuard.nutritionManager.model.ProductPrice;
import com.bakuard.nutritionManager.model.ProductTag;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ProductDaoImpl implements ProductDao {

    private PreparedStatement readProductById;
    private PreparedStatement readProductByName;
    private PreparedStatement readProductsSortedByName;
    private PreparedStatement addProduct;
    private PreparedStatement updateProduct;
    private PreparedStatement removeProductById;
    private PreparedStatement addPrice;
    private PreparedStatement addCalories;
    private PreparedStatement removePrice;
    private PreparedStatement removeCalories;

    public ProductDaoImpl(DataSource dataSource) throws Exception {
        readProductById = dataSource.getConnection().prepareStatement(
                "SELECT Products.* FROM Products WHERE Products.id=?;"
        );
        dataSource.addStatementForClose(readProductById);

        readProductByName = dataSource.getConnection().prepareStatement(
                "SELECT Products.* FROM Products WHERE Products.name=?;"
        );
        dataSource.addStatementForClose(readProductByName);

        readProductsSortedByName = dataSource.getConnection().prepareStatement(
                "SELECT Products.* FROM Products ORDER BY name LIMIT ? OFFSET ?;"
        );
        dataSource.addStatementForClose(readProductsSortedByName);

        addProduct = dataSource.getConnection().prepareStatement(
                "INSERT INTO Products(name, unit) VALUES (?, ?);",
                PreparedStatement.RETURN_GENERATED_KEYS
        );
        dataSource.addStatementForClose(addProduct);

        updateProduct = dataSource.getConnection().prepareStatement(
                "UPDATE Products SET name=?, unit=? WHERE id=?;"
        );
        dataSource.addStatementForClose(updateProduct);

        removeProductById = dataSource.getConnection().prepareStatement(
                "DELETE FROM Products WHERE id=?;"
        );
        dataSource.addStatementForClose(removeProductById);

        addPrice = dataSource.getConnection().prepareStatement(
                "INSERT INTO ProductsToPrices(productId, priceId) VALUES (?, ?);"
        );
        dataSource.addStatementForClose(addPrice);

        addCalories = dataSource.getConnection().prepareStatement(
                "INSERT INTO ProductsToCalories(productId, caloriesId) VALUES (?, ?);"
        );
        dataSource.addStatementForClose(addCalories);

        removePrice = dataSource.getConnection().prepareStatement(
                "DELETE FROM ProductsToPrices WHERE productId=? AND priceId=?;"
        );
        dataSource.addStatementForClose(removePrice);

        removeCalories = dataSource.getConnection().prepareStatement(
                "DELETE FROM ProductsToCalories WHERE productId=? AND caloriesId=?;"
        );
        dataSource.addStatementForClose(removeCalories);
    }

    @Override
    public Product getProductById(int id,
                                  HashMap<Integer, ProductPrice> prices,
                                  HashMap<Integer, ProductCalories> calories) throws Exception {
        readProductById.setInt(1, id);
        try(ResultSet productReader = readProductById.executeQuery()) {
            if(productReader.next()) {
                Product.Builder builder = new Product.Builder();
                builder.setId(productReader.getInt("id"));
                builder.setName(productReader.getString("name"));
                builder.setUnit(productReader.getString("unit"));
                builder.setPrices(prices);
                builder.setCalories(calories);
                return builder.build();
            }
        }

        return null;
    }

    @Override
    public Product getProductByName(String productName,
                                    HashMap<Integer, ProductPrice> prices,
                                    HashMap<Integer, ProductCalories> calories) throws Exception {
        readProductById.setString(1, productName);
        try(ResultSet productReader = readProductById.executeQuery()) {
            if(productReader.next()) {
                Product.Builder builder = new Product.Builder();
                builder.setId(productReader.getInt("id"));
                builder.setName(productReader.getString("name"));
                builder.setUnit(productReader.getString("unit"));
                builder.setPrices(prices);
                builder.setCalories(calories);
                return builder.build();
            }
        }

        return null;
    }

    @Override
    public LinkedHashMap<Integer, Product.Builder> getProductsSortedByName(int fromIndex, int count) throws Exception {
        LinkedHashMap<Integer, Product.Builder> products = new LinkedHashMap<>();

        readProductsSortedByName.setInt(1, count);
        readProductsSortedByName.setInt(2, fromIndex);
        try(ResultSet productsReader = readProductsSortedByName.executeQuery()) {
            while(productsReader.next()) {
                Product.Builder builder = new Product.Builder();
                builder.setId(productsReader.getInt("id"));
                builder.setName(productsReader.getString("name"));
                builder.setUnit(productsReader.getString("unit"));
                products.put(builder.getId(), builder);
            }
        }

        return products;
    }

    @Override
    public Product add(Product product) throws Exception {
        try {
            addProduct.setString(1, product.getName());
            addProduct.setString(2, product.getUnit());
            addProduct.executeUpdate();

            ResultSet result = addProduct.getGeneratedKeys();
            result.next();
            int generatedId = result.getInt("id");
            result.close();

            return product.setId(generatedId);
        } catch(SQLException e) {
            if(e.getErrorCode() == PostgresErrorsCode.UNIQUE_VIOLATION.CODE) {
                throw new Exception("Продукт с именем " + product.getName() + " уже присутсвует в БД.", e);
            }
            throw e;
        }
    }

    @Override
    public Product update(Product product) throws Exception {
        try {
            updateProduct.setString(1, product.getName());
            updateProduct.setString(2, product.getUnit());
            updateProduct.setInt(3, product.getId());
            updateProduct.executeUpdate();
            return product;
        } catch(SQLException e) {
            if(e.getErrorCode() == PostgresErrorsCode.UNIQUE_VIOLATION.CODE) {
                throw new Exception("Продукт с именем " + product.getName() + " уже присутсвует в БД.", e);
            }
            throw e;
        }
    }

    @Override
    public void removeProductById(int productId) throws Exception {
        removeProductById.setInt(1, productId);
        removeProductById.executeUpdate();
    }

    @Override
    public void addPrice(Product product, ProductPrice price) throws Exception {
        addPrice.setInt(1, product.getId());
        addPrice.setInt(2, price.getId());
        addPrice.executeUpdate();
    }

    @Override
    public void addCalories(Product product, ProductCalories calories) throws Exception {
        addCalories.setInt(1, product.getId());
        addCalories.setInt(2, calories.getId());
        addCalories.executeUpdate();
    }

    @Override
    public void removePrice(Product product, ProductPrice price) throws Exception {
        removePrice.setInt(1, product.getId());
        removePrice.setInt(2, price.getId());
        removePrice.executeUpdate();
    }

    @Override
    public void removeCalories(Product product, ProductCalories calories) throws Exception {
        removeCalories.setInt(1, product.getId());
        removeCalories.setInt(2, calories.getId());
        removeCalories.executeUpdate();
    }

}
