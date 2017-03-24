package com.duy.pascal.backend.debugable;

import com.js.interpreter.ast.FunctionDeclaration;
import com.js.interpreter.ast.VariableDeclaration;

/**
 * Created by Duy on 24-Mar-17.
 */

public interface DebugListener {
    void onGlobalVariableChangeValue(VariableDeclaration variableDeclaration);

    void onLocalVariableChangeValue(VariableDeclaration variableDeclaration);

    void onFunctionCall(FunctionDeclaration functionDeclaration);

    void onProcedureCall(FunctionDeclaration functionDeclaration);

    void onNewMessage(String msg);

    void onClearDebug();

    void onVariableChangeValue(String name, Object value);
}