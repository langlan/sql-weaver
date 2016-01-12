package langlan.sql.weaver.i;

/**
 * A Signle Criteria.<br/>
 * The instance including two parts of infomations : weaver-fragment and bind-variables. using {@link #toString()} and
 * {@link #vars()} to get them.
 */
public interface Criteria extends VariablesBound {
	/** SQL-fragment that represents this criteria */
	String toString();
}
