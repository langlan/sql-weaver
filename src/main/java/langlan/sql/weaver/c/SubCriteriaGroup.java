package langlan.sql.weaver.c;

import langlan.sql.weaver.d.CriteriaGroupD;
import langlan.sql.weaver.i.Criteria;

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
		if (getAppliedCriteria().isEmpty()) {
			return "";
		} else if (getAppliedCriteria().size() > 1) {
			return "(" + super.toString() + ")";
		} else {
			return super.toString();
		}
	}
}
