package langlan.sql.dsl.u;

import java.util.Collection;

public class Variables {
	public static final Object[] EMPTY_ARRAY = new Object[0];

	/** return true if obj is null or an empty [string|array|collection] */
	public static boolean isEmpty(Object obj) {
		return obj == null || (obj instanceof String && ((String) obj).length() == 0)
			|| (obj instanceof Collection && ((Collection<?>) obj).isEmpty())
			|| (obj instanceof Object[] && ((Object[]) obj).length == 0);

	}

	/** @see #isEmpty(Object)  */
	public static boolean isNotEmpty(Object obj){
		return !isEmpty(obj);
	}
}
