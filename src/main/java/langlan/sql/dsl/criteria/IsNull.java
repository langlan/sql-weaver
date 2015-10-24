package langlan.sql.dsl.criteria;

public class IsNull extends AbstractSingleValueTestingCriteria {
	public IsNull(String testing) {
		this.testing = testing;
		this.expr = testing + " Is Null";
	}
}