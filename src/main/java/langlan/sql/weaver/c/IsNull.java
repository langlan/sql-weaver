package langlan.sql.weaver.c;

public class IsNull extends AbstractSingleValueTestingCriteria {
	public IsNull(String testing) {
		super(testing);
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	protected void calcExpression() {
		this.expr = getTesting() + (isNegative() ? " Is Not Null" : " Is Null");
	}
}