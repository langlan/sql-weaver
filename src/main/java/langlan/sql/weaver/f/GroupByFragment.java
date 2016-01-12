package langlan.sql.weaver.f;

import langlan.sql.weaver.i.Fragment;
import langlan.sql.weaver.u.FragmentsValidator;

import java.util.List;

public class GroupByFragment extends AbstractListFragment {
	public GroupByFragment(String items) {
		super(items);
	}

	@Override
	public void validateFragmentPosition(List<Fragment> fragments) {
		FragmentsValidator.assertExistsAndNotEmpty(fragments, FromFragment.class);
		// FragmentsValidator.assertNotExists(fragments, getClass());
	}
}
