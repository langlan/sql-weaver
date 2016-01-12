package langlan.sql.weaver.e;

/**
 * Indicates a development-time exception, should not happend in production environment.
 */
public class DevException extends IllegalStateException {
	private static final long serialVersionUID = 1L;

	public DevException(String msg, Exception e) {
		super(msg, e);
	}

	public DevException(String msg) {
		super(msg);
	}
}