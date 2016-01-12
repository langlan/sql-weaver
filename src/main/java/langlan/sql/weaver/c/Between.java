package langlan.sql.weaver.c;

public class Between extends AbstractSingleValueTestingCriteria {
	public Between(String testing, Object left, Object right) {
		this.testing = testing;
		this.expr = testing + " Between ? And ?";
		this.vars = new Object[] { left, right };
	}

	public Object getLeftBoundValue() {
		return vars[0];
	}

	public Object getRightBoundValue() {
		return vars[1];
	}

}