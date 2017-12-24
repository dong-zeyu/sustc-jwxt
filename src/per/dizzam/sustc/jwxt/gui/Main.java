package per.dizzam.sustc.jwxt.gui;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;

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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import per.dizzam.sustc.cas.Method;
import per.dizzam.sustc.jwxt.CourseData;
import per.dizzam.sustc.jwxt.StatusException;
import per.dizzam.sustc.jwxt.gui.CourseManager.Course;

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
		FillLayout fl_group_1 = new FillLayout(SWT.HORIZONTAL);
		fl_group_1.marginHeight = 3;
		fl_group_1.marginWidth = 5;
		group_1.setLayout(fl_group_1);

		text_1 = new Text(group_1, SWT.MULTI);
		text_1.setText("总学分：0\n");
		text_1.setEnabled(false);

		Group group = new Group(sashForm, SWT.NONE);
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
					timeTableManager.searchCourse(null, true);
				} else {
					timeTableManager.searchCourse(null, false);
				}
			}
		});

		ScrolledComposite scroll = new ScrolledComposite(sashForm_p, SWT.H_SCROLL | SWT.V_SCROLL);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setMinWidth(0);

		Button button_3 = new Button(group, SWT.NONE);
		FormData fd_button_3 = new FormData();
		fd_button_3.right = new FormAttachment(button, -10);
		button_3.setLayoutData(fd_button_3);
		button_3.setText("开始选课");
		button_3.addMouseListener(new MouseAdapter() {

			private boolean isRunning = false;
			private TimerTask run = new TimerTask() {

				@Override
				public void run() {
					ArrayList<Course> courses = timeTableManager.getSelected();
					try {
						courseData.login();
						logger.info("Begin!");
						while (!courses.isEmpty() && isRunning) {
							courses.removeIf(new Predicate<Course>() {

								@Override
								public boolean test(Course t) {
									try {
										boolean result = courseData.select(t.getCategory().name(),
												t.getCourse().get("jx0404id").getAsString());
										t.setStatus(result);
										if (result) {
											Display.getDefault().syncExec(new Runnable() {
												
												@Override
												public void run() {
													String infoText = text_1.getText();
													text_1.setText(infoText + t.getCourse().get("kcmc").getAsString() + ": 选课成功");
												}
											});
										}
										return result;
									} catch (Exception e) {
										logger.error("Failed due to Execption: " + e.getMessage(), e);
									}
									return false;
								}
							});
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) { }
						}
						logger.info("Over!");
					} catch (AuthenticationException e) {
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								login(Main.this);
							}
						});
					} finally {
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								button_3.setText("开始选课");
							}
						});
						isRunning = false;
					}
				}
			};
			Thread task = new Thread(run);
			Timer timer = new Timer(true);
			
			@Override
			public void mouseDown(MouseEvent e) {
				if (isRunning) {
					button_3.setText("开始选课");
					timer.cancel();
					isRunning = false;
				} else {
					try {
						courseData.login();
						SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
						Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
						calendar.set(2017, 11, 24, 20, 22, 59);
						long shift = format.parse(courseData.dataFetcher(Method.GET, "/").getFirstHeader("Date").getValue()).getTime() - new Date().getTime();
						timer.schedule(run, new Date(calendar.getTime().getTime() - shift));
						button_3.setText("停止选课"); 
						isRunning = true;
					} catch (AuthenticationException e1) {
						login(Main.this);
					} catch (ParseException | IOException e1) {
						logger.warn(e1.getMessage(), e1);
					}
				}
			}
		});

		timeTableManager = new CourseManager(scroll, tree, text_1, courseData);
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
