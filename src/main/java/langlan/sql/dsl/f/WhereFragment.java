package langlan.sql.dsl.f;

import langlan.sql.dsl.d.CriteriaGroupD;
import langlan.sql.dsl.d.SqlD;
import langlan.sql.dsl.i.Fragment;
import langlan.sql.dsl.u.FragmentsValidator;

import java.util.Arrays;
import java.util.List;

/**
 * @param <OSQL> The Owner : a Sql or a SubSql scope.
 */
public class WhereFragment<OSQL extends SqlD<OSQL>> extends CriteriaGroupD<WhereFragment<OSQL>, OSQL> implements Fragment {

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
		if (!this.getAppliedCriterias().isEmpty()) {
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
}