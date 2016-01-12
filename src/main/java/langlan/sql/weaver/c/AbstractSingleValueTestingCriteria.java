package langlan.sql.weaver.c;

import langlan.sql.weaver.i.Criteria;

/**
 * For : <br/>
 * =, <=, >=, <>, BETWEEN, LIKE, IN, IS NULL, <br/>
 * NOT BETWEEN, NOT LIKE, NOT IN, IS NOT NULL
 */
public abstract class AbstractSingleValueTestingCriteria extends AbstractCriteria {
	protected String testing;

	/** Testing part expression */
	public String getTesting() {
		return testing;
	}

	public Criteria negative() {
		return new SingleValueNegativeTesting(this);
	}
}