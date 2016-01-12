package langlan.sql.weaver.c;

public class Custom extends AbstractCriteria {
	public Custom(String sqlFrament, Object... vars) {
		this.expr = sqlFrament;
		this.vars = vars;
	}
}