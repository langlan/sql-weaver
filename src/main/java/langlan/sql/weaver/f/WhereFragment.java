package langlan.sql.weaver.f;

import langlan.sql.weaver.d.CriteriaGroupD;
import langlan.sql.weaver.d.SqlD;
import langlan.sql.weaver.i.Fragment;
import langlan.sql.weaver.u.FragmentsValidator;

import java.util.Arrays;
import java.util.List;

/**
 * @param <OSQL> The Owner : a Sql or a SubSql scope.
 */
public class WhereFragment<OSQL extends SqlD<OSQL>> extends CriteriaGroupD<WhereFragment<OSQL>, OSQL> implements Fragment, Fragment.Ignorable {

	public WhereFragment(OSQL owner, boolean orMode) {
		super(owner, orMode);
	}

	/**
	 * Terminate the 'WHERE' clause
	 *
	 * @return the owner:a Sql or a SubSql
	 */
	public OSQL endWhere() {
		return end();
	}

	@Override
	public void joinFragment(StringBuilder sb, List<Object> variables) {
		if (!isEmpty()) {
			sb.append("Where");
			sb.append(" ");
			sb.append(toString());
			variables.addAll(Arrays.asList(vars()));
		}
	}
	
	@Override
	public void validateFragmentPosition(List<Fragment> fragments) {
		FragmentsValidator.assertExistsAndNotEmpty(fragments, FromFragment.class);
		// FragmentsValidator.assertNotExists(fragments, getClass());
	}

	@Override
	public boolean isEmpty() {
		return getAppliedCriteria().isEmpty();
	}
}