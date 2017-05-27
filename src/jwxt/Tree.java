package jwxt;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

public class Tree extends org.eclipse.swt.widgets.Tree {

	public Tree(Composite parent, int style) {
		super(parent, style);
		addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.item instanceof TreeItem) {
					TreeItem item = (TreeItem) e.item;
					item.setChecked(item.getChecked());
				}
			}
		});
	}

	@Override
	public TreeItem[] getItems() {
		org.eclipse.swt.widgets.TreeItem[] items0 =  super.getItems();
		TreeItem[] items = new TreeItem[items0.length];
		for (int i = 0; i < items.length; i++) {
			items[i] = (TreeItem) items0[i];
		}
		return items;
	}

	@Override
	protected void checkSubclass() {
	}
}
