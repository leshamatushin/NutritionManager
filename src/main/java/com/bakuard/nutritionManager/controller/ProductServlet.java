package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.controller.convert.JsonConverter;
import com.bakuard.nutritionManager.model.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductServlet extends HttpServlet {

    private NutritionManager nutritionManager;
    private JsonConverter jsonConverter;
    private Logger workLogger;
    private Logger exceptionLogger;
    private Logger debugLogger;

    public ProductServlet() {}

    @Override
    public void init() throws ServletException {
        nutritionManager = (NutritionManager) getServletContext().getAttribute("NutritionManager");
        jsonConverter = new JsonConverter(nutritionManager.getMathContext());
        workLogger = (Logger) getServletContext().getAttribute("WorkLogger");
        exceptionLogger = (Logger) getServletContext().getAttribute("ExceptionLogger");
        debugLogger = (Logger) getServletContext().getAttribute("DebugLogger");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        workLogger.logp(Level.INFO, getClass().getName(), "doPost()", "save product");

        StringBuilder productJsonIn = new StringBuilder();
        req.getReader().lines().forEach(productJsonIn::append);

        try {
            Product newProduct = jsonConverter.toProduct(productJsonIn.toString());
            Product oldProduct = nutritionManager.getProductById(newProduct.getId());

            debugLogger.logp(Level.INFO, getClass().getName(), "doPost()",
                    "save product / input product json = " + productJsonIn.toString());

            String productJsonOut = jsonConverter.toJson(nutritionManager.save(oldProduct, newProduct));

            debugLogger.logp(Level.INFO, getClass().getName(), "doPost()",
                    "save product / output product json = " + productJsonOut);

            PrintWriter writer = resp.getWriter();
            writer.print(productJsonOut);
            writer.flush();
        } catch(Exception e) {
            exceptionLogger.logp(Level.SEVERE, getClass().getName(), "doPost()",
                    "save product", e);
            workLogger.logp(Level.SEVERE, getClass().getName(), "doPost()",
                    e.getClass().getName());
            debugLogger.logp(Level.SEVERE, getClass().getName(), "doPost()",
                    productJsonIn.toString());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean isNewTab = Boolean.parseBoolean(req.getParameter("newTab"));

        if(isNewTab) {
            workLogger.logp(Level.INFO, getClass().getName(), "doGet()", "open new tab");

            RequestDispatcher requestDispatcher = getServletContext().getRequestDispatcher("/html/Product.html");
            requestDispatcher.forward(req, resp);
        } else {
            String dataType = req.getParameter("dataType");
            workLogger.logp(Level.INFO, getClass().getName(), "doGet()", "dataType=" + dataType);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            PrintWriter writer = resp.getWriter();

            if("newProductTag".equals(dataType)) {
                String result = null;
                try {
                    result = jsonConverter.toJson(new ProductTag());
                    writer.print(result);
                    writer.flush();
                } catch(Exception e) {
                    exceptionLogger.logp(Level.SEVERE, getClass().getName(), "doGet()",
                            "dataType=" + dataType, e);
                    workLogger.logp(Level.SEVERE, getClass().getName(), "doGet()",
                            e.getClass().getName());
                    debugLogger.logp(Level.SEVERE, getClass().getName(), "doGet()", result);
                }
            } else if("newProductPrice".equals(dataType)) {
                String result = null;
                try {
                    result = jsonConverter.toJson(new ProductPrice());
                    writer.print(result);
                    writer.flush();
                } catch(Exception e) {
                    exceptionLogger.logp(Level.SEVERE, getClass().getName(), "doGet()",
                            "dataType=" + dataType, e);
                    workLogger.logp(Level.SEVERE, getClass().getName(), "doGet()",
                            e.getClass().getName());
                    debugLogger.logp(Level.SEVERE, getClass().getName(), "doGet()", result);
                }
            } else if("newProductCalories".equals(dataType)) {
                String result = null;
                try {
                    result = jsonConverter.toJson(new ProductCalories());
                    writer.print(result);
                    writer.flush();
                } catch(Exception e) {
                    exceptionLogger.logp(Level.SEVERE, getClass().getName(), "doGet()",
                            "dataType=" + dataType, e);
                    workLogger.logp(Level.SEVERE, getClass().getName(), "doGet()",
                            e.getClass().getName());
                    debugLogger.logp(Level.SEVERE, getClass().getName(), "doGet()", result);
                }
            } else if("newProduct".equals(dataType)) {
                Product product = null;
                String result = null;
                try {
                    product = new Product();
                    result = jsonConverter.toJson(product);
                    writer.print(result);
                    writer.flush();
                } catch (Exception e) {
                    exceptionLogger.logp(Level.SEVERE, getClass().getName(), "doGet()",
                            "dataType=" + dataType + " | product=" + product, e);
                    workLogger.logp(Level.SEVERE, getClass().getName(), "doGet()",
                            e.getClass().getName());
                    debugLogger.logp(Level.SEVERE, getClass().getName(), "doGet()", result);
                }
            } else if("existsProduct".equals(dataType)) {
                Product product = null;
                String result = null;
                try {
                    int productId = Integer.parseInt(req.getParameter("productId"));
                    product = nutritionManager.getProductById(productId);

                    if(product != null) {
                        result = jsonConverter.toJson(product);
                        writer.print(result);
                        writer.flush();
                    } else {
                        workLogger.logp(Level.INFO, getClass().getName(), "doGet()",
                                "don't find product with id=" + productId);
                    }
                } catch (Exception e) {
                    exceptionLogger.logp(Level.SEVERE, getClass().getName(), "doGet()",
                            "dataType=" + dataType + " | product=" + product, e);
                    workLogger.logp(Level.SEVERE, getClass().getName(), "doGet()",
                            e.getClass().getName());
                    debugLogger.logp(Level.SEVERE, getClass().getName(), "doGet()", result);
                }
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    @Override
    public void destroy() {

    }

}
