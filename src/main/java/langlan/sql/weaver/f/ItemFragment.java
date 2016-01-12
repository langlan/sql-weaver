package langlan.sql.weaver.f;

import langlan.sql.weaver.i.Fragment;
import langlan.sql.weaver.u.FragmentsValidator;

import java.util.List;

public class ItemFragment extends CustomFragment {
	public ItemFragment(String fragment, Object... bindVariables) {
		super(fragment, bindVariables);
	}

	@Override
	public void validateFragmentPosition(List<Fragment> fragments) {
		FragmentsValidator.assertLastFragmentInstanceof(fragments, AbstractListFragment.class);
	}
}
