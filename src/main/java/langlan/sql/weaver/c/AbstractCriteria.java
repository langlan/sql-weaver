package langlan.sql.weaver.c;

import langlan.sql.weaver.i.Criteria;
import langlan.sql.weaver.u.Variables;

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