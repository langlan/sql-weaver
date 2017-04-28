package langlan.sql.weaver.u;

import java.util.Collection;

public class Variables {
	public static final Object[] EMPTY_ARRAY = new Object[0];

	/** return true if var is null or an empty [string|array|collection] */
	public static boolean isEmpty(Object var) {
		return var == null || (var instanceof String && ((String) var).isEmpty())
			|| (var instanceof Collection && ((Collection<?>) var).isEmpty())
			|| (var instanceof Object[] && ((Object[]) var).length == 0);

	}

	/** @see #isEmpty(Object) */
	public static boolean isNotEmpty(Object var) {
		return !isEmpty(var);
	}

	/**
	 * Simply return <code>var</code> if <code>var</code> is empty or contains '%' or fuzz-parameters are both false.
	 * Otherwise 'change' it by adding character '%' on left or right or both regarding fuzz-parameters, and return the
	 * result.
	 */
	public static String wrap4Like(String var, boolean fuzzLeft, boolean fuzzRight) {
		if (!isEmpty(var) && !var.contains("%") && (fuzzLeft || fuzzRight)) {
			if (fuzzLeft) {
				var = "%" + var;
			}
			if (fuzzRight) {
				var = var + "%";
			}
		}
		return var;
	}

	/**
	 * Returns true if the arguments are equal to each other and false otherwise. Consequently, if both arguments are
	 * null, true is returned and if exactly one argument is null, false is returned. Otherwise, equality is determined
	 * by using the equals method of the first argument.<br>
	 * <b>NOTE:Using java7 logic.</b>
	 */
	public static boolean equals(Object a, Object b) {
		return (a == b) || (a != null && a.equals(b));
	}
}
