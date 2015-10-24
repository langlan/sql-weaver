package langlan.sql.dsl.i;

/**
 * A Signle Criteria.<br/>
 * The instance including two parts of infomations : sql-fragment and bind-variables. using {@link #toString()} and
 * {@link #vars()} to get them.
 */
public interface Criteria extends VariablesBound {
	/** SQL-fragment that represents this criteria */
	String toString();
}
