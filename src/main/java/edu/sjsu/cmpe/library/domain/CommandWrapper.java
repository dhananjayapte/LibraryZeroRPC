package edu.sjsu.cmpe.library.domain;

import org.msgpack.annotation.Message;
import org.msgpack.type.Value;

@Message
public class CommandWrapper {
	private String methodName;
	private Value[] args;
	
	/**
	 * default constructor
	 */
	public CommandWrapper() {}

	/**
	 * @param methodName
	 * @param args
	 */
	public CommandWrapper(String methodName, Value[] args) {
		this.methodName = methodName;
		this.args = args;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @param methodName 
	 * 				the methodName to set
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * @return the args
	 */
	public Value[] getArgs() {
		return args;
	}

	/**
	 * @param args the args to set
	 */
	public void setArgs(Value[] args) {
		this.args = args;
	}
}
