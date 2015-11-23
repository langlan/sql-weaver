package langlan.sql.dsl.criteria;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** For: =|<=|>=|<>|LIKE|IN */
public class BinaryComparison extends AbstractSingleValueTestingCriteria {
	// final String LIKE = "Like", IN = "In", EQ = "=", GE = ">=", LE = "<=", GT = ">", LT = "<";
	static Pattern OPERATORS = Pattern.compile("(=|<=|>=|<>)|(LIKE)|(IN)", Pattern.CASE_INSENSITIVE);
	private String operator;

	public BinaryComparison(String testing, String compareOperator, Object right) {
		this.operator = compareOperator;
		this.testing = testing;
		this.vars = new Object[]{right};
		Matcher m = OPERATORS.matcher(operator);
		if (m.find()) {
			if (m.group(1) != null) {
				this.expr = (testing + operator + "?");
			} else if (m.group(2) != null) {
				this.expr = (testing + " " + operator + " ?");
			} else if (m.group(3) != null) {
				if (right != null && !(right instanceof Object[]) && !(right instanceof Collection)) {
					throw new IllegalArgumentException("The Bind-Variable of 'IN' Should be array-type or Collection-type");
				}
				this.expr = (testing + " " + operator + " (?)");
			}
		} else {
			throw new IllegalArgumentException("Not Supported Binary-Comparison Operator: " + operator);
		}
	}

	public Object getBoundValue() {
		return vars[0];
	}

	/** the operator of compare operation : <code>=,>,<,>=,<=,<>,Like,In...</code> */
	public String getOperator() {
		return operator;
	}
}