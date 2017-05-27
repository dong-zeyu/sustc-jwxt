package jwxt;

public class TreeItem extends org.eclipse.swt.widgets.TreeItem {

	public TreeItem(Tree parent, int style) {
		super(parent, style);
	}

	public TreeItem(TreeItem parent, int style) {
		super(parent, style);
	}

	@Override
	protected void checkSubclass() {
	}

	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		for (TreeItem item : getItems()) {
			item.setChecked(checked);
		}
		TreeItem parent;
		TreeItem head = this;
		while ((parent = (TreeItem) head.getParentItem()) != null) {
			if (checked) {
				parent.superSetChecked(true);
				parent.setGrayed(false);
				for (TreeItem item : parent.getItems()) {
					if (!item.getChecked() || item.getGrayed()) {
						parent.setGrayed(true);
						break;
					}
				}
			} else {
				parent.setGrayed(true);
				parent.superSetChecked(false);
				for (TreeItem item : parent.getItems()) {
					if (item.getChecked()) {
						parent.superSetChecked(true);
						break;
					}
				}
			}
			head = parent;
		}
	}
	
	private void superSetChecked(boolean isChecked) {
		super.setChecked(isChecked);
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
}
