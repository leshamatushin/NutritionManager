package com.bakuard.nutritionManager.model.dal;

public enum PostgresErrorsCode {

    UNIQUE_VIOLATION(23505);


    public final int CODE;

    private PostgresErrorsCode(int code) {
        CODE = code;
    }

}
