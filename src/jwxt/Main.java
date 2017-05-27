package jwxt;

import java.util.Map.Entry;

import org.apache.http.auth.AuthenticationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import http.CourseData;
import http.StatusException;

import org.eclipse.swt.widgets.Group;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class Main extends Shell {

	public static CourseData courseData;
	private SashForm sashForm;
	private Tree tree;
	private static String[] WEEK = new String[] { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
	private Text text;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			courseData = new CourseData("", "");
			Display display = Display.getDefault();
			Main shell = new Main(display);
			login(shell);
			shell.createContents();
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean login(Shell shell) {
		Login dialog = new Login(shell, SWT.PRIMARY_MODAL);
		do {
			try {
				Object o = dialog.open();
				if (o != null) {
					String[] auth = (String[]) o;
					courseData.login(auth[0], auth[1]);
					break;
				}
				return false;
			} catch (AuthenticationException e) {
			}
		} while (true);
		return true;
	}

	/**
	 * Create the shell.
	 * 
	 * @param display
	 */
	public Main(Display display) {
		super(display, SWT.SHELL_TRIM);
		setSize(900, 700);
		setLayout(new FormLayout());

		Group group = new Group(this, SWT.NONE);
		FormData fd_group = new FormData();
		fd_group.left = new FormAttachment(0, 20);
		fd_group.right = new FormAttachment(40, 0);
		fd_group.bottom = new FormAttachment(100, -20);
		fd_group.top = new FormAttachment(0, 15);
		group.setLayoutData(fd_group);
		group.setLayout(new FormLayout());
		group.setText("课程");

		tree = new Tree(group, SWT.BORDER | SWT.CHECK);
		FormData fd_tree = new FormData();
		fd_tree.bottom = new FormAttachment(100, -7);
		fd_tree.right = new FormAttachment(100, -7);
		fd_tree.top = new FormAttachment(0, 30);
		fd_tree.left = new FormAttachment(0, 7);
		tree.setLayoutData(fd_tree);
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);

		TreeColumn trclmnA = new TreeColumn(tree, SWT.NONE);
		trclmnA.setWidth(275);
		trclmnA.setText("课程名称");

		TreeColumn treeColumn = new TreeColumn(tree, SWT.NONE);
		treeColumn.setWidth(36);
		treeColumn.setText("学分");

		TreeColumn trlclmn_ls = new TreeColumn(tree, SWT.NONE);
		trlclmn_ls.setWidth(60);
		trlclmn_ls.setText("老师");

		TreeColumn trlclmn_pgtj = new TreeColumn(tree, SWT.NONE);
		trlclmn_pgtj.setWidth(275);
		trlclmn_pgtj.setText("先修课程");

		sashForm = new SashForm(this, SWT.VERTICAL);
		FormData fd_sashForm = new FormData();
		fd_sashForm.bottom = new FormAttachment(group, 0, SWT.BOTTOM);
		fd_sashForm.top = new FormAttachment(group, 0, SWT.TOP);
		fd_sashForm.left = new FormAttachment(group, 7, SWT.RIGHT);

		Button button = new Button(group, SWT.NONE);
		Shell shell = this;
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				do {
					try {
						courseData.updateCourseData();
						courseData.updateSelected();
						break;
					} catch (AuthenticationException e1) {
						if (!login(shell)) {
							break;
						}
					} catch (StatusException e1) {
						MessageBox messageBox = new MessageBox(shell, SWT.OK);
						messageBox.setMessage(e1.getMessage());
						messageBox.open();
						break;
					}
				} while (true);
				updateData(null);
			}
		});
		FormData fd_button = new FormData();
		fd_button.right = new FormAttachment(tree, 0, SWT.RIGHT);
		button.setLayoutData(fd_button);
		button.setText("更新数据");

		text = new Text(group, SWT.BORDER);
		FormData fd_text = new FormData();
		fd_text.top = new FormAttachment(button, 2, SWT.TOP);
		fd_text.left = new FormAttachment(tree, 0, SWT.LEFT);
		text.setLayoutData(fd_text);

		Button button_1 = new Button(group, SWT.NONE);
		button_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				updateData(text.getText());
			}
		});
		FormData fd_button_1 = new FormData();
		fd_button_1.left = new FormAttachment(0, 86);
		fd_button_1.top = new FormAttachment(button, 0, SWT.TOP);
		button_1.setLayoutData(fd_button_1);
		button_1.setText("搜索");
		fd_sashForm.right = new FormAttachment(100, -7);
		sashForm.setLayoutData(fd_sashForm);

		SashForm sashForm_1 = new SashForm(sashForm, SWT.BORDER);
		sashForm_1.setEnabled(false);

		Composite composite = new Composite(sashForm_1, SWT.NONE);
		composite.setLayout(new FormLayout());

		Label lblNewLabel = new Label(composite, SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.top = new FormAttachment(0);
		fd_lblNewLabel.left = new FormAttachment(0, 5);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setAlignment(SWT.CENTER);
		lblNewLabel.setText("1");

		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		FormData fd_lblNewLabel_1 = new FormData();
		fd_lblNewLabel_1.top = new FormAttachment(0, 17);
		fd_lblNewLabel_1.left = new FormAttachment(0, 5);
		lblNewLabel_1.setLayoutData(fd_lblNewLabel_1);
		lblNewLabel_1.setText("New Label");

		for (String string : WEEK) {
			Composite composite_1 = new Composite(sashForm_1, SWT.NONE);
			composite_1.setLayout(new FormLayout());

			Label lbl = new Label(composite_1, SWT.CENTER);
			FormData fd = new FormData();
			fd.top = new FormAttachment(0, 0);
			fd.bottom = new FormAttachment(0, 22);
			fd.left = new FormAttachment(0, 0);
			fd.right = new FormAttachment(100, 0);
			lbl.setLayoutData(fd);
			lbl.setText(string);

			Label line = new Label(composite_1, SWT.SEPARATOR | SWT.HORIZONTAL);

			FormData fd_line = new FormData();
			fd_line.left = new FormAttachment(0, 0);
			fd_line.right = new FormAttachment(100, 0);
			fd_line.top = new FormAttachment(lbl, 0, SWT.BOTTOM);
			line.setLayoutData(fd_line);
		}

		sashForm_1.setWeights(new int[] { 2, 9, 9, 9, 9, 9, 9, 9 });

		sashForm.setWeights(new int[] { 20 });
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("App");
		setMaximized(true);

		updateData(null);
	}

	private void updateData(String string) {
		tree.removeAll();
		JsonObject result;
		if (string == null) {
			result = courseData.getCourse();
		} else {
			result = courseData.search(string);
		}
		for (Entry<String, JsonElement> entry : result.entrySet()) {
			for (JsonElement element : entry.getValue().getAsJsonArray()) {
				TreeItem root = null;
				for (TreeItem item : tree.getItems()) {
					if (item.getText().equals(entry.getKey())) {
						root = item;
					}
				}
				if (root == null) {
					TreeItem newItem = new TreeItem(tree, SWT.CHECK);
					newItem.setText(entry.getKey());
					root = newItem;
				}
				TreeItem parent = null;
				for (TreeItem item : root.getItems()) {
					if (item.getText().equals(element.getAsJsonObject().get("kcmc").getAsString())) {
						parent = item;
					}
				}
				if (parent == null) {
					TreeItem newItem = new TreeItem(root, SWT.NONE);
					newItem.setText(0, element.getAsJsonObject().get("kcmc").getAsString());
					newItem.setText(1, String.valueOf(element.getAsJsonObject().get("xf").getAsInt()));
					JsonElement e = element.getAsJsonObject().get("pgtj");
					newItem.setText(3, e.isJsonNull() ? "无" : e.getAsString());
					parent = newItem;
				}
				TreeItem item = new TreeItem(parent, SWT.NONE);
				JsonElement e1 = element.getAsJsonObject().get("fzmc");
				item.setText(0, element.getAsJsonObject().get("kcmc").getAsString()
						+ (e1.isJsonNull() ? "" : "[" + e1.getAsString() + "]"));
				item.setText(1, String.valueOf(element.getAsJsonObject().get("xf").getAsInt()));
				JsonElement e2 = element.getAsJsonObject().get("skls");
				item.setText(2, e2.isJsonNull() ? "无" : e2.getAsString());
				JsonElement e3 = element.getAsJsonObject().get("pgtj");
				item.setText(3, e3.isJsonNull() ? "无" : e3.getAsString());
			}
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
