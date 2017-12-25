package per.dizzam.sustc.jwxt.gui;

import java.io.IOException;

import org.apache.http.auth.AuthenticationException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import per.dizzam.sustc.jwxt.CourseData;
import per.dizzam.sustc.jwxt.StatusException;

public class Main extends Shell {

	private static Logger logger = Logger.getLogger("Main");

	public static CourseData courseData;
	private CourseManager timeTableManager;

	private Text text;
	private Button button_2;
	private Text text_1;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			try {
				PropertyConfigurator.configure(Main.class.getResourceAsStream("/log4j.properties"));
			} catch (NullPointerException e) {
				PropertyConfigurator.configure("log4j.properties");
			}
			courseData = new CourseData();
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
			shell.timeTableManager.save();
			courseData.saveToFile();
		} catch (Exception e) {
			logger.fatal(e.getMessage(), e);
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
				MessageBox messageBox = new MessageBox(shell, SWT.OK);
				messageBox.setMessage(e.getMessage());
				messageBox.open();
				if (e.getCause() instanceof IOException) {
					return false;
				}
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

		SashForm sashForm_p = new SashForm(this, SWT.NONE);
		FormData fd_sashForm_p = new FormData();
		fd_sashForm_p.bottom = new FormAttachment(100, -20);
		fd_sashForm_p.top = new FormAttachment(0, 15);
		fd_sashForm_p.right = new FormAttachment(100, -20);
		fd_sashForm_p.left = new FormAttachment(0, 20);
		sashForm_p.setLayoutData(fd_sashForm_p);

		SashForm sashForm = new SashForm(sashForm_p, SWT.VERTICAL);

		Group group_1 = new Group(sashForm, SWT.NONE);
		group_1.setText("信息");
		group_1.setLayout(new FormLayout());

		Group group = new Group(sashForm, SWT.NONE);
		group.setLayout(new FormLayout());
		group.setText("课程");

		Button button = new Button(group, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				timeTableManager.save();
				do {
					try {
						courseData.updateCourseData();
						courseData.updateSelected();
						break;
					} catch (AuthenticationException e1) {
						if (!login(Main.this)) {
							break;
						}
					} catch (StatusException e1) {
						MessageBox messageBox = new MessageBox(Main.this, SWT.OK);
						messageBox.setMessage(e1.getMessage());
						messageBox.open();
						break;
					}
				} while (true);
				timeTableManager.updateData();
			}
		});

		FormData fd_button = new FormData();
		fd_button.right = new FormAttachment(100, -7);
		button.setLayoutData(fd_button);
		button.setText("更新数据");

		text = new Text(group, SWT.BORDER);
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR || e.character == SWT.LF) {
					timeTableManager.searchCourse(text.getText().equals("") ? null : text.getText(),
							button_2.getSelection());
				}
			}
		});
		FormData fd_text = new FormData();
		fd_text.top = new FormAttachment(button, 2, SWT.TOP);
		fd_text.left = new FormAttachment(0, 7);
		text.setLayoutData(fd_text);

		Button button_1 = new Button(group, SWT.NONE);
		button_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				timeTableManager.searchCourse(text.getText().equals("") ? null : text.getText(),
						button_2.getSelection());
			}
		});
		FormData fd_button_1 = new FormData();
		fd_button_1.left = new FormAttachment(0, 86);
		fd_button_1.top = new FormAttachment(button, 0, SWT.TOP);
		button_1.setLayoutData(fd_button_1);
		button_1.setText("搜索");

		Tree tree = new Tree(group, SWT.BORDER | SWT.CHECK);
		FormData fd_tree = new FormData();
		fd_tree.bottom = new FormAttachment(100, -7);
		fd_tree.right = new FormAttachment(100, -7);
		fd_tree.top = new FormAttachment(0, 30);
		fd_tree.left = new FormAttachment(0, 7);
		tree.setLayoutData(fd_tree);
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);

		button_2 = new Button(group, SWT.CHECK);
		FormData fd_button_2 = new FormData();
		fd_button_2.top = new FormAttachment(button_1, 0, SWT.CENTER);
		fd_button_2.left = new FormAttachment(button_1, 10);
		button_2.setLayoutData(fd_button_2);
		button_2.setText("仅显示已选择");
		button_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (button_2.getSelection()) {
					timeTableManager.searchCourse(text.getText().equals("") ? null : text.getText(), true);
				} else {
					timeTableManager.searchCourse(text.getText().equals("") ? null : text.getText(), false);
				}
			}
		});

		ScrolledComposite scroll = new ScrolledComposite(sashForm_p, SWT.H_SCROLL | SWT.V_SCROLL);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setMinWidth(0);

		Label lblNewLabel = new Label(group_1, SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.right = new FormAttachment(100, -3);
		fd_lblNewLabel.left = new FormAttachment(0, 3);
		fd_lblNewLabel.top = new FormAttachment(0, 3);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("总学分：0");

		text_1 = new Text(group_1, SWT.READ_ONLY | SWT.MULTI);
		FormData fd_text_1 = new FormData();
		fd_text_1.top = new FormAttachment(lblNewLabel, 3);
		fd_text_1.right = new FormAttachment(100);
		fd_text_1.bottom = new FormAttachment(100);
		fd_text_1.left = new FormAttachment(0);
		text_1.setLayoutData(fd_text_1);

		timeTableManager = new CourseManager(scroll, tree, group_1, courseData);
		sashForm_p.setWeights(new int[] { 7, 13 });
		sashForm.setWeights(new int[] { 1, 4 });
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("课表");
		setMaximized(true);

		timeTableManager.updateData();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
