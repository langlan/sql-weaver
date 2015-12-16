package langlan.sql.dsl.f;

import langlan.sql.dsl.i.Fragment;
import langlan.sql.dsl.i.Fragment.Ignorable;
import langlan.sql.dsl.u.Variables;

import java.util.*;

public abstract class AbstractListFragment implements Fragment, Ignorable {
	//element is String or Fragment
	protected List<Object> items = new LinkedList<Object>();
	private Object[] firstStringFragmentVars = Variables.EMPTY_ARRAY;

	public AbstractListFragment(String... items) {
		for (String item : items) {
			this.pushItem(item);
		}
	}

	public AbstractListFragment(String item, Object[] bindVariables) {
		this.pushItem(item);
		this.firstStringFragmentVars = bindVariables;
	}

	@Override
	public void joinFragment(StringBuilder sb, List<Object> variables) {
		List<Object> items = getNonEmptyItems();
		// Support ignoring empty-fragment.
		if (items.size() == 0) {
			return;
		} else if (firstStringFragmentVars.length > 0) {
			variables.addAll(Arrays.asList(firstStringFragmentVars));
		}
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
		items.add(item);
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
		name = name.replaceAll("([A-Z])", " $0").substring(1);
		return name;
	}

	public boolean hasItem() {
		return !items.isEmpty();
	}

	protected List<Object> getNonEmptyItems() {
		List<Object> nonEmptyItems = null;
		for (Object item : items) {
			if (item == null
				|| item instanceof String && ((String) item).isEmpty()
				|| (item instanceof Ignorable && ((Ignorable) item).isEmpty())) {
				continue;
			} else if (nonEmptyItems == null) {
				nonEmptyItems = new LinkedList<Object>();
			}
			nonEmptyItems.add(item);
		}
		return nonEmptyItems != null ? nonEmptyItems : Collections.emptyList();
	}

	@Override
	public boolean isEmpty() {
		return getNonEmptyItems().isEmpty();
	}
}
