package langlan.sql.dsl.d;

import langlan.sql.dsl.criteria.*;
import langlan.sql.dsl.criteria.SubSqlCriteria.ExistsSubSql;
import langlan.sql.dsl.criteria.SubSqlCriteria.InSubSql;
import langlan.sql.dsl.criteria.SubSqlCriteria.NotExistsSubSql;
import langlan.sql.dsl.criteria.SubSqlCriteria.NotInSubSql;
import langlan.sql.dsl.e.SqlSyntaxException;
import langlan.sql.dsl.i.Criteria;
import langlan.sql.dsl.i.CriteriaStrategy;
import langlan.sql.dsl.i.CriteriaStrategyAware;
import langlan.sql.dsl.i.VariablesBound;
import langlan.sql.dsl.u.Variables;

import java.util.*;

/**
 * Represents a group of criteria. Using toString() to get the sql-fragment it represents. <br/>
 * <h1>Methods Summary</h1>
 * <ul>
 * <li> {@link #toString()} {@link #vars()}</li>
 * <li>Criterias Summary
 * <ul>
 * <li> {@link #eq(String, Object)}, {@link #ne(String, Object)}</li>
 * <li> {@link #like(String, String)}, {@link #notLike(String, String)}</li>
 * <li> {@link #gt(String, Object)}, {@link #ge(String, Object)}, {@link #lt(String, Object)},
 * {@link #le(String, Object)}</li>
 * <li> {@link #between(String, Object, Object)}</li>
 * <li> {@link #in(String, Object)}, {@link #notIn(String, Object)}</li>
 * <li> {@link #isNull(String)}, {@link #isNotNull(String)}</li>
 * <li> {@link #_(String, Object...)}</li>
 * </ul>
 * </li>
 * <li>Sub CriteriaGroup
 * <ul>
 * <li> {@link #grp()}, {@link #grp(boolean)} | {@link SubCriteriaGroup#endGrp()}</li>
 * </ul>
 * </li>
 * <li>Sub Sql Summary
 * <ul>
 * <li> {@link #subSql(String)}, {@link SubSqlCriteria#endSubSql()}</li>
 * <li> {@link #exists()}, {@link ExistsSubSql#endExists()}</li>
 * <li> {@link #notExists()}, {@link NotExistsSubSql#endNotExists()}</li>
 * <li> {@link #in(String)}, {@link InSubSql#endIn()}</li>
 * <li> {@link #notIn(String)}, {@link NotInSubSql#endNotIn()}</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @param <T> the concrete type derived from this type
 * @param <O> the type of the owner(parent scope)
 */
