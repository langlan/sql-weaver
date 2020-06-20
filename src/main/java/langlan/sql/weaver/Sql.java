package langlan.sql.weaver;

import langlan.sql.weaver.d.CriteriaGroupD;
import langlan.sql.weaver.d.InlineStrategySupport;
import langlan.sql.weaver.d.SqlD;
import langlan.sql.weaver.e.SqlSyntaxException;
import langlan.sql.weaver.i.CriteriaStrategy;
import langlan.sql.weaver.i.VariablesBound;

/**
 * Usage:
 * <pre>
 * Sql weaver = new Sql()
 * <span style="color:blue;font-weight: bold;">(</span>
 * .select("a.*"[, var1, var2...]).from("A a")
 * <span style="color:blue;font-weight: bold;">|</span>
 * [.select([...])<span style="color:gray;">[.$(condition)]</span>]<span style="color:blue;">* -> 1 applied</span>
 *   [.____(item, [, var1, var2...])<span style="color:gray;">[.$(condition)]</span>]<span style="color:blue;font-weight: bold;">*</span>
 * [.from("...")<span style="color:gray;">[.$(condition)]</span>]<span style="color:blue;">* -> 1 applied</span>
 * <span style="color:blue;font-weight: bold;">)</span>
 * [.[leftJoin|rightJoin|fullJoin|join|crossJoin]("...")[$(condition)]]<span style="color:blue;font-weight: bold;">*</span>
 * [
 * .where()
 *   [.eq("a.name", var)<span style="color:gray;"><span style="color:gray;">[.$(condition)]</span></span>]
 *   [.gt("a.age", var)<span style="color:gray;">[.$(condition)]</span>]
 *   [.ge("a.age", var)<span style="color:gray;">[.$(condition)]</span>]
 *   [.lt("a.age", var)<span style="color:gray;">[.$(condition)]</span>]
 *   [.le("a.age", var)<span style="color:gray;">[.$(condition)]</span>]
 *   [.between("a.age", var, var)<span style="color:gray;">[.$(condition)]</span>]
 *   [.like("a.name", var)<span style="color:gray;">[.$(condition)]</span>]
 *   [.notLike("a.name", var)<span style="color:gray;">[.$(condition)]</span>]
 *   [.in("a.name", vars)<span style="color:gray;">[.$(condition)]</span>]
 *   [.notIn("a.name", vars)<span style="color:gray;">[.$(condition)]</span>]
 *   [.__("a.name", vars)<span style="color:gray;">[.$(condition)]</span>]
 *   [.grp([boolean])...endGrp()<span style="color:gray;">[.$(condition)]</span>]
 *   [.exists().select("1").from("B b")
 *     ...
 *   .endExists()[$(condition)]
 *   ]
 *   [.in("a.name").select("c.attr").from("C c")
 *     ...
 *   .endIn()[$(condition)]
 *   ]
 *   [.notExists()....endNotExists()<span style="color:gray;">[.$(condition)]</span>]
 *   [.notIn("a.xxx")....endNotIn()<span style="color:gray;">[.$(condition)]</span>]
 *  .endWhere()
 * ]
 * [.orderBy("...")<span style="color:gray;">[.$(condition)]</span>]<span style="color:blue;">* -> 1 or 0 applied</span>
 *   [.____(item, [, var1, var2...])<span style="color:gray;">[.$(condition)]</span>]<span style="color:blue;font-weight: bold;">*</span>
 * [.groupBy("...")<span style="color:gray;">[.$(condition)]</span>]<span style="color:blue;">* -> 1 or 0 applied</span>
 *   [.____(item, [, var1, var2...])<span style="color:gray;">[.$(condition)]</span>]<span style="color:blue;font-weight: bold;">*</span>
 * [.endSql()];
 * return dao.find(weaver.toString(), weaver.vars());
 * </pre>
 *
 * <b>NOTE</b> the following semantics of pairs of methods:
 * <ul>
 * <li>Both <code>Sql.where([boolean])...endWhere()</code> And <code>.grp([boolean])...endGrp()</code> are entering and
 * leaving a CriteriaGroup。</li>
 * <li>Following code are entering and leaving a SubSqlCriteria: <br>
 * <code>.sub("[Not ][Exists|In]")....endSub() <br>
 * .exists()...endExists() <br>
 * .in("..")....endIn() <br>
 * .notExists()....endNotExists() <br>
 * .notIn("..")....endNotIn() <br>
 * </code></li>
 * </ul>
 * About [fragment/criteria apply strategy] see {@link #setCriteriaStrategy(CriteriaStrategy)} and {@link
 * InlineStrategySupport#$(Boolean)}
 *
 * @see CriteriaGroupD
 * @see VariablesBound
 * @see CriteriaStrategy
 */
public class Sql extends SqlD<Sql> {

	@Override
	public Sql $(Boolean b) {
		Sql ret = super.$(b);
		if ($isSelfInvalid()) {
			throw new SqlSyntaxException("Cannot Make the TOP-LEVEL-SQL Invalid!");
		}
		return ret;
	}

	/**
	 * Optional : End the Sql EXPLICITLY.<br>
	 * After this action, Sql instance can not be add or remove(by {@link #$(Boolean)}) any Fragment, neither
	 * EXPLICITLY-End it again.
	 */
	@Override
	public Sql endSql() {
		return super.endSql();
	}
}
