package langlan.sql.dsl.f;

import langlan.sql.dsl.i.Fragment;
import langlan.sql.dsl.u.FragmentsValidator;

import java.util.List;

public class JoinFragment implements Fragment {
	public static final String LEFT = "Left", RIGHT = "Right", FULL = "Full", INNER = "", CROSS = "Cross";
	private String type, expr;

	public JoinFragment(String type, String expr) {
		this.type = type;
		this.expr = expr;
	}

	@Override
	public void joinFragment(StringBuilder sb, List<Object> variables) {
		if (!type.isEmpty()) {
			sb.append(type);
			sb.append(" ");
		}
		sb.append("Join");
		sb.append(" ");
		sb.append(expr);
	}

	@Override
	public void validateFragmentPosition(List<Fragment> fragments) {
		FragmentsValidator.assertExistsAndNotEmpty(fragments, FromFragment.class);
	}
}
