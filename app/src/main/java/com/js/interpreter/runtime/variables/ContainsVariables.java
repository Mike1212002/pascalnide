package com.js.interpreter.runtime.variables;

import com.js.interpreter.runtime.exception.RuntimePascalException;

public interface ContainsVariables extends Cloneable {
	Object getVariable(String name) throws RuntimePascalException;

	void setVariable(String name, Object val);

	ContainsVariables clone();
}
