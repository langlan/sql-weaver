package langlan.sql.weaver.c;

public class SingleValueNegativeTesting extends AbstractSingleValueTestingCriteria {
	private AbstractSingleValueTestingCriteria internal;

	public SingleValueNegativeTesting(AbstractSingleValueTestingCriteria singleValueTesting) {
		this.internal = singleValueTesting;
		if (internal instanceof IsNull) {
			this.expr = internal.testing + " Is Not Null";
		} else {
			this.expr = "Not " + internal.expr;
		}
	}

	public AbstractSingleValueTestingCriteria getInternal() {
		return this.internal;
	}

	public AbstractSingleValueTestingCriteria negative() {
		return internal;
	}

	@Override
	public Object[] vars() {
		return internal.vars();
	}
}
