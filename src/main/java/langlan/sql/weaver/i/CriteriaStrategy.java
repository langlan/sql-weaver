package langlan.sql.weaver.i;

/**
 * Criteria applying and transforming strategy. This class gives a chance to modify or omit criteria(s).
 */
public interface CriteriaStrategy {
	/**
	 * Examine the criteria and return the applied criteria.
	 * 
	 * @param criteria
	 *            the {@link Criteria} to be examined.
	 * @return a criteria should be applied.
	 * <ul>
	 *     <li><b>null</b> if examined criteria should not be applied.</li>
	 *     <li><b>original</b> if examined criteria should be applied directly.</li>
	 *     <li><b>transformed</b> if examined criteria should be applied but has to be transformed first.</li>
	 * </ul>
	 */
	Criteria apply(Criteria criteria);
}
