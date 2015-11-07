package langlan.sql.dsl.f;

import langlan.sql.dsl.i.Fragment;

import java.util.List;

public class SelectFragment extends AbstractListFragment {

	public SelectFragment() {
		super();
	}

	public SelectFragment(String items, Object[] bindVariables) {
		super(items, bindVariables);
	}

	@Override
	public void validateFragmentPosition(List<Fragment> fragments) {
		// FragmentsValidator.assertNotExists(fragments, getClass());
	}
}