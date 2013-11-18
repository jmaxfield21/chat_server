package server;

/**
Tuple.java
@author Jace Maxfield && Sean Groathouse

A simple data structure for immutable storage of two linked objectsj
*/

public class Tuple<X, Y> {
	public final X x;
	public final Y y;

	public Tuple(X x, Y y) {
		this.x = x;
		this.y = y;
	}
}