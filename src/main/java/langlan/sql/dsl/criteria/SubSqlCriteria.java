package langlan.sql.dsl.criteria;

import langlan.sql.dsl.d.CriteriaGroupD;
import langlan.sql.dsl.d.SubSqlCriteriaD;
import langlan.sql.dsl.i.CriteriaStrategyAware;

/**
 * Used by sub query in criteriaGroup.
 * 
 * @param <O> The Owner (where came from to sub-sql scope)
 * @see CriteriaGroupD#exists()
 * @see CriteriaGroupD#notExists()
 * @see CriteriaGroupD#in(String)
 * @see CriteriaGroupD#notIn(String)
 * @see CriteriaGroupD#subSql(String)
 */
public class SubSqlCriteria<O extends CriteriaStrategyAware> extends SubSqlCriteriaD<SubSqlCriteria<O>, O> {
	public SubSqlCriteria(O owner, String pre) {
		super(owner, pre);
	}

	/**
	 * Terminate this sub-sql scope.
	 * 
	 * @return A {@link CriteriaGroupD} where came from to this scope.
	 */
	public O endSubSql() {
		return end();
	}

	public static class ExistsSubSql<O extends CriteriaGroupD<?, ?>> extends SubSqlCriteriaD<ExistsSubSql<O>, O> {
		public ExistsSubSql(O owner) {
			super(owner, "Exists");
		}

		/**
		 * Terminate this sub-sql scope.
		 * 
		 * @return A {@link CriteriaGroupD} where came from to this scope.
		 */
		public O endExists() {
			return end();
		}
	}

	public static class NotExistsSubSql<O extends CriteriaGroupD<?, ?>> extends SubSqlCriteriaD<NotExistsSubSql<O>, O> {
		public NotExistsSubSql(O owner) {
			super(owner, "Not Exists");
		}

		/**
		 * Terminate this sub-sql scope.
		 * 
		 * @return A {@link CriteriaGroupD} where came from to this scope.
		 */
		public O endNotExists() {
			return end();
		}
	}

	public static class InSubSql<O extends CriteriaGroupD<?, ?>> extends SubSqlCriteriaD<InSubSql<O>, O> {
		public InSubSql(O owner, String testing) {
			super(owner, testing + " In");
		}

		/**
		 * Terminate this sub-sql scope.
		 * 
		 * @return A {@link CriteriaGroupD} where came from to this scope.
		 */
		public O endIn() {
			return end();
		}
	}

	public static class NotInSubSql<O extends CriteriaGroupD<?, ?>> extends SubSqlCriteriaD<NotInSubSql<O>, O> {
		public NotInSubSql(O owner, String testing) {
			super(owner, testing + " Not In");
		}

		/**
		 * Terminate this sub-sql scope.
		 * 
		 * @return A {@link CriteriaGroupD} where came from to this scope.
		 */
		public O endNotIn() {
			return end();
		}
	}
}
