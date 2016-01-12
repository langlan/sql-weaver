package langlan.sql.weaver.d;

import langlan.sql.weaver.u.Branch;

public abstract class BranchSupportD<T extends BranchSupportD<T>> extends InlineStrategySupport<T> {
	private Branch branch;

	public T $$else() {
		getBranch().doElse();
		return realThis();
	}

	public T $$elseIf(boolean enter) {
		getBranch().doElseIf(enter);
		return realThis();
	}

	public T $$endIf() {
		getBranch().doEndIf();
		return realThis();
	}

	public T $$if(boolean enter) {
		getBranch().doIf(enter);
		return realThis();
	}

	public Branch getBranch() {
		if (branch == null) {
			branch = new Branch();
		}
		return branch;
	}
}
