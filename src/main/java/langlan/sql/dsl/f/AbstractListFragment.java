package langlan.sql.dsl.f;

import langlan.sql.dsl.i.Fragment;
import langlan.sql.dsl.u.Variables;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractListFragment implements Fragment {
	protected List<Object> items;
	private Object[] vars = Variables.EMPTY_ARRAY;

	public AbstractListFragment() {
		this.items = new LinkedList<Object>();
	}

	public AbstractListFragment(String[] items) {
		this.items = new LinkedList<Object>(Arrays.asList(items));
	}

	public AbstractListFragment(String items, Object[] bindVariables) {
		this.items = new LinkedList<Object>();
		this.items.add(items);
		this.vars = bindVariables;
	}

	@Override
	public void join(StringBuilder sb, List<Object> variables) {
		variables.addAll(Arrays.asList(vars));
		for (Iterator<Object> iterator = items.iterator(); iterator.hasNext(); ) {
			Object item = iterator.next();
			sb.append(" ");
			if (item instanceof Fragment) {
				((Fragment) item).join(sb, variables);
			} else {
				sb.append(item);
			}
			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}
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
}
