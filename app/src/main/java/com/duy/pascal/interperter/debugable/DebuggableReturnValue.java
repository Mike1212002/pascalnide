package com.duy.pascal.interperter.debugable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.pascal.interperter.ast.codeunit.RuntimeExecutableCodeUnit;
import com.duy.pascal.interperter.ast.expressioncontext.ExpressionContext;
import com.duy.pascal.interperter.ast.runtime.value.AssignableValue;
import com.duy.pascal.interperter.ast.runtime.value.RuntimeValue;
import com.duy.pascal.interperter.ast.variablecontext.VariableContext;
import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.interperter.exceptions.runtime.RuntimePascalException;
import com.duy.pascal.interperter.exceptions.runtime.UnhandledPascalException;
import com.duy.pascal.interperter.utils.NullSafety;

public abstract class DebuggableReturnValue implements RuntimeValue {
    private LineNumber lineNumber;
    public abstract boolean canDebug();

    @Nullable
    @Override
    public Object getValue(VariableContext context, RuntimeExecutableCodeUnit<?> main)
            throws RuntimePascalException {
        try {
            return NullSafety.zReturn(getValueImpl(context, main));
        } catch (RuntimePascalException e) {
            throw e;
        } catch (Exception e) {
            throw new UnhandledPascalException(this.getLineNumber(), e);
        } catch (Throwable e) {
            throw new UnhandledPascalException(getLineNumber(), e);
        }
    }

    @Override
    public AssignableValue asAssignableValue(ExpressionContext context) {
        return null;
    }

    @Override
    public void setLineNumber(LineNumber lineNumber) {
        this.lineNumber = lineNumber;
    }

    @NonNull
    @Override
    public LineNumber getLineNumber() {
        return lineNumber;
    }

    public abstract Object getValueImpl(VariableContext context, RuntimeExecutableCodeUnit<?> main)
            throws RuntimePascalException;

}
