package langlan.sql.weaver.c;

import langlan.sql.weaver.d.CriteriaGroupD;
import langlan.sql.weaver.i.Criteria;

/**
 * @param <O> the type of the owner (where this Sub-Criteria-Group came from).
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
		if (!getAppliedCriteria().isEmpty()) { //quoted if multiple items.
			String temp = super.toString();
			if (temp != null && !temp.isEmpty()) {
				return "(" + temp + ")";
			}
			return temp;
		} else {
			return super.toString();
		}
	}
}
