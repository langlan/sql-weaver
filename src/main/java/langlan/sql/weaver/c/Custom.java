package langlan.sql.weaver.c;

public class Custom extends AbstractCriteria {
	public Custom(String sqlFragment, Object... vars) {
		this.expr = sqlFragment;
		this.vars = vars;
	}
}