package langlan.sql.dsl.f;

import langlan.sql.dsl.i.Fragment;
import langlan.sql.dsl.u.FragmentsValidator;

import java.util.List;

public class OrderByFragment extends AbstractListFragment {
	public OrderByFragment(String items) {
		super(items);
	}

	@Override
	public void validateFragmentPosition(List<Fragment> fragments) {
		FragmentsValidator.assertExistsAndNotEmpty(fragments, FromFragment.class);
		// FragmentsValidator.assertNotExists(fragments, getClass());
	}
}
