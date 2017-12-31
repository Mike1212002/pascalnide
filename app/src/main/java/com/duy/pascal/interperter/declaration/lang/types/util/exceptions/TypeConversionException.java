package com.duy.pascal.interperter.declaration.lang.types.util.exceptions;

public class TypeConversionException extends BaseCommonException {
    private static final long serialVersionUID = 2021L;

    public TypeConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeConversionException(String message) {
        super(message);
    }

}
