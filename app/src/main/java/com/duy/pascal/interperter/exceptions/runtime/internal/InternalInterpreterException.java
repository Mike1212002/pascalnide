package com.duy.pascal.interperter.exceptions.runtime.internal;

import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.interperter.exceptions.runtime.RuntimePascalException;

public class InternalInterpreterException extends RuntimePascalException {
    public InternalInterpreterException(LineNumber line) {
        super(line);
    }

    public String getInternalError() {
        return "Unspecified";
    }

    @Override
    public String getMessage() {
        return "Internal Interpreter Error: " + getInternalError();
    }
}
