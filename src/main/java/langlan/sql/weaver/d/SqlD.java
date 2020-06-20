package langlan.sql.weaver.d;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import langlan.sql.weaver.c.strategy.DefaultCriteriaStrategy;
import langlan.sql.weaver.e.SqlSyntaxException;
import langlan.sql.weaver.f.AbstractListFragment;
import langlan.sql.weaver.f.CustomFragment;
import langlan.sql.weaver.f.FromFragment;
import langlan.sql.weaver.f.GroupByFragment;
import langlan.sql.weaver.f.ItemFragment;
import langlan.sql.weaver.f.JoinFragment;
import langlan.sql.weaver.f.OrderByFragment;
import langlan.sql.weaver.f.SelectFragment;
import langlan.sql.weaver.f.WhereFragment;
import langlan.sql.weaver.i.CriteriaStrategy;
import langlan.sql.weaver.i.CriteriaStrategyAware;
import langlan.sql.weaver.i.Fragment;
import langlan.sql.weaver.i.Fragment.Ignorable;
import langlan.sql.weaver.i.VariablesBound;

/**
 * @param <T> the concrete type derived from this type
 */
public abstract class SqlD<T extends SqlD<T>> extends InlineStrategySupport<T>
		implements CriteriaStrategyAware, VariablesBound {
	private int version, buildVersion;
	private String generatedSql;
	private Object[] vars;
	private LinkedList<Fragment> fragments;
	private boolean endedExplicitly;
	private CriteriaStrategy criteriaStrategy = DefaultCriteriaStrategy.INSTANCE;

	/**
	 * Append custom fragment to the weaver.
	 */
	public T __(String fragment, Object... bindVariables) {
		return addFragment(new CustomFragment(fragment, bindVariables));
	}

	/**
	 * Append custom item-fragment to the previous clause of the weaver.<br>
	 * The previous clause could be ('Select'|'From'|'Order by'|'Group By'). <br>
	 * NOTE: DO NOT ADD Delimiter!
	 *
	 * @throws SqlSyntaxException if the invoke-position is not allowed.
	 */
	public T ____(String fragment, Object... bindVariables) throws SqlSyntaxException {
		return addFragment(new ItemFragment(fragment, bindVariables));
	}

	@Override
	public T $(Boolean b) {
		assertNotEnded();
		// if (getBranch().isEntered()) {
		return super.$(b);
		// }
	}

	@Override
	protected T $invalidLastItem() {
		Fragment f = fragments.get(fragments.size() - 1);
		if (f instanceof AbstractListFragment) {
			Object fi = ((AbstractListFragment) f).peekItem();
			if (fi != null && fi instanceof Fragment) {
				((AbstractListFragment) f).popItem();
			} else {
				fragments.removeLast();
			}
		} else {
			fragments.removeLast();
		}
		version++;
		return super.$invalidLastItem();
	}

	/** assert not ended explicitly */
	protected void assertNotEnded() {
		if (endedExplicitly) {
			throw new SqlSyntaxException("The Sql is ended!");
		}
	}

	protected T addFragment(Fragment fragment) {
		assertNotEnded();
		if (fragments == null) {
			fragments = new LinkedList<Fragment>();
		}
		fragment.validateFragmentPosition(Collections.unmodifiableList(fragments));
		if (fragment instanceof ItemFragment) {
			Fragment f = fragments.getLast();
			((AbstractListFragment) f).pushItem(fragment);
		} else {
			fragments.add(fragment);
		}
		version++;
		return this.$setInvokable();
	}

	public T select() {
		return addFragment(new SelectFragment());
	}

	public T select(String items, Object... bindVariables) {
		return addFragment(new SelectFragment(items, bindVariables));
	}

	public T from(String... items) {
		return addFragment(new FromFragment(items));
	}

	/** Add a Left(-Outer)-Join Clause */
	public T leftJoin(String expr) {
		return addFragment(new JoinFragment(JoinFragment.LEFT, expr));
	}

	/** Add a Right(-Outer)-Join Clause */
	public T rightJoin(String expr) {
		return addFragment(new JoinFragment(JoinFragment.RIGHT, expr));
	}

	/** Add a Full(-Outer)-Join Clause */
	public T fullJoin(String expr) {
		return addFragment(new JoinFragment(JoinFragment.FULL, expr));
	}

	/** Add a (Inner-)Join Clause */
	public T join(String expr) {
		return addFragment(new JoinFragment(JoinFragment.INNER, expr));
	}

	public T on(String expr, Object... bindVariables) {
		Fragment f = fragments.getLast();
		if(f==null || !(f instanceof JoinFragment)) {
			throw new SqlSyntaxException("On Should follow a Join");
		}
		return __("On (" + expr + ")", bindVariables);
	}

	/** Add a Cross-Join Clause */
	public T crossJoin(String expr) {
		return addFragment(new JoinFragment(JoinFragment.CROSS, expr));
	}

	/**
	 * @return a {@link WhereFragment} whose orMode is false(And Mode).。
	 * @see #where(boolean)
	 */
	public WhereFragment<T> where() {
		return where(false);
	}

	/**
	 * @param or whether the CriteriaGroup is 'OR' Mode or 'AND' Mode.
	 * @return a {@link WhereFragment} whose orMode is param <code>or</code>.。
	 */
	public WhereFragment<T> where(boolean or) {
		WhereFragment<T> where = new WhereFragment<T>(realThis(), or);
		addFragment(where);
		return where;
	}

	public T orderBy(String expr) {
		return addFragment(new OrderByFragment(expr));
	}

	public T groupBy(String expr) {
		return addFragment(new GroupByFragment(expr));
	}

	public T having(String expr, Object... vars) {
		return addFragment(new CustomFragment("Having " + expr, vars));
	}

	public String toString() {
		endImplicitly();
		return generatedSql;
	}

	public Object[] vars() {
		endImplicitly();
		return vars;
	}

	private void endImplicitly() {
		if (version != buildVersion) {
			Iterator<Fragment> it = fragments.iterator();
			StringBuilder sb = new StringBuilder();
			LinkedList<Object> variables = new LinkedList<Object>();
			while (it.hasNext()) {
				Fragment fragment = it.next();
				if (fragment instanceof Ignorable && ((Ignorable) fragment).isEmpty()) {
					continue;
				} else if (sb.length() > 0) {
					sb.append(" ");
				}
				fragment.joinFragment(sb, variables);
			}
			vars = variables.toArray();
			generatedSql = sb.toString();
			buildVersion = version;
		}
	}

	// end weaver EXPLICITLY
	protected T endSql() {
		if (endedExplicitly) {
			throw new SqlSyntaxException("Sql is already ended!");
		}
		endImplicitly();
		endedExplicitly = true;
		return realThis();
	}

	/**
	 * Optional : Set a custom CriteriaStrategy.
	 * 
	 * @see DefaultCriteriaStrategy
	 */
	public T setCriteriaStrategy(CriteriaStrategy criteriaStrategy) {
		this.criteriaStrategy = criteriaStrategy;
		return realThis();
	}

	public CriteriaStrategy getCriteriaStrategy() {
		return criteriaStrategy;
	}
}
