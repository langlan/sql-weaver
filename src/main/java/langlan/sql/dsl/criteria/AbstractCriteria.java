package langlan.sql.dsl.criteria;

import langlan.sql.dsl.i.Criteria;
import langlan.sql.dsl.u.Variables;

public abstract class AbstractCriteria implements Criteria {
	protected String expr;
	protected Object[] vars = Variables.EMPTY_ARRAY;

	@Override
	public String toString() {
		return expr;
	}

	@Override
	public Object[] vars() {
		return this.vars;
	}
}