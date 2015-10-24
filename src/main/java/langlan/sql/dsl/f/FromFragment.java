package langlan.sql.dsl.f;

import langlan.sql.dsl.e.SqlSyntaxException;
import langlan.sql.dsl.i.Fragment;
import langlan.sql.dsl.u.FragmentsValidator;

import java.util.List;

public class FromFragment extends AbstractListFragment {

	public FromFragment(String[] items) {
		super((items));
	}

	@Override
	public void join(StringBuilder sb, List<Object> variables) {
		sb.append("From");
		if (items.isEmpty()) {
			throw new SqlSyntaxException("FROM clause is empty!");
		}
		super.join(sb, variables);
	}

	@Override
	public void validate(List<Fragment> fragments) {
		FragmentsValidator.require(fragments, SelectFragment.class);
		FragmentsValidator.requireNotExists(fragments, getClass());
	}

}
