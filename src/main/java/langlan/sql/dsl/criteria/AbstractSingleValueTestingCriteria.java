package langlan.sql.dsl.criteria;

import langlan.sql.dsl.i.Criteria;

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