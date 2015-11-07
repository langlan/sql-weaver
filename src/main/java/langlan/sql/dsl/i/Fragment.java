package langlan.sql.dsl.i;

import java.util.List;

/** Sql Fragment */
public interface Fragment {
	void joinFragment(StringBuilder sb, List<Object> variables);
	void validateFragmentPosition(List<Fragment> fragments);
}
