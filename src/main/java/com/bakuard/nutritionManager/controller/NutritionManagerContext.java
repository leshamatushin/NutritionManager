package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.model.NutritionManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.util.logging.Logger;

public class NutritionManagerContext implements ServletContextListener {

    private NutritionManager nutritionManager;
    private Logger workLogger;
    private Logger exceptionLogger;
    private Logger debugLogger;

    public NutritionManagerContext() {}

    @Override
    public void contextInitialized(ServletContextEvent event) {
        workLogger = Logger.getLogger("com.bakuard.nutritionManager.WorkLogger");
        exceptionLogger = Logger.getLogger("com.bakuard.nutritionManager.ExceptionLogger");
        debugLogger = Logger.getLogger("com.bakuard.nutritionManager.DebugLogger");
        nutritionManager = new NutritionManager(debugLogger);

        try {
            nutritionManager.connect();
        } catch(Exception e) {
            event.getServletContext().log("Connect to DataBase", e);
        }

        event.getServletContext().setAttribute("NutritionManager", nutritionManager);
        event.getServletContext().setAttribute("WorkLogger", workLogger);
        event.getServletContext().setAttribute("ExceptionLogger", exceptionLogger);
        event.getServletContext().setAttribute("DebugLogger", debugLogger);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        try {
            nutritionManager.disconnect();
        } catch(Exception e) {
            event.getServletContext().log("Disconnect to DataBase", e);
        }
    }

}
