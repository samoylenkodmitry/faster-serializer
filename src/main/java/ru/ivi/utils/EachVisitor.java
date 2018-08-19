package ru.ivi.utils;

public interface EachVisitor<T> {
	
	void visit(T t, int pos);
}
