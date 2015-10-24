package langlan.sql.dsl.u;

import java.util.List;

import langlan.sql.dsl.e.SqlSyntaxException;
import langlan.sql.dsl.i.Fragment;

public class FragmentsValidator {
	public static void requirLastFragmentInstanceof(List<Fragment> fragments, Class<? extends Fragment> type) {
		if (fragments.size() == 0) {
			throw new SqlSyntaxException("None fragment exists!");
		} else if (type.isInstance(fragments.get(fragments.size() - 1))) {
			throw new SqlSyntaxException("Expecting previous fragment instance of [" + type.getClass() + "]!");
		}
	}

	public static void require(List<Fragment> fragments, Class<? extends Fragment> type) {
		if (!hasFragment(fragments, type)) {
			throw new SqlSyntaxException("Clause '" + type.getName().replace("Fragment", "") + "' is missing!");
		}
	}

	public static void requireNotExists(List<Fragment> fragments, Class<? extends Fragment> type) {
		if (hasFragment(fragments, type)) {
			throw new SqlSyntaxException("Multiple Clause '" + type.getName().replace("Fragment", "") + "'");
		}
	}

	public static boolean hasFragment(List<Fragment> fragments, Class<? extends Fragment> clazz) {
		for (Fragment f : fragments) {
			if (clazz.isInstance(f)) {
				return true;
			}
		}
		return false;
	}
}
