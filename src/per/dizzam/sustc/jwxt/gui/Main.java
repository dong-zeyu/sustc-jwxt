package per.dizzam.sustc.jwxt.gui;

import java.io.IOException;

import org.apache.http.auth.AuthenticationException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import per.dizzam.sustc.jwxt.CourseData;
import per.dizzam.sustc.jwxt.StatusException;

public class Main extends Shell {

	private static Logger logger = Logger.getLogger("Main");

	public static CourseData courseData;
	private Text text;

	private TimeTableManager timeTableManager;

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

		Group group = new Group(sashForm_p, SWT.NONE);
		group.setLayout(new FormLayout());
		group.setText("课程");

		Button button = new Button(group, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
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
				timeTableManager.updateData(null);
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
				timeTableManager.updateData(text.getText().equals("") ? null : text.getText());
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
				timeTableManager.updateData(text.getText().equals("") ? null : text.getText());
			}
		});
		FormData fd_button_1 = new FormData();
		fd_button_1.left = new FormAttachment(0, 86);
		fd_button_1.top = new FormAttachment(button, 0, SWT.TOP);
		button_1.setLayoutData(fd_button_1);
		button_1.setText("搜索");

		timeTableManager = new TimeTableManager(sashForm_p, group, courseData);

		sashForm_p.setWeights(new int[] { 6, 13 });

	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("App");
		setMaximized(true);

		timeTableManager.updateData(null);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
