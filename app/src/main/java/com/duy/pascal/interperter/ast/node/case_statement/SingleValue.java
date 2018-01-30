package com.duy.pascal.interperter.ast.node.case_statement;

import com.duy.pascal.interperter.ast.codeunit.RuntimeExecutableCodeUnit;
import com.duy.pascal.interperter.ast.variablecontext.VariableContext;
import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.interperter.exceptions.runtime.RuntimePascalException;

class SingleValue implements CaseCondition {
    private Object mValue;
    private LineNumber line;

    SingleValue(Object value, LineNumber line) {
        this.mValue = value;
        this.line = line;
    }

    @Override
    public boolean fits(VariableContext f, RuntimeExecutableCodeUnit<?> main, Object value) throws RuntimePascalException {
        if (value.equals(mValue)) return true;

        if (value instanceof Number && mValue instanceof Number) {
            if (mValue instanceof Double || value instanceof Double //real mValue
                    || mValue instanceof Float || value instanceof Float) {
                double v1 = ((Number) mValue).doubleValue();
                double v2 = ((Number) mValue).doubleValue();
                return v1 == v2;
            } else { //integer mValue
                long v1 = ((Number) value).longValue();
                long v2 = ((Number) mValue).longValue();
                return v1 == v2;
            }
        }
        //other type, include string, object
        return value.equals(mValue);
    }

    @Override
    public LineNumber getLineNumber() {
        return line;
    }
}
