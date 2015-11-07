package langlan.sql.dsl.f;

import langlan.sql.dsl.i.Fragment;
import langlan.sql.dsl.u.Variables;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractListFragment implements Fragment {
	//element is String or Fragment
	protected List<Object> items = new LinkedList<Object>();
	private Object[] vars = Variables.EMPTY_ARRAY;

	public AbstractListFragment(String... items) {
		for (String item : items) {
			this.pushItem(item);
		}
	}

	public AbstractListFragment(String items, Object[] bindVariables) {
		this.pushItem(items);
		this.vars = bindVariables;
	}

	@Override
	public void joinFragment(StringBuilder sb, List<Object> variables) {
		variables.addAll(Arrays.asList(vars));
		sb.append(getName());
		for (Iterator<Object> iterator = items.iterator(); iterator.hasNext(); ) {
			Object item = iterator.next();
			sb.append(" ");
			if (item instanceof Fragment) {
				((Fragment) item).joinFragment(sb, variables);
			} else {
				sb.append(item);
			}
			if (iterator.hasNext()) {
				sb.append(",");
			}
		}
	}

	// Do not make public
	protected void pushItem(String item) {
		//if (item != null && !item.trim().isEmpty()) {
		items.add(item);
		//}
	}

	public void pushItem(Fragment item) {
		items.add(item);
	}

	public Object peekItem() {
		return items.size() == 0 ? null : items.get(items.size() - 1);
	}

	public Object popItem() {
		return items.remove(items.size() - 1);
	}

	public String getName() {
		String name = getClass().getSimpleName().replace("Fragment", "");
		name = name.replaceAll("[a-z][A-Z]", " $0");
		return name;
	}

	public boolean hasItem() {
		return !items.isEmpty();
	}
}
