package langlan.sql.weaver.d;

import langlan.sql.weaver.c.*;
import langlan.sql.weaver.c.SubSqlCriteria.ExistsSubSql;
import langlan.sql.weaver.c.SubSqlCriteria.InSubSql;
import langlan.sql.weaver.c.SubSqlCriteria.NotExistsSubSql;
import langlan.sql.weaver.c.SubSqlCriteria.NotInSubSql;
import langlan.sql.weaver.e.SqlSyntaxException;
import langlan.sql.weaver.i.Criteria;
import langlan.sql.weaver.i.CriteriaStrategy;
import langlan.sql.weaver.i.CriteriaStrategyAware;
import langlan.sql.weaver.i.VariablesBound;
import langlan.sql.weaver.u.Variables;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Represents a group of criteria. Using toString() to get the sql-fragment it represents. <br/>
 * <h1>Methods Summary</h1>
 * <ul>
 * <li> {@link #toString()} {@link #vars()}</li>
 * <li>Simple-Criteria Methods Summary
 * <ul>
 * <li> {@link #eq(String, Object)}, {@link #ne(String, Object)}</li>
 * <li> {@link #like(String, String)}, {@link #notLike(String, String)}</li>
 * <li> {@link #gt(String, Object)}, {@link #ge(String, Object)}, {@link #lt(String, Object)},
 * {@link #le(String, Object)}</li>
 * <li> {@link #between(String, Object, Object)}</li>
 * <li> {@link #in(String, Object)}, {@link #notIn(String, Object)}</li>
 * <li> {@link #isNull(String)}, {@link #isNotNull(String)}</li>
 * <li> {@link #__(String, Object...)}</li>
 * </ul>
 * </li>
 * <li>Sub-CriteriaGroup Methods Summary
 * <ul>
 * <li> {@link #grp()}, {@link #grp(boolean)} | {@link SubCriteriaGroup#endGrp()}</li>
 * </ul>
 * </li>
 * <li>Sub-Sql Methods Summary
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
	private Stack<Criteria> criteriaStack = new Stack<Criteria>();
	private List<Criteria> appliedCriteria = new LinkedList<Criteria>();
	/** false if is AndMode */
	private boolean orMode;
	/** Used for two purpose : 1. returning owner when end; 2. get owner's strategy */
	private O owner;
	/** the string representation */
	private String generatedExpression;
	private Object[] vars;

	public CriteriaGroupD(O owner, boolean orMode) {
		this.owner = owner;
		this.orMode = orMode;
	}

	/**
	 * Inline apply strategy of a simple-criteria.
	 *
	 * @param apply Inline-Apply-Flag indicates whether previous criteria should be applied. <code>null</code> will be
	 *              treated as <code>false</code>.
	 * @throws SqlSyntaxException If this method is not invoked immediate following a criteria-about method or invoked
	 *                            repeatedly.
	 */
	public T $(Boolean apply) throws SqlSyntaxException {
		assertNotEnded();
		//if (getBranch().isEntered()) {
		return super.$(apply);
		//}
	}

	@Override
	protected T $invalidLastItem() {
		criteriaStack.pop();
		return super.$invalidLastItem();
	}

	/**
	 * Add one custom criteria with some variables. ig:
	 *
	 * <pre>
	 * new Sql()....from("X x")
	 * .where()
	 * 	.__("x.a=1")
	 * 	.__("(x.b=? Or x.c=?)",obj,obj2)
	 * 	.__("Not Exists(select 1 from Y y Where y.a=2 And y.id=x.id)
	 * ...
	 * </pre>
	 */
	public T __(String exp, Object... bindVariables) {
		return addCriteria(new Custom(exp, bindVariables));
	}

	protected T addCriteria(Criteria c) {
		//if (getBranch().isEntered()) {
		assertNotEnded();
		criteriaStack.push(c);
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
			CriteriaStrategy criteriaStrategy = getCriteriaStrategy();
			for (Criteria c : criteriaStack) {
				// transformed by strategy, chance to omit criteria (if null).
				c = criteriaStrategy.apply(c);
				// omit empty criteria group
				if(c instanceof CriteriaGroupD && ((CriteriaGroupD<?, ?>) c).appliedCriteria.isEmpty()){
					c = null;
				}
				// applied
				if (c != null) {
					if (!appliedCriteria.isEmpty()) {
						sb.append(orMode ? " Or " : " And ");
					}
					appliedCriteria.add(c);
					String expr = c.toString();
					sb.append(expr);
					vars.addAll(Arrays.asList(c.vars()));
				}
			}
		}
		this.generatedExpression = sb.toString();
		this.vars = vars.toArray();
		return owner;
	}

	protected List<Criteria> getAppliedCriteria(){
		return appliedCriteria;
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
	 * weaver = new Sql().select(&quot;x.*&quot;).from(&quot;X x&quot;).where()//@fmt:off
	 *   .eq(&quot;x.a&quot;, &quot;1&quot;)
	 *   .subSql(&quot;ExistsSubSql&quot;).select(&quot;1&quot;).from(&quot;Y&quot;)
	 *   .endSubSql()
	 * .endWhere();
	 * Assert.assertEquals(plainSql, weaver.toString());
	 *
	 * weaver = new Sql().select(&quot;x.*&quot;).from(&quot;X x&quot;).where()
	 *   .eq(&quot;x.a&quot;, &quot;1&quot;)
	 *   .exists().select(&quot;1&quot;).from(&quot;Y&quot;)
	 *   .endExists()
	 * .endWhere();//@fmt:on
	 * Assert.assertEquals(plainSql, weaver.toString());
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

	@Override
	public CriteriaStrategy getCriteriaStrategy() {
		return owner.getCriteriaStrategy();
	}

	/**
	 * Add a andMode group
	 *
	 * <pre>
	 * weaver = new Sql().select(&quot;i&quot;).from(&quot;x&quot;).where()//@fmt:off
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
	 * weaver.toString());
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
	 * weaver = new Sql().select(&quot;i&quot;).from(&quot;x&quot;).where()//@fmt:off
	 *   .eq(&quot;i.a&quot;, &quot;1&quot;)
	 *   .grp(true)
	 *     .eq(&quot;i.b&quot;, &quot;2&quot;)
	 *     .eq(&quot;i.c&quot;, &quot;3&quot;)
	 *   .endGrp()
	 * .endWhere();//@fmt:on
	 * Assert.assertEquals(&quot;Select i From x Where i.a=? And (i.b=? Or i.c=?)&quot;, weaver.toString());
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

	/**
	 * Add a (IN) criteria
	 * @throws IllegalArgumentException if values is not an array or collection
	 */
	public T in(String exp, Object values) throws IllegalArgumentException {
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
	 * Add a (Like) criteria, different from {@link #like(String, String)}, this method will wrap bind-variable using
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

	/** Add a (Not Between) criteria */
	public T notBetween(String exp, Object leftValue, Object rightValue) {
		return addCriteria(new Between(exp, leftValue, rightValue).negative());
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

	/**
	 * Add a (NOT IN) criteria.
	 * @throws IllegalArgumentException if values is not an array or collection
	 */
	public T notIn(String exp, Object values) throws IllegalArgumentException{
		return addCriteria(new BinaryComparison(exp, "In", values).negative());
	}

	/** Add a (NOT Like) criteria */
	public T notLike(String testing, String value) {
		return addCriteria(new BinaryComparison(testing, "Like", value).negative());
	}

	/**
	 * Add a (NOT Like) criteria, different from {@link #notLike(String, String)}, this method will wrap bind-variable
	 * using {@link Variables#wrap4Like(String, boolean, boolean)}
	 */
	public T notLike(String testing, String value, boolean fuzzLeft, boolean fuzzRight) {
		return notLike(testing, Variables.wrap4Like(value, fuzzLeft, fuzzRight));
	}

	/**
	 * Add a SubSql clause. e.g.
	 *
	 * <pre>
	 * ...
	 * .subSql("id In") //[xxx Not In] or [Exists] or [Not Exists]
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