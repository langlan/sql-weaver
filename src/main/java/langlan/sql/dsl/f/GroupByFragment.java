package langlan.sql.dsl.f;

import java.util.List;

import langlan.sql.dsl.i.Fragment;
import langlan.sql.dsl.u.FragmentsValidator;

public class GroupByFragment implements Fragment {
	private final String expr;

	public GroupByFragment(String expr) {
		this.expr = expr;
	}

	@Override
	public void join(StringBuilder sb, List<Object> variables) {
		sb.append("Group By");
		sb.append(" ");
		sb.append(expr);
	}

	@Override
	public void validate(List<Fragment> fragments) {
		FragmentsValidator.require(fragments, FromFragment.class);
		FragmentsValidator.requireNotExists(fragments, getClass());
	}
}
