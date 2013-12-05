package com.l2fprod.common.sheet;

public interface Converter<T> {

	T toObject(String string);

	String toString(T object);
}
