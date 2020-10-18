package com.bakuard.nutritionManager.model.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class DataSource {

    private Connection connection;
    private ArrayList<PreparedStatement> statementsForClose;

    public DataSource() throws Exception {
        statementsForClose = new ArrayList<>();

        try {
            DriverManager.registerDriver(new org.postgresql.Driver());
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost/NutritionManagerDB",
                    "postgres",
                    "PostgreSQL.store((User user) -> user.isValid(123#));"
            );
            connection.setAutoCommit(false);
        } catch(SQLException e) {
            try {
                if(connection != null) connection.close();
            } catch(SQLException e1) {
                e1.initCause(e);
                throw new SQLException(
                        "Не удалось настроить соединение с БД. При его закрытии произошла ошибка.", e1);
            }

            throw new SQLException("Не удалось установить соединение с БД", e);
        }
    }

    public void close() throws Exception {
        for(PreparedStatement ps : statementsForClose) ps.close();
        connection.close();
    }

    public void commit() throws Exception {
        connection.commit();
    }

    public void rollback() throws Exception {
        connection.rollback();
    }

    Connection getConnection() {
        return connection;
    }

    void addStatementForClose(PreparedStatement ps) {
        statementsForClose.add(ps);
    }

}
