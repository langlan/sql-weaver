package langlan.sql.weaver.c;

import langlan.sql.weaver.i.Criteria;

/**
 * For : <br/>
 * =, >, <, >=, <=, <>, <br/>
 * LIKE, IN, BETWEEN, IS NULL, <br/>
 * NOT LIKE, NOT IN, NOT BETWEEN, IS NOT NULL
 */
public abstract class AbstractSingleValueTestingCriteria extends AbstractCriteria {
	private String testing;
	private boolean negative;

	AbstractSingleValueTestingCriteria(String testing){
		this(testing, true);
	}

	AbstractSingleValueTestingCriteria(String testing, boolean calcImmediate) {
		this.testing = testing;
		if(calcImmediate){
			calcExpression();
		}
	}

	/** Testing part expression */
	public String getTesting() {
		return testing;
	}

	/**
	 * flip the flag of "using 'Not' keyword".
	 * @throws IllegalArgumentException if this criteria represents '=, >, <, >=, <=, <>'
	 */
	public Criteria negative() throws IllegalArgumentException{
		this.negative = !negative;
		calcExpression();
		return this;
	}

	abstract protected void calcExpression();

	/**
	 * Test if using 'Not' keyword, always <code>false</code> if this criteria represents '=, >, <, >=, <=, <>'
	 * @see #negative
	 */
	public boolean isNegative() {
		return negative;
	}
}