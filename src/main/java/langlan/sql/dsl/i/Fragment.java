package langlan.sql.dsl.i;

import java.util.List;

/** Sql Fragment */
public interface Fragment {
	void join(StringBuilder sb, List<Object> variables);
	void validate(List<Fragment> fragments);
}
