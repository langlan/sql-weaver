package langlan.sql.weaver.u.form;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import langlan.sql.weaver.c.Between;
import langlan.sql.weaver.c.BetweenRange;
import langlan.sql.weaver.c.strategy.DefaultCriteriaStrategy;
import langlan.sql.weaver.d.CriteriaGroupD;

/**
 * Provide a Range class to use {@link CriteriaGroupD#between(String, Range)} conveniently.
 * 
 * <p> This Range class provide more information like whether <b>bound-exclusive</b>, whether <b>using
 * {@code IS NULL}</b> to help criteria-strategy to convert the {@link Between} criteria to else (e.g. =, >=, <=, >, *
 * <, > ? and < ?, ? Is Null, ..) which partially-supported by the old form <b>{@code between(prop, lower, upper)}</b>
 * method along with default-criteria-strategy.
 * 
 * <blockquote style="color:gray">
 * 
 * For the sake of BETWEEN semantic in SQL standard, criteria-strategy can not introduce bound-exclusive semantics when
 * using the old form <b>{@code between(prop, lower, upper)}</b> method, and it is hard to decide when to convert to
 * {@code IS NULL} by simply examine the two bound values, although it can simply omit when both bounds are
 * null-or-empty).
 * 
 * <p> Furthermore, if we use it by unwrapping bound values from {@code Range}/Values-Holder, we need null-check code
 * which is not fluent and elegant.
 * 
 * </blockquote>
 * 
 * @see DefaultCriteriaStrategy#applyCriteria(BetweenRange)
 * @see CriteriaGroupD#between(String, Range)
 * @see CriteriaGroupD#notBetween(String, Range)
 * @see DefaultCriteriaStrategy#applyCriteria(Between)
 * @see CriteriaGroupD#between(String, Object, Object)
 * @see CriteriaGroupD#notBetween(String, Object, Object)
 */
public class Range<T> {
	private String raw;
	private T min, max;
	private boolean minExclusive, maxExclusive;

	/**
	 * When using a single String value to convert to Range, keep it as it is. <p>
	 * 
	 * To help the criteria-strategy find out whether should replace a {@code IS NULL} criteria.
	 */
	public String getRaw() {
		return raw;
	}

	public void setRaw(String raw) {
		this.raw = raw;
	}

	public T getMin() {
		return min;
	}

	public void setMin(T min) {
		this.min = min;
	}

	public T getMax() {
		return max;
	}

	public void setMax(T max) {
		this.max = max;
	}

	public boolean isMinExclusive() {
		return minExclusive;
	}

	public void setMinExclusive(boolean minExclusive) {
		this.minExclusive = minExclusive;
	}

	public boolean isMaxExclusive() {
		return maxExclusive;
	}

	public void setMaxExclusive(boolean maxExclusive) {
		this.maxExclusive = maxExclusive;
	}

	static Pattern P = Pattern.compile("^(\\[|\\()?\\s*" // optional left-bound-indicator : inclusive or exclusive
			+ "(.*?)?" // optional min-value-part
			+ "(" + "\\s*,\\s*" // delimiter
			+ "(.*?)?" // optional max-value-part, but following delimiter if exists
			+ ")?" + "\\s*(\\]|\\))?$"); // optional right-bound-indicator : inclusive or exclusive

	/**
	 * inclusive : [1, 2] or 1, 2 | [1, ] or 1, | [, 1] or , 1<p>
	 * 
	 * exclusive: (1, 2) | (1,) | (,1) <p>
	 * 
	 * half-exclusive:
	 * 
	 * @param text
	 * @return
	 */
	public static Range<String> of(String text) {
		if (text != null) { // empty acceptable.
			text = text.trim();
			Range<String> ret = new Range<String>();
			ret.setRaw(text);

			Matcher m = P.matcher(text);
			if (m.find()) {
				ret.setMinExclusive("(".equals(m.group(1)));
				ret.setMin(m.group(2));
				if (m.group(3) != null) {
					ret.setMax(m.group(4));
				} else {
					ret.setMax(ret.getMin());
				}
				ret.setMaxExclusive(")".equals(m.group(5)));
			}

			return ret;
		}
		return null;
	}

	public static <E> Range<E> of(E min, E max) {
		return of(min, max, false, false, null);
	}

	public static <E> Range<E> of(E min, E max, boolean minExclusive, boolean maxExclusive, String raw) {
		Range<E> ret = new Range<E>();
		ret.setMin(min);
		ret.setMax(max);
		ret.setRaw(raw);
		return ret;
	}
}
