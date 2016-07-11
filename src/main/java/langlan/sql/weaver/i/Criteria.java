package langlan.sql.weaver.i;

/**
 * A Single Criteria.<br/>
 * The instance including two types of information : sql-fragment and bind-variables. using {@link #toString()} and
 * {@link #vars()} to get them.
 */
public interface Criteria extends VariablesBound {
	/** SQL-fragment that represents this criteria */
	String toString();
}
