package langlan.sql.weaver.c;

import langlan.sql.weaver.e.DevException;
import langlan.sql.weaver.i.Criteria;
import langlan.sql.weaver.u.Variables;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** For: =, >, <, >=, <=, <>, LIKE, IN */
public class BinaryComparison extends AbstractSingleValueTestingCriteria {
	private static final Pattern OPERATORS = Pattern.compile("^(=)|(>)|(<)|(>=)|(<=)|(<>)|(LIKE)|(IN)$", Pattern.CASE_INSENSITIVE);
	public static final int TYPE_EQ = 1, TYPE_GT = 2, TYPE_LT = 3, TYPE_GE = 4, TYPE_LE = 5, TYPE_NE = 6, TYPE_LIKE = 7, TYPE_IN = 8;
	private String operator;
	private int type;

	/**
	 * @throws IllegalArgumentException <ul
	 *                                  <li>if <code>compareOperator</code> is none of : <code>=, >, <, >=, <=, <>,
	 *                                  LIKE, IN</code></li>
	 *                                  <li>if <code>compareOperator</code> is <code>IN</code> but bind-variable is not
	 *                                  null but neither an array/collection</li>
	 *                                  </ul>
	 */
	public BinaryComparison(String testing, String compareOperator, Object right) throws IllegalArgumentException {
		super(testing, false);
		this.operator = compareOperator;
		this.vars = new Object[]{right};
		Matcher m = OPERATORS.matcher(operator);
		if (m.find()) {
			for (int i = 1; i <= m.groupCount(); i++) {
				if (m.group(i) != null) {
					this.type = i;
					break;
				}
			}
		} else {
			throw new IllegalArgumentException("Not Supported Binary-Comparison Operator: " + operator);
		}
		if (this.type == TYPE_IN) {
			//!(right instanceof Object[]) only test array of Object
			if (right != null && !(right instanceof Collection) && !(right.getClass().isArray())) {
				throw new IllegalArgumentException("The Bind-Variable of 'IN' Should be array-type or Collection-type");
			}
		}
		calcExpression();
	}

	public Object getBoundValue() {
		return vars[0];
	}

	/** the operator of compare operation : <code>=,>,<,>=,<=,<>,Like,In</code> */
	public String getOperator() {
		return operator;
	}

	/**
	 * {@link #TYPE_EQ} {@link #TYPE_GT} {@link #TYPE_LT} {@link #TYPE_GE} {@link #TYPE_LE} {@link #TYPE_NE} {@link
	 * #TYPE_LIKE} {@link #TYPE_IN}
	 */
	public int getOperatorType() {
		return type;
	}

	@Override
	public Criteria negative() throws IllegalArgumentException {
		if (getOperatorType() != TYPE_LIKE && getOperatorType() != TYPE_IN) {
			throw new IllegalArgumentException("Not support negative of ");
		}
		return super.negative();
	}

	@Override
	protected void calcExpression() {
		switch (getOperatorType()) {
			case TYPE_EQ:
			case TYPE_GT:
			case TYPE_LT:
			case TYPE_GE:
			case TYPE_LE:
			case TYPE_NE:
				this.expr = getTesting() + getOperator() + "?";
				break;
			case TYPE_LIKE:
				this.expr = getTesting() + (isNegative() ? " Not " : " ") + getOperator() + " ?";
				break;
			case TYPE_IN:
				this.expr = getTesting() + (isNegative() ? " Not " : " ") + getOperator() + " (?)";
				break;
			default:
				throw new DevException("Code not supposed to reach here!");
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof BinaryComparison) {
			BinaryComparison other = (BinaryComparison) obj;
			return getTesting().equals(other.getTesting()) && (isNegative() == other.isNegative()) && Variables.equals(getBoundValue(), other.getBoundValue());
		}
		return false;
	}
}