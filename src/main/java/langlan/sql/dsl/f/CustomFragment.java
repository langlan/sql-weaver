package langlan.sql.dsl.f;

import langlan.sql.dsl.i.Fragment;

import java.util.Arrays;
import java.util.List;

public class CustomFragment implements Fragment {
	String fragment;
	Object[] vars;

	public CustomFragment(String fragment, Object... vars) {
		this.fragment = fragment;
		this.vars = vars;
	}

	@Override
	public void joinFragment(StringBuilder sb, List<Object> variables) {
		sb.append(fragment);
		variables.addAll(Arrays.asList(vars));
	}

	@Override
	public void validateFragmentPosition(List<Fragment> fragments) {
		
	}
}