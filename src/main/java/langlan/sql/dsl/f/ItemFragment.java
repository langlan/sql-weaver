package langlan.sql.dsl.f;

import langlan.sql.dsl.i.Fragment;
import langlan.sql.dsl.u.FragmentsValidator;

import java.util.List;

public class ItemFragment extends CustomFragment {
	public ItemFragment(String fragment, Object... bindVariables) {
		super(fragment, bindVariables);
	}

	@Override
	public void validate(List<Fragment> fragments) {
		FragmentsValidator.requirLastFragmentInstanceof(fragments, AbstractListFragment.class);
	}
}
