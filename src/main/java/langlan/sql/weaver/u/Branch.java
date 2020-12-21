package langlan.sql.weaver.u;

import langlan.sql.weaver.e.BranchStateException;

/** legacy design: unused */
public class Branch {
	private static final int INIT_OR_END = 0, IF = 1, ELSE_IF = 2, ELSE = 3;
	private int status;
	private Branch nested;
	private boolean entered, alreadyEntered;

	public boolean isEntered() {
		return this.isCompleted() || this.entered && (nested == null || nested.isEntered());
	}

	public boolean isCompleted() {
		return status == INIT_OR_END;
	}

	public void doIf(boolean b) {
		if (isCompleted()) {
			this.status = IF;
			this.entered = b;
			this.alreadyEntered = b;
		} else {
			if (nested == null) {
				nested = new Branch();
			}
			nested.doIf(b);
		}
	}

	public void doEndIf() {
		if (isCompleted()) {
			throw new BranchStateException("No [if] matched!");
		} else if (nested == null || nested.isCompleted()) {
			this.status = INIT_OR_END;
			this.entered = false;
			this.alreadyEntered = false;
		} else {
			nested.doEndIf();
		}
	}

	/**
	 * @throws BranchStateException
	 *             if has previous [else]
	 */
	public void doElse() {
		if (isCompleted()) {
			throw new BranchStateException("No [if] matched!");
		} else if (nested == null || nested.isCompleted()) {
			if (this.status == ELSE) {
				throw new BranchStateException("Not allowed [else] after [else]!");
			}
			this.status = ELSE;
			this.entered = !alreadyEntered;
			this.alreadyEntered = true;
		} else {
			nested.doElse();
		}
	}

	/**
	 * @throws BranchStateException
	 *             if has previous [else]
	 */
	public void doElseIf(boolean b) throws BranchStateException {
		if (isCompleted()) {
			throw new BranchStateException("No [if] matched!");
		} else if (nested == null || nested.isCompleted()) {
			if (this.status == ELSE) {
				throw new BranchStateException("Not allowed [elseif] after [else]!");
			}
			this.status = ELSE_IF;
			this.entered = b && !alreadyEntered;
			this.alreadyEntered = b || alreadyEntered;
		} else {
			nested.doElseIf(b);
		}
	}

	/**
	 * @throws BranchStateException
	 *             if incomplete
	 */
	public void validate() {
		if (!this.isCompleted()) {
			throw new BranchStateException("[if] not closed by [endIf]");
		}
	}
}