package langlan.sql.dsl.f;

import langlan.sql.dsl.i.Fragment;
import langlan.sql.dsl.u.FragmentsValidator;

import java.util.List;

public class SelectFragment extends AbstractListFragment {

	public SelectFragment() {
		super();
	}

	public SelectFragment(String items, Object[] bindVariables) {
		super(items, bindVariables);
	}

	@Override
	public void join(StringBuilder sb, List<Object> variables) {
		sb.append("Select");
		if (items.isEmpty()) {
			sb.append(" ");
			sb.append("*");
		} else {
			super.join(sb, variables);
		}
	}

	@Override
	public void validate(List<Fragment> fragments) {
		FragmentsValidator.requireNotExists(fragments, getClass());
	}
}