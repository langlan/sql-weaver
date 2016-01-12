package langlan.sql.weaver.u;

import langlan.sql.weaver.e.SqlSyntaxException;
import langlan.sql.weaver.f.AbstractListFragment;
import langlan.sql.weaver.i.Fragment;

import java.util.List;

public class FragmentsValidator {
	public static void assertLastFragmentInstanceof(List<Fragment> fragments, Class<? extends Fragment> type) {
		if (fragments.size() == 0) {
			throw new SqlSyntaxException("None fragment exists! expect instance of [" + type.getSimpleName() + "]!");
		} else if (!type.isInstance(fragments.get(fragments.size() - 1))) {
			throw new SqlSyntaxException("Expecting previous fragment instance of [" + type.getSimpleName() + "]!");
		}
	}

	public static void assertExistsAndNotEmpty(List<Fragment> fragments, Class<? extends Fragment> type) {
		Fragment f = getAny(fragments, type);
		if (f == null) {
			throw new SqlSyntaxException("Clause '" + type.getName().replace("Fragment", "") + "' is missing!");
		} else if (f instanceof AbstractListFragment) {
			AbstractListFragment lf = (AbstractListFragment) f;
			if (!lf.hasItem()) {
				throw new SqlSyntaxException("Empty [" + lf.getName() + "] Clause");
			}
		}
	}

	public static void assertNotExists(List<Fragment> fragments, Class<? extends Fragment> type) {
		if (hasAny(fragments, type)) {
			throw new SqlSyntaxException("Multiple Clause '" + type.getName().replace("Fragment", "") + "'");
		}
	}

	public static <T extends Fragment> T getAny(List<Fragment> fragments, Class<T> clazz) {
		for (Fragment f : fragments) {
			if (clazz.isInstance(f)) {
				return clazz.cast(f);
			}
		}
		return null;
	}

	public static boolean hasAny(List<Fragment> fragments, Class<? extends Fragment> clazz) {
		return getAny(fragments, clazz) != null;
	}
}
