package langlan.sql.dsl.i;

/**
 * This Interface indicates that instance will hold bound variables on self. Using {@link #vars()} to get bound
 * variables.
 */
public interface VariablesBound {
	/**
	 * Return bound variables.
	 * 
	 * @return An array of object. Never null.
	 */
	Object[] vars();
}
