/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.pascal.interperter.ast.node.with_statement;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.pascal.interperter.ast.codeunit.RuntimeExecutableCodeUnit;
import com.duy.pascal.interperter.ast.expressioncontext.CompileTimeContext;
import com.duy.pascal.interperter.ast.expressioncontext.ExpressionContext;
import com.duy.pascal.interperter.ast.node.Node;
import com.duy.pascal.interperter.ast.node.ExecutionResult;
import com.duy.pascal.interperter.ast.variablecontext.VariableContext;
import com.duy.pascal.interperter.ast.runtime.value.RuntimeValue;
import com.duy.pascal.interperter.ast.runtime.value.access.FieldAccess;
import com.duy.pascal.interperter.debugable.DebuggableNodeReturnValue;
import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.interperter.exceptions.runtime.RuntimePascalException;
import com.duy.pascal.interperter.declaration.lang.types.RuntimeType;

import java.util.ArrayList;

public class WithNode extends DebuggableNodeReturnValue {

    public ArrayList<FieldAccess> arguments;
    private WithStatement withStatement;
    private LineNumber line;

    public WithNode(WithStatement withStatement, ArrayList<FieldAccess> arguments, LineNumber line) {
        this.withStatement = withStatement;
        this.line = line;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "with";
    }

    @Override
    public ExecutionResult visitImpl(VariableContext context,
                                     RuntimeExecutableCodeUnit<?> main) throws RuntimePascalException {
        Object valueImpl = getValueImpl(context, main);
        if (valueImpl == ExecutionResult.EXIT) {
            return ExecutionResult.EXIT;
        }
        return ExecutionResult.NOPE;
    }

    @Override
    public Object compileTimeValue(CompileTimeContext context)
            throws Exception {
        return null;
    }

    @Override
    public Object getValueImpl(@NonNull VariableContext f, @NonNull RuntimeExecutableCodeUnit<?> main)
            throws RuntimePascalException {
        if (main.isDebug()) {
            main.getDebugListener().onLine((Node) this, getLineNumber());
        }
        main.incStack(getLineNumber());
        main.scriptControlCheck(getLineNumber());

        new WithOnStack(f, main, withStatement).execute();

        main.decStack();
        return ExecutionResult.NOPE;
    }

    @Nullable
    @Override
    public RuntimeType getRuntimeType(ExpressionContext context) {
        return null;
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
    public RuntimeValue compileTimeExpressionFold(CompileTimeContext context)
            throws Exception {
        return new WithNode(withStatement, arguments, line);
    }

    @Override
    public Node compileTimeConstantTransform(CompileTimeContext c)
            throws Exception {
        return new WithNode(withStatement, arguments, line);
    }
}