public abstract class CriteriaGroupD<T extends CriteriaGroupD<T, O>, O extends CriteriaStrategyAware & VariablesBound>
	extends InlineStrategySupport<T>
	implements CriteriaStrategyAware, VariablesBound {
	private Stack<Criteria> criterias = new Stack<Criteria>();
	private List<Criteria> appliedCriterias = new LinkedList<Criteria>();
	/** false if is AndMode */
	private boolean orMode;
	/** Used for two purpose : 1. reutrning owner when end; 2. get owner's strategy */
	private O owner;
	/** the string representation */
	private String generatedExpression;
	private Object[] vars;

	public CriteriaGroupD(O owner, boolean orMode) {
		this.owner = owner;
		this.orMode = orMode;
	}

	/**
	 * Inline apply strategy of a signle criteria.
	 *
	 * @param apply Inline-Apply-Flag indicates whether previous criteria should be applied.
	 * @throws SqlSyntaxException If this method is not invoked immediate following a criteria-about method or invoked
	 *                            repeatedly.
	 */
	public T $(boolean apply) throws SqlSyntaxException {
		assertNotEnded();
		//if (getBranch().isEntered()) {
		return super.$(apply);
		//}
	}

	@Override
	protected T $invalidLastItem() {
		criterias.pop();
		return super.$invalidLastItem();
	}

	/**
	 * Add one custom criteria with some variables. ig:
	 *
	 * <pre>
	 * new Sql()....from("X x")
	 * .where()
	 * 	._("x.a=1")
	 * 	._("(x.b=? Or x.c=?)",obj,obj2)
	 * 	._("Not Exists(select 1 from Y y Where y.a=2 And y.id=x.id)
	 * ...
	 * </pre>
	 */
	public T _(String exp, Object... bindVariables) {
		return addCriteria(new Custom(exp, bindVariables));
	}

	protected T addCriteria(Criteria c) {
		//if (getBranch().isEntered()) {
		assertNotEnded();
		criterias.push(c);
		//}
		return $setInvokable();
	}

	private void assertNotEnded() {
		if (generatedExpression != null) {
			throw new SqlSyntaxException("The group is already ended.");
		}
	}

	/**
	 * Add a (Between) criteria
	 *
	 * @param exp
	 * @param leftValue
	 * @param rightValue
	 */
	public T between(String exp, Object leftValue, Object rightValue) {
		Between criteria = new Between(exp, leftValue, rightValue);
		return addCriteria(criteria);
	}

	/** Terminate this current group scope. */
	protected O end() {
		assertNotEnded();
		//getBranch().validate();
		StringBuilder sb = new StringBuilder();
		List<Object> vars = new LinkedList<Object>();
		if (!$isSelfInvalid()) {
			CriteriaStrategy criteriaStrategy = getCriteraiaStrategy();
			Iterator<Criteria> it = criterias.iterator();
			while (it.hasNext()) {
				Criteria c = it.next();
				c = criteriaStrategy.apply(c);
				if (c != null) {
					appliedCriterias.add(c);
					String expr = c.toString();
					sb.append(expr);
					vars.addAll(Arrays.asList(c.vars()));
					if (it.hasNext()) {
						sb.append(orMode ? " Or " : " And ");
					}
				}
			}
		}
		this.generatedExpression = sb.toString();
		this.vars = vars.toArray();
		return owner;
	}

	/** Add a Equal criteria */
	public T eq(String exp, Object value) {
		return addCriteria(new BinaryComparison(exp, "=", value));
	}

	/**
	 * Add a [Exists (SubSql)] criteria.<br/>
	 * same as sub("Exists"), but should be ended by endExists() instead of endSubSql(); e.g.
	 *
	 * <pre>
	 * String plainSql = &quot;Select x.* From X x Where x.a=? And Exists(Select 1 From Y)&quot;;
	 * sql = new Sql().select(&quot;x.*&quot;).from(&quot;X x&quot;).where()//@fmt:off
	 *   .eq(&quot;x.a&quot;, &quot;1&quot;)
	 *   .subSql(&quot;ExistsSubSql&quot;).select(&quot;1&quot;).from(&quot;Y&quot;)
	 *   .endSubSql()
	 * .endWhere();
	 * Assert.assertEquals(plainSql, sql.toString());
	 *
	 * sql = new Sql().select(&quot;x.*&quot;).from(&quot;X x&quot;).where()
	 *   .eq(&quot;x.a&quot;, &quot;1&quot;)
	 *   .exists().select(&quot;1&quot;).from(&quot;Y&quot;)
	 *   .endExists()
	 * .endWhere();//@fmt:on
	 * Assert.assertEquals(plainSql, sql.toString());
	 *
	 * <pre>
	 */
	public SubSqlCriteria.ExistsSubSql<T> exists() {
		SubSqlCriteria.ExistsSubSql<T> ret = new SubSqlCriteria.ExistsSubSql<T>(realThis());
		addCriteria(ret);
		return ret;
	}

	/** add a (Greater or Equal) criteria */
	public T ge(String exp, Object value) {
		return addCriteria(new BinaryComparison(exp, ">=", value));
	}

	public List<Criteria> getAppliedCriterias() {
		return appliedCriterias;
	}

	@Override
	public CriteriaStrategy getCriteraiaStrategy() {
		return owner.getCriteraiaStrategy();
	}

	/**
	 * Add a andMode group
	 *
	 * <pre>
	 * sql = new Sql().select(&quot;i&quot;).from(&quot;x&quot;).where()//@fmt:off
	 *    .eq(&quot;i.a&quot;, &quot;1&quot;)
	 *    .grp(true)
	 *       .eq(&quot;i.b&quot;, &quot;2&quot;)
	 *       .eq(&quot;i.c&quot;, &quot;3&quot;)
	 *       .grp()
	 *      	.eq(&quot;i.d&quot;, &quot;4&quot;)
	 *      	.eq(&quot;i.e&quot;, &quot;5&quot;)
	 *       .endGrp()
	 *    .endGrp()
	 * .endWhere();//@fmt:on
	 * Assert.assertEquals(&quot;Select i From x Where i.a=? And (i.b=? Or i.c=? Or (i.d=? And i.e=?))&quot;,
	 * sql.toString());
	 * </pre>
	 *
	 * @return
	 */
	public SubCriteriaGroup<T> grp() {
		return grp(false);
	}

	/**
	 * Add a orMode group. e.g.
	 *
	 * <pre>
	 * sql = new Sql().select(&quot;i&quot;).from(&quot;x&quot;).where()//@fmt:off
	 *   .eq(&quot;i.a&quot;, &quot;1&quot;)
	 *   .grp(true)
	 *     .eq(&quot;i.b&quot;, &quot;2&quot;)
	 *     .eq(&quot;i.c&quot;, &quot;3&quot;)
	 *   .endGrp()
	 * .endWhere();//@fmt:on
	 * Assert.assertEquals(&quot;Select i From x Where i.a=? And (i.b=? Or i.c=?)&quot;, sql.toString());
	 * </pre>
	 */
	public SubCriteriaGroup<T> grp(boolean orMode) {
		SubCriteriaGroup<T> ret = new SubCriteriaGroup<T>(realThis(), orMode);
		addCriteria(ret);
		return ret;
	}

	/** Add a (Greater than) criteria */
	public T gt(String exp, Object value) {
		return addCriteria(new BinaryComparison(exp, ">", value));
	}

	/**
	 * Add a (xxx IN $SubSql) clause.
	 */
	public SubSqlCriteria.InSubSql<T> in(String testing) {
		InSubSql<T> ret = new SubSqlCriteria.InSubSql<T>(realThis(), testing);
		addCriteria(ret);
		return ret;
	}

	/** Add a (IN) criteria */
	public T in(String exp, Object values) {
		return addCriteria(new BinaryComparison(exp, "In", values));
	}

	/** Add a (Is Not Null) criteria */
	public T isNotNull(String testing) {
		return addCriteria(new IsNull(testing).negative());
	}

	/** Add a (Is Null) criteria */
	public T isNull(String testing) {
		return addCriteria(new IsNull(testing));
	}

	/** Add a (Less or Equal) criteria */
	public T le(String exp, Object value) {
		return addCriteria(new BinaryComparison(exp, "<=", value));
	}

	/** Add a (Like) criteria */
	public T like(String exp, String value) {
		return addCriteria(new BinaryComparison(exp, "Like", value));
	}

	/**
	 * Add a (Like) criteria, diffence from {@link #like(String, String)}, this method will wrap bind-variale using
	 * {@link Variables#wrap4Like(String, boolean, boolean)}
	 */
	public T like(String testing, String value, boolean fuzzLeft, boolean fuzzRight) {
		return like(testing, Variables.wrap4Like(value, fuzzLeft, fuzzRight));
	}

	/** Add a (Less than) criteria */
	public T lt(String exp, Object value) {
		return addCriteria(new BinaryComparison(exp, "<", value));
	}

	/** Add a (Not Equal) criteria */
	public T ne(String exp, Object value) {
		return addCriteria(new BinaryComparison(exp, "<>", value));
	}

	/**
	 * Add a [Not Exists $SubSql] criteria.<br/>
	 * same as sub("Not Exists"), but should ended by endNotExists() instead of endSubSql();
	 */
	public SubSqlCriteria.NotExistsSubSql<T> notExists() {
		SubSqlCriteria.NotExistsSubSql<T> ret = new SubSqlCriteria.NotExistsSubSql<T>(realThis());
		addCriteria(ret);
		return ret;
	}

	/**
	 * Add a (xxx NOT IN $SubSql) clause.
	 */
	public NotInSubSql<T> notIn(String testing) {
		NotInSubSql<T> ret = new SubSqlCriteria.NotInSubSql<T>(realThis(), testing);
		addCriteria(ret);
		return ret;
	}

	/** Add a (NOT IN) criteria. */
	public T notIn(String exp, Object values) {
		return addCriteria(new BinaryComparison(exp, "In", values).negative());
	}

	/** Add a (NOT Like) criteria */
	public T notLike(String testing, String value) {
		return addCriteria(new BinaryComparison(testing, "Like", value).negative());
	}

	/**
	 * Add a (NOT Like) criteria, diffence from {@link #notLike(String, String)}, this method will wrap bind-variale
	 * using {@link Variables#wrap4Like(String, boolean, boolean)}
	 */
	public T notLike(String testing, String value, boolean fuzzLeft, boolean fuzzRight) {
		return notLike(testing, Variables.wrap4Like(value, fuzzLeft, fuzzRight));
	}

	/**
	 * @return num of citerias.
	 */
	public int size() {
		return criterias.size();
	}

	/**
	 * Add a SubSql clause. e.g.
	 *
	 * <pre>
	 * ...
	 * .sub("id In") //[xxx Not In] or [Exists] or [Not Exists]
	 * 	.select(***)
	 * 	.from(***)
	 * 	.where()....endWhere()
	 * .endSubSql()
	 * ...
	 * </pre>
	 *
	 * @see SubSqlCriteria#endSubSql()
	 * @see #exists()
	 * @see #notExists()
	 */
	public SubSqlCriteria<T> subSql(String pre) {
		SubSqlCriteria<T> ret = new SubSqlCriteria<T>(realThis(), pre);
		addCriteria(ret);
		return ret;
	}

	public String toString() {
		return generatedExpression;
	}

	public Object[] vars() {
		return vars;
	}
}