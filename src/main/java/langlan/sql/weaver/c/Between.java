package langlan.sql.weaver.c;

public class Between extends AbstractSingleValueTestingCriteria {
	public Between(String testing, Object left, Object right) {
		super(testing);
		this.vars = new Object[] { left, right };
	}
	
	protected Between(String testing, Object left, Object right, boolean calcImmediate) {
		super(testing, calcImmediate);
		this.vars = new Object[] { left, right };
	}

	public Object getLeftBoundValue() {
		return vars[0];
	}

	public Object getRightBoundValue() {
		return vars[1];
	}

	@Override
	protected void calcExpression() {
		this.expr = getTesting() + (isNegative() ? " Not Between ? And ?" : " Between ? And ?");
	}
}