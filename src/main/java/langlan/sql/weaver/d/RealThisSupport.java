package langlan.sql.weaver.d;

public abstract class RealThisSupport<T extends RealThisSupport<T>> {
	protected T realThis() {
		@SuppressWarnings("unchecked")
		T t = (T) this;
		return t;
	}
}
