package langlan.sql.dsl.d;

import langlan.sql.dsl.e.SqlSyntaxException;
import langlan.sql.dsl.i.Criteria;

/**
 * Used in a 'FROM' cluase.
 *
 * @param <O> the type of the owner
 */
public class FromClauseSubSql<T extends FromClauseSubSql<T, O>, O extends SqlD<O>> extends SubSqlD<T, O> implements
	Criteria {
	private String alias;

	public FromClauseSubSql(O owner) {
		super(owner);
	}

	@Override
	public String toString() {
		return super.toString() + (alias != null && !alias.isEmpty() ? (" As " + alias) : "");
	}

	/**
	 * Give the sub-sql an alias.
	 *
	 * @throws SqlSyntaxException if the alias is null or empty.
	 */
	public T as(String alias) throws SqlSyntaxException {
		if (alias == null || alias.trim().isEmpty()) {
			throw new SqlSyntaxException("Alias of SubSql can not be null or empty!");
		}
		this.alias = alias;
		return realThis();
	}
}
