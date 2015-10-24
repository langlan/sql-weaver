package langlan.sql.dsl.f;

import java.util.List;

import langlan.sql.dsl.i.Fragment;
import langlan.sql.dsl.u.FragmentsValidator;

public class JoinFragment implements Fragment {
	public static final String LEFT = "Left", RIGHT = "Right", FULL = "Full", INNER = "", CROSS = "Cross";
	private String type, expr;

	public JoinFragment(String type, String expr) {
		this.type = type;
		this.expr = expr;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public void join(StringBuilder sb, List<Object> variables) {
		sb.append(type);
		sb.append(" ");
		sb.append("Join");
		sb.append(" ");
		sb.append(expr);
	}

	@Override
	public void validate(List<Fragment> fragments) {
		FragmentsValidator.require(fragments, FromFragment.class);
	}
}
