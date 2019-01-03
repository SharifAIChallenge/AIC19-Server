package ir.sharif.aichallenge.server.server.config;

/**
 * Copyright (C) 2016 Hadi
 */
public class StringParam extends Param<String> {
    public StringParam(String paramName, String defaultValue) {
        super(paramName, defaultValue);
    }

    @Override
    public String getValueFromString(String value) {
        return value;
    }
}
