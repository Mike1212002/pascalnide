package com.js.interpreter.ast.returnsvalue;

import com.duy.pascal.backend.debugable.DebuggableExecutableReturnsValue;
import com.duy.pascal.backend.exceptions.AmbiguousFunctionCallException;
import com.duy.pascal.backend.exceptions.BadFunctionCallException;
import com.duy.pascal.backend.exceptions.ParsingException;
import com.duy.pascal.backend.exceptions.UnassignableTypeException;
import com.duy.pascal.backend.tokens.WordToken;
import com.js.interpreter.ast.AbstractFunction;
import com.js.interpreter.ast.expressioncontext.CompileTimeContext;
import com.js.interpreter.ast.expressioncontext.ExpressionContext;
import com.js.interpreter.ast.instructions.ExecutionResult;
import com.js.interpreter.ast.instructions.SetValueExecutable;
import com.js.interpreter.runtime.VariableContext;
import com.js.interpreter.runtime.codeunit.RuntimeExecutable;
import com.js.interpreter.runtime.exception.RuntimePascalException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class FunctionCall extends DebuggableExecutableReturnsValue {

    ReturnsValue[] arguments;

    public static ReturnsValue generateFunctionCall(WordToken name,
                                                    List<ReturnsValue> arguments, ExpressionContext f)
            throws ParsingException {
        List<List<AbstractFunction>> possibilities = new ArrayList<List<AbstractFunction>>();
        f.getCallableFunctions(name.name.toLowerCase(), possibilities);

        boolean matching = false;

        AbstractFunction chosen = null;
        boolean perfectfit = false;
        AbstractFunction ambigous = null;
        ReturnsValue result = null;
        for (List<AbstractFunction> l : possibilities) {
            for (AbstractFunction a : l) {
                result = a.generatePerfectFitCall(name.lineInfo, arguments, f);
                if (result != null) {
                    if (perfectfit == true) {
                        throw new AmbiguousFunctionCallException(name.lineInfo,
                                chosen, a);
                    }
                    perfectfit = true;
                    chosen = a;
                    continue;
                }
                result = a.generateCall(name.lineInfo, arguments, f);
                if (result != null && !perfectfit) {
                    if (chosen != null) {
                        ambigous = chosen;
                    }
                    chosen = a;
                }
                if (a.argumentTypes().length == arguments.size()) {
                    matching = true;
                }
            }
        }
        if (result == null) {
            throw new BadFunctionCallException(name.lineInfo, name.name,
                    !possibilities.isEmpty(), matching);
        } else if (!perfectfit && ambigous != null) {
            throw new AmbiguousFunctionCallException(name.lineInfo, chosen,
                    ambigous);
        } else {
            return result;
        }
    }

    @Override
    public String toString() {
        return getFunctionName() + "(" + Arrays.toString(arguments) + ')';
    }

    protected abstract String getFunctionName();

    @Override
    public ExecutionResult executeImpl(VariableContext f,
                                       RuntimeExecutable<?> main) throws RuntimePascalException {
        getValueImpl(f, main);
        return ExecutionResult.NONE;
    }

    @Override
    public SetValueExecutable createSetValueInstruction(ReturnsValue r)
            throws UnassignableTypeException {
        throw new UnassignableTypeException(r);
    }

    @Override
    public Object compileTimeValue(CompileTimeContext context)
            throws ParsingException {
        return null;
    }

    ReturnsValue[] compileTimeExpressionFoldArguments(CompileTimeContext context)
            throws ParsingException {
        ReturnsValue[] args = new ReturnsValue[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            args[i] = arguments[i].compileTimeExpressionFold(context);
        }
        return args;
    }
}