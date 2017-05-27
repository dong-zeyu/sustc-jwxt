package jwxt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import http.CourseData;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FillLayout;

public class Main extends Shell {

	public static CourseData courseData;
	private Table table;
	private SashForm sashForm;
	private ProgressBar progressBar;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			Main shell = new Main(display);
			// Login dialog = new Login(shell, SWT.PRIMARY_MODAL);
			// courseData = new CourseData("", "");
			// do {
			// try {
			// String[] auth = (String[]) dialog.open();
			// courseData.login(auth[0], auth[1]);
			// break;
			// } catch (AuthenticationException e) {
			// }
			// } while (true);
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

	/**
	 * Create the shell.
	 * 
	 * @param display
	 */
	public Main(Display display) {
		super(display, SWT.SHELL_TRIM);
		setLayout(new FormLayout());

		Group group = new Group(this, SWT.NONE);
		FormData fd_group = new FormData();
		fd_group.left = new FormAttachment(0, 20);
		fd_group.right = new FormAttachment(100, -20);
		fd_group.bottom = new FormAttachment(0, 250);
		fd_group.top = new FormAttachment(0, 20);
		group.setLayoutData(fd_group);
		group.setLayout(new FormLayout());
		group.setText("213");

		table = new Table(group, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
		FormData fd_table = new FormData();
		fd_table.bottom = new FormAttachment(100, -7);
		fd_table.right = new FormAttachment(100, -7);
		fd_table.top = new FormAttachment(0, 25);
		fd_table.left = new FormAttachment(0, 7);
		table.setLayoutData(fd_table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(242);
		tblclmnNewColumn.setText("New Column");

		TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_1.setWidth(50);
		tblclmnNewColumn_1.setText("New Column");

		TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_2.setWidth(142);
		tblclmnNewColumn_2.setText("New Column");

		TableItem tableItem_1 = new TableItem(table, SWT.NONE);
		tableItem_1.setText(2, "HEllo");
		tableItem_1.setText("New TableItem");

		TableItem tableItem = new TableItem(table, SWT.NONE);
		tableItem.setText("New TableItem");

		progressBar = new ProgressBar(this, SWT.NONE);
		FormData fd_progressBar = new FormData();
		fd_progressBar.bottom = new FormAttachment(100, -7);
		fd_progressBar.left = new FormAttachment(group, 0, SWT.LEFT);
		progressBar.setLayoutData(fd_progressBar);

		Composite composite = new Composite(this, SWT.NONE);
		FormData fd_composite = new FormData();
		fd_composite.bottom = new FormAttachment(progressBar, -7);
		fd_composite.top = new FormAttachment(group, 7);
		fd_composite.left = new FormAttachment(group, 0, SWT.LEFT);
		fd_composite.right = new FormAttachment(group, 0, SWT.RIGHT);
		composite.setLayoutData(fd_composite);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));

		sashForm = new SashForm(composite, SWT.VERTICAL);

		SashForm sashForm_1 = new SashForm(sashForm, SWT.BORDER);
		sashForm_1.setEnabled(false);

		Composite composite_1 = new Composite(sashForm_1, SWT.NONE);
		composite_1.setLayout(null);

		Label lblMon = new Label(composite_1, SWT.CENTER);
		lblMon.setBounds(0, 0, 105, 19);
		lblMon.setText("Mon");

		Label label = new Label(composite_1, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setBounds(0, 19, 105, 2);

		Label lblTue = new Label(sashForm_1, SWT.CENTER);
		lblTue.setText("Tue");

		Label lblWed = new Label(sashForm_1, SWT.CENTER);
		lblWed.setText("Wed");

		Label lblThu = new Label(sashForm_1, SWT.CENTER);
		lblThu.setText("Thu");

		Label lblFri = new Label(sashForm_1, SWT.CENTER);
		lblFri.setText("Fri");

		Label lblSat = new Label(sashForm_1, SWT.CENTER);
		lblSat.setText("Sat");

		Label lblSun = new Label(sashForm_1, SWT.CENTER);
		lblSun.setText("Sun");
		sashForm_1.setWeights(new int[] { 1, 1, 1, 1, 1, 1, 1 });
		sashForm.setWeights(new int[] { 20 });
		createContents();
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("App");
		setSize(839, 700);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
