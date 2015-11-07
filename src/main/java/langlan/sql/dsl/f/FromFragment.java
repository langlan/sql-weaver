package langlan.sql.dsl.f;

import langlan.sql.dsl.i.Fragment;
import langlan.sql.dsl.u.FragmentsValidator;

import java.util.List;

public class FromFragment extends AbstractListFragment {

	public FromFragment(String[] items) {
		super((items));
	}

	@Override
	public void validateFragmentPosition(List<Fragment> fragments) {
		FragmentsValidator.assertExistsAndNotEmpty(fragments, SelectFragment.class);
		// FragmentsValidator.assertNotExists(fragments, getClass());
	}

}
