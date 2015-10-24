package langlan.sql.dsl.criteria;

import langlan.sql.dsl.d.CriteriaGroupD;
import langlan.sql.dsl.i.Criteria;

/**
 * 
 * @author langlan
 * 
 * @param <O>
 *            The owner (where came from to this Sub-Criteria-Group scope).
 */
public class SubCriteriaGroup<O extends CriteriaGroupD<?, ?>> extends CriteriaGroupD<SubCriteriaGroup<O>, O> implements
	Criteria {

	/** Use for nested/sub group */
	public SubCriteriaGroup(O owner, boolean orMode) {
		super(owner, orMode);
	}

	/** Terminate this nested/sub-group scope */
	public O endGrp() {
		return end();
	}

	@Override
	public String toString() {
		if (getAppliedCriterias().isEmpty()) {
			return "";
		} else if (size() > 1) {
			return "(" + super.toString() + ")";
		} else {
			return super.toString();
		}
	}
}
