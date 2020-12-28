package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.controller.convert.JsonConverter;
import com.bakuard.nutritionManager.model.NutritionManager;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductsServlet extends HttpServlet {

    private NutritionManager nutritionManager;
    private JsonConverter jsonConverter;
    private Logger workLogger;
    private Logger exceptionLogger;
    private Logger debugLogger;

    public ProductsServlet() {}

    @Override
    public void init() throws ServletException {
        nutritionManager = (NutritionManager) getServletContext().getAttribute("NutritionManager");
        jsonConverter = new JsonConverter(nutritionManager.getMathContext());
        workLogger = (Logger) getServletContext().getAttribute("WorkLogger");
        exceptionLogger = (Logger) getServletContext().getAttribute("ExceptionLogger");
        debugLogger = (Logger) getServletContext().getAttribute("DebugLogger");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String dataType = req.getParameter("dataType");
        workLogger.logp(Level.INFO, getClass().getName(), "doGet()", "dataType=" + dataType);

        if("productsList".equals(dataType)) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            PrintWriter writer = resp.getWriter();

            String result = null;
            try {
                int fromIndex = Integer.parseInt(req.getParameter("from"));
                int count = Integer.parseInt(req.getParameter("count"));
                result = jsonConverter.toJson(nutritionManager.getProductsSortedByName(fromIndex, count));
                writer.print(result);
                writer.flush();
            } catch (Exception e) {
                exceptionLogger.logp(Level.SEVERE, getClass().getName(), "doGet()",
                        "getProductsSortedByName", e);
                workLogger.logp(Level.SEVERE, getClass().getName(), "doGet()", e.getClass().getName());
                debugLogger.logp(Level.SEVERE, getClass().getName(), "doGet()", result);
            }
        } else {
            RequestDispatcher requestDispatcher = getServletContext().getRequestDispatcher("/html/Products.html");
            requestDispatcher.forward(req, resp);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int productId = Integer.parseInt(req.getReader().readLine());
        try {
            nutritionManager.removeProduct(productId);
        } catch(Exception e) {
            exceptionLogger.logp(Level.SEVERE, getClass().getName(), "doDelete()", "" +
                    "delete product#" + productId, e);
            workLogger.logp(Level.SEVERE, getClass().getName(), "doDelete()", e.getClass().getName());
        }

        workLogger.logp(Level.INFO, getClass().getName(), "doDelete()", "deleted product#" + productId);
    }

    @Override
    public void destroy() {

    }

}
