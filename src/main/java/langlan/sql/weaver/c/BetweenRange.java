package langlan.sql.weaver.c;

import langlan.sql.weaver.u.form.Range;

public class BetweenRange extends Between {
	Range<?> range;

	public BetweenRange(String testing, Range<?> range) {
		super(testing, range != null ? range.getMin() : null, range != null ? range.getMax() : null, false);
		this.range = range;
		calcExpression();
	}

	public Range<?> getRange() {
		return range;
	}

	@Override
	protected void calcExpression() {
		if(!range.isMinExclusive() && !range.isMaxExclusive()) {
			super.calcExpression();
			return;
		}
		// if any side exclusive
		StringBuilder sb = new StringBuilder();
		if(!isNegative()) {
			sb.append(getTesting());
			sb.append(range.isMinExclusive() ? ">?" : ">=?");
			sb.append(" And ");
			sb.append(getTesting());
			sb.append((range.isMaxExclusive() ? "<?" : "<=?"));
		}else {
			sb.append("(");
			sb.append(getTesting());
			sb.append(range.isMinExclusive() ? "<=?" : "<?");
			sb.append(" Or ");
			sb.append(getTesting());
			sb.append((range.isMaxExclusive() ? ">=?" : ">?"));
			sb.append(")");
		}
		this.expr = sb.toString();
	}
}