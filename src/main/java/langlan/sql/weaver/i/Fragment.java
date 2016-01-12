package langlan.sql.weaver.i;

import java.util.List;

/** Sql Fragment */
public interface Fragment {
	interface Ignorable{
		boolean isEmpty();
	}
	void joinFragment(StringBuilder sb, List<Object> variables);
	void validateFragmentPosition(List<Fragment> fragments);
}
