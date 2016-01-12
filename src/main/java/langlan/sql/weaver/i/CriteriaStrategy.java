package langlan.sql.weaver.i;

/**
 * Criteria applying and transforming strategy
 */
public interface CriteriaStrategy {
	/**
	 * Test the criteria and return the applied criteria.
	 * 
	 * @param criteria
	 *            the {@link Criteria} to be examined.
	 * @return Applied criteria. <b>null</b> if examined criteria should not be applied.<br/>
	 *         <b>NOTE:</b> the returned criteria may be transformed from the original examined criteria.
	 */
	Criteria apply(Criteria criteria);
}
