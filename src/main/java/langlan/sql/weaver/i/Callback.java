package langlan.sql.weaver.i;

public interface Callback<T> {
	void call(T target);
}
