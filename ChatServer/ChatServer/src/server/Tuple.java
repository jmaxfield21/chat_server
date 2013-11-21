package server;

/**
Tuple.java
@author Jace Maxfield && Sean Groathouse

A simple data structure for immutable storage of two linked objects.
For the purposes of this project, two tuples are considered equal 
if they contain the same first element x (in this context that is 
generally the username string). Note also that the username String
is case-sensitive in the protocol, so the checking will be appropriate.
*/

@SuppressWarnings("unchecked")
public class Tuple<X, Y> {
	public final X x;
	public final Y y;

	public Tuple(X x, Y y) {
		this.x = x;
		this.y = y;
	}

	/** 
	Two tuples are for this project considered equal if they have
	the same x value.
	*/
	public boolean equals(Object other) {
		if (other == null) 
			return false;
		if (other == this) 
    		return true;
		if (!(other.getClass() == this.getClass()))
			return false;
		Tuple<X, Y> otherTuple = (Tuple<X, Y>)other;
		return (this.x.equals(otherTuple.x));
	}
}