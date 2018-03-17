package com.duy.pascal.interperter.ast.runtime.value;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.pascal.ui.debug.DebugManager;
import com.duy.pascal.interperter.ast.codeunit.RuntimeExecutableCodeUnit;
import com.duy.pascal.interperter.ast.expressioncontext.CompileTimeContext;
import com.duy.pascal.interperter.ast.expressioncontext.ExpressionContext;
import com.duy.pascal.interperter.ast.node.Node;
import com.duy.pascal.interperter.ast.variablecontext.VariableContext;
import com.duy.pascal.interperter.declaration.Name;
import com.duy.pascal.interperter.declaration.lang.function.AbstractCallableFunction;
import com.duy.pascal.interperter.declaration.lang.function.MethodDeclaration;
import com.duy.pascal.interperter.declaration.lang.types.ArgumentType;
import com.duy.pascal.interperter.declaration.lang.types.RuntimeType;
import com.duy.pascal.interperter.exceptions.runtime.MethodCallException;
import com.duy.pascal.interperter.exceptions.runtime.RuntimePascalException;
import com.duy.pascal.interperter.exceptions.runtime.internal.MethodReflectionException;
import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.interperter.function.builtin.IMethodDeclaration;
import com.duy.pascal.interperter.utils.ArrayUtil;

import java.lang.reflect.InvocationTargetException;

public class SimpleFunctionCall extends FunctionCall {
    private AbstractCallableFunction function;

    private LineNumber line;

    public SimpleFunctionCall(AbstractCallableFunction function,
                              RuntimeValue[] arguments, LineNumber line) {
        this.function = function;
        if (function == null) {
            System.err.println("Warning: Null function call");
        }
        this.arguments = arguments;
        this.line = line;
    }

    @Override
    public Object getValueImpl(@NonNull VariableContext f, @NonNull RuntimeExecutableCodeUnit<?> main)
            throws RuntimePascalException {
        if (main.isDebug()) {
            main.getDebugListener().onLine((Node) this, line);
        }
        main.incStack(line);
        //Do not enable debug in any case, because you will need to get value of list parameter,
        //In the case of empty parameters, pause once
        main.scriptControlCheck(line, false);

        //array store value of parameters
        Object[] values = new Object[arguments.length];
        //list type of parameters
        ArgumentType[] argumentTypes = function.argumentTypes();

        for (int i = 0; i < values.length; i++) {
            values[i] = arguments[i].getValue(f, main);
        }

        if (main.isDebug()) {
            if (arguments.length > 0) {
                DebugManager.showMessage(arguments[0].getLineNumber(),
                        ArrayUtil.paramsToString(arguments, values), main);
            }
            main.scriptControlCheck(line);

        }
        Object result;
        try {
            result = function.visit(f, main, values);

            DebugManager.onFunctionCalled(function, arguments, result, main);//debug
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new MethodReflectionException(line, e);
        } catch (InvocationTargetException e) {
            throw new MethodCallException(line, e.getTargetException(), function);
        }

        main.decStack();
        if (result == null) {
            result = NullValue.get();
        }
        return result;
    }

    @Nullable
    @Override
    public RuntimeType getRuntimeType(ExpressionContext context) {
        return new RuntimeType(function.returnType(), false);
    }

    @NonNull
    @Override
    public LineNumber getLineNumber() {
        return line;
    }

    @Override
    public void setLineNumber(LineNumber lineNumber) {

    }

    @Override
    protected Name getFunctionName() {
        return function.getName();
    }

    @Override
    public RuntimeValue compileTimeExpressionFold(CompileTimeContext context)
            throws Exception {
        return new SimpleFunctionCall(function, compileTimeExpressionFoldArguments(context), line);
    }

    @Override
    public Object compileTimeValue(CompileTimeContext context) throws Exception {
        Object[] args = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            args[i] = arguments[i].compileTimeValue(context);
            if (args[i] == null) return null;
        }
        if (function instanceof MethodDeclaration || function instanceof IMethodDeclaration) {
            try {
                return function.visit(null, null, args);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Node compileTimeConstantTransform(CompileTimeContext c)
            throws Exception {
        return new SimpleFunctionCall(function, compileTimeExpressionFoldArguments(c), line);
    }
}
