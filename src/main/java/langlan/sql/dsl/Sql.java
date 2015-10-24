package langlan.sql.dsl;

import langlan.sql.dsl.criteria.strategy.DefaultCriteriaStrategy;
import langlan.sql.dsl.d.CriteriaGroupD;
import langlan.sql.dsl.d.SqlD;
import langlan.sql.dsl.e.SqlSyntaxException;
import langlan.sql.dsl.i.CriteriaStrategy;
import langlan.sql.dsl.i.VariablesBound;

/**
 * 此类是本包下的主要对外接口，本类提供了一些如下特性。
 * <ul>
 * <li>DSL 以及链式编程</li>
 * <li>Sql语句与绑定变量分离，使用 {@link #toString()} 和 {@link #vars()} 分别获取。</li>
 * <li>条件自动判断是否应用（是否将条件拼接到Sql之中，并添加相应变量到最终绑定变量数组中）
 * <ul>
 * <li>默认的判断依据是：如果绑定的变量是null或空值（空串，空数组，空集合）则不应用该条件。<br/>
 * 如下面的实例生成的Sql是[Select a from A a]：<br/>
 * <code>
 * new Sql().select("a").from("A a").where().like("a.name","").eq("a.id",null).endWhere()
 * </code></li>
 * <li>默认使用的判断策略是 {@link CriteriaStrategy}, 更复杂的判断策略请使用 {@link #setCriteriaStrategy(CriteriaStrategy)}</li>
 * </ul>
 * </li>
 * </ul>
 * 关于支持的所有条件，请参考 {@link CriteriaGroupD}，下面是几个简单示例：<br/>
 * 注：中括号表示可选部分。
 *
 * <pre>
 * Sql sql = new Sql().select("a.*").from("A a")
 * [.[leftJoin|rightJoin|fullJoin|join|crossJoin]("...")[$(condition)]]
 * [.where()
 *   [.eq("a.name", var)[.$(condition)]]
 *   [.gt("a.age", var)[.$(condition)]]
 *   [.ge("a.age", var)[.$(condition)]]
 *   [.lt("a.age", var)[.$(condition)]]
 *   [.le("a.age", var)[.$(condition)]]
 *   [.btween("a.age", var, var)[.$(condition)]]
 *   [.like("a.name", var)[.$(condition)]]
 *   [.notLike("a.name", var)[.$(condition)]]
 *   [.in("a.name", vars)[.$(condition)]]
 *   [.notIn("a.name", vars)[.$(condition)]]
 *   [._("a.name", vars)[.$(condition)]]
 *   [.grp([boolean])...endGrp()[.$(condition)]]
 *   [.exists().select("1").from("B b")
 *     ...
 *   .endExists()[$(condition)]
 *   ]
 *   [.in("a.name").select("c.attr").from("C c")
 *     ...
 *   .endIn()[$(condition)]
 *   ]
 *   [.notExists()....endNotExists()[.$(condition)]]
 *   [.notIn("a.xxx")....endNotIn()[.$(condition)]]
 *  .endWhere()
 * ]
 * [.orderBy("...")];
 * return dao.find(sql.toString(), sql.vals());
 * </pre>
 *
 * <b>NOTE</b> the following semantics of pairs of methods:
 * <ul>
 * <li>Both <code>Sql.where([boolean])...endWhere()</code> And <code>.grp([boolean])...endGrp()</code> are entering and
 * leaving a CriteriaGroup。</li>
 * <li>Following code are entering and leaving a SubSqlCriteria: <br/>
 * <code>.sub("[Not ][Exists|In]")....endSub() <br/>
 * .exists()...endExists() <br/> .nexists()....endNotExists()</code></li>
 * </ul>
 *
 * @see CriteriaGroupD
 * @see VariablesBound
 */
public class Sql extends SqlD<Sql> {
	private CriteriaStrategy criteriaStrategy = DefaultCriteriaStrategy.INSTANCE;

	@Override
	public Sql $(boolean b) {
		Sql ret = super.$(b);
		if ($isSelfInvalid()) {
			throw new SqlSyntaxException("Cannot Make the TOP-LEVEL-SQL Invalid!");
		}
		return ret;
	}

	public void setCriteriaStrategy(CriteriaStrategy criteriaStrategy) {
		this.criteriaStrategy = criteriaStrategy;
	}

	public CriteriaStrategy getCriteraiaStrategy() {
		return criteriaStrategy;
	}

	/**
	 * End the Sql EXPLICITLY.<br/>
	 * After this action, Sql instance can not be add or remove(by {@link #$(boolean)}) any Fragment, neither
	 * EXPLICITLY-End it again.
	 */
	@Override
	public Sql endSql() {
		return super.endSql();
	}
}
