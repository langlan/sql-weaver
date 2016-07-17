package langlan.sql.weaver.d;

import langlan.sql.weaver.i.Criteria;
import langlan.sql.weaver.i.CriteriaStrategyAware;

/**
 * Represents a sub-sql-query used in a criteria-group.
 *
 * @param <O> the type of the owner
 */
public abstract class SubSqlCriteriaD<T extends SubSqlCriteriaD<T, O>, O extends CriteriaStrategyAware> extends SubSqlD<T, O> implements
	Criteria {
	private String pre;

	public SubSqlCriteriaD(O owner, String pre) {
		super(owner);
		this.pre = pre;
	}

	@Override
	public String toString() {
		return pre + super.toString();
	}
}
