package langlan.sql.dsl.d;

import langlan.sql.dsl.i.CriteriaStrategy;
import langlan.sql.dsl.i.CriteriaStrategyAware;

/**
 * Used by sub-sql-query in a criteria-group
 *
 * @param <O> the type of the owner
 */
public abstract class SubSqlD<T extends SubSqlD<T, O>, O extends CriteriaStrategyAware> extends SqlD<T> {
	private O owner;

	public SubSqlD(O owner) {
		this.owner = owner;
	}

	protected O end() {
		super.endSql();
		return this.owner;
	}

	@Override
	public String toString() {
		return "(" + super.toString() + ")";
	}

	@Override
	public CriteriaStrategy getCriteraiaStrategy() {
		return owner.getCriteraiaStrategy();
	}
}
