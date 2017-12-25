package per.dizzam.sustc.jwxt.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Stack;

import org.apache.http.auth.AuthenticationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import per.dizzam.sustc.jwxt.CourseData;
import per.dizzam.sustc.jwxt.CourseRepo;

public class CourseManager {
	
	private static final String[] WEEK = new String[] { "", "周一", "周二", "周三", "周四", "周五", "周六", "周日" };
	private static final Font NORMAL_TREE = new Font(Display.getDefault(), "华文黑体", 12, SWT.NORMAL);
	private static final Font BOLD_TREE = new Font(Display.getDefault(), "华文黑体", 12, SWT.BOLD);
	private static final Font NORMAL = new Font(Display.getDefault(), "华文黑体", 12, SWT.NORMAL);
	private static final Font ITALIC = new Font(Display.getDefault(), "华文黑体", 12, SWT.ITALIC);
	
	class Course {
		
		private static final int LABLE_WIDTH = 19;
		private ArrayList<Label> labels;
		private JsonObject course;
		private float hue = 0;
		private boolean isSelected = false;
		private boolean isChecked = false;
		private boolean status = false;
		private TreeItem item;
		private CourseRepo category;
		
		public Course(JsonObject course, CourseRepo category) {
			labels = new ArrayList<>();
			this.course = course;
			this.category = category;
		}
		
		public JsonObject getCourse() {
			return course;
		}
		
		public CourseRepo getCategory() {
			return category;
		}
		
		public boolean getStatus() {
			return status;
		}
		
		public void setStatus(boolean status) {
			this.status = status;
		}
		
		public void layoutLable() {
			if (!labels.isEmpty()) {
				return;
			}
			hue = picker.getCurrentHue();
			JsonArray times = course.get("kkapList").getAsJsonArray();
			Color color = picker.changeLighten(hue, isSelected);
			for (JsonElement time : times) {
				JsonObject t = time.getAsJsonObject();
				int week = t.get("xq").getAsInt();
				int from = Integer.valueOf(t.get("skjcmc").getAsString().split("-")[0]);
				int to = Integer.valueOf(t.get("skjcmc").getAsString().split("-")[1]);
				Composite composite = weekList.get(week);
				FormData fd_l = new FormData();
				Label label = new Label(composite, SWT.WRAP);
				label.setAlignment(SWT.CENTER);
				String mc;
				if (t.get("kkzc").getAsString().contains("单")) {
					mc = "单  " + course.get("kcmc").getAsString();
					label.setFont(ITALIC);
				} else if (t.get("kkzc").getAsString().contains("双")) {
					mc = "双  " + course.get("kcmc").getAsString();
					label.setFont(ITALIC);
				} else {
					mc = course.get("kcmc").getAsString();
					label.setFont(NORMAL);
				}
				label.setText(mc);
				label.setData(this);
				label.setBackground(color);
				fd_l.top = new FormAttachment((from - 1) * 10, 2);
				fd_l.bottom = new FormAttachment(to * 10, -1);
				int left = 0;
				for (left = 0; ; left+=LABLE_WIDTH) {
					boolean available = true;
					for (Control control : composite.getChildren()) {
						if (control.getData() != null && control.getData() instanceof Course && !control.equals(label)) {
							FormData data = (FormData) control.getLayoutData();
							if (data.top.numerator < fd_l.bottom.numerator && data.bottom.numerator > fd_l.top.numerator
									&& data.left.offset == left) {
								available = false;
							}
						}
					}
					if (available) {
						break;
					}
				}
				fd_l.left = new FormAttachment(0, left);
				fd_l.right = new FormAttachment(0, left + LABLE_WIDTH);
				label.setLayoutData(fd_l);
				label.moveAbove(null);
				label.requestLayout();
				label.addMouseTrackListener(new MouseTrackAdapter() {

					@Override
					public void mouseEnter(MouseEvent e) {
						if (!isSelected) {
							lightenLable(true);
						}
						for (Control control : info.getChildren()) {
							if (control instanceof Text) {
								Text text = (Text) control;
								text.setText(Course.this.toString());								
							}
						}
					}

					@Override
					public void mouseExit(MouseEvent e) {
						if (!isSelected) {
							lightenLable(false);
						}
					}
				});
				label.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseDoubleClick(MouseEvent e) {
						MessageBox box = new MessageBox(scroll.getShell(), SWT.OK | SWT.CANCEL);
						box.setText("警告！！");
						box.setMessage("确认退课？");
						int result = box.open();
						if (result == SWT.OK) {
							String id = Course.this.course.get("jx0404id").getAsString();
							while (true) {
								try {
									if (courseData.quit(id)) {
										isSelected = false;
										selected.remove(Course.this);
										lightenLable(false);
										Course.this.status = false;
										save();
										MessageBox info = new MessageBox(scroll.getShell(), SWT.OK);
										info.setMessage("退课成功");
										info.open();
										break;
									} else {
										MessageBox info = new MessageBox(scroll.getShell(), SWT.OK);
										info.setMessage("退课失败");
										info.open();
										break;
									}
								} catch (AuthenticationException e1) {
									if (!Main.login(scroll.getShell())) {
										break;
									}
								} catch (Exception e2) {
									MessageBox info = new MessageBox(scroll.getShell(), SWT.OK);
									info.setMessage("退课失败：" + e2.getMessage());
									info.open();
									break;
								}
							}
						}
					}
					
					@Override
					public void mouseDown(MouseEvent e) {
						if (e.button == 3) {
							isChecked = false;
							Course.this.checkItem(false);
							Course.this.disposeLable();
						} else if (e.button == 1){
							if (isSelected) {
								isSelected = false;
								selected.remove(Course.this);
								if (item != null) {
									item.setFont(0, NORMAL_TREE);
								}
							} else {
								isSelected = true;
								selected.add(Course.this);
								if (item != null) {
									item.setFont(0, BOLD_TREE);
								}
							}
							for (Control control : info.getChildren()) {
								if (control instanceof Label) {
									Label label = (Label) control;
									label.setText("总学分：" + String.valueOf(computeMarks()));
								}
							}
						}
					}
				});
				labels.add(label);
			}
		}
		
		public void disposeLable() {
			for (Label label : labels) {
				label.dispose();
			}
			labels.removeAll(labels);
		}
		
		public void lightenLable(boolean light) {
			for (Label l : labels) {
				l.setBackground(picker.changeLighten(hue, light));							
			}
		}
		
		public void displayItem() {
			TreeItem root = null;
			for (TreeItem item : tree.getItems()) {
				if (item.getData().equals(category)) {
					root = item;
				}
			}
			TreeItem parent = null;
			JsonElement element = course;
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
			item = new TreeItem(parent, SWT.NONE);
			JsonElement e1 = element.getAsJsonObject().get("fzmc");
			item.setText(0, element.getAsJsonObject().get("kcmc").getAsString()
					+ (e1.isJsonNull() ? "" : "[" + e1.getAsString() + "]"));
			if (isSelected) {
				item.setFont(0, BOLD_TREE);
			}
			item.setText(1, String.valueOf(element.getAsJsonObject().get("xf").getAsInt()));
			JsonElement e2 = element.getAsJsonObject().get("skls");
			item.setText(2, e2.isJsonNull() ? "无" : e2.getAsString());
			JsonElement e3 = element.getAsJsonObject().get("pgtj");
			item.setText(3, e3.isJsonNull() ? "无" : e3.getAsString());
			checkItem(isChecked);
			item.setData(this);
		}
		
		public void disposeItem() {
			if (item != null) {
				item.setChecked(isChecked);
				recurseParent(item, isChecked);
				TreeItem parent = item.getParentItem();
				item.dispose();
				item = null;
				if (parent.getItems().length == 0) {
					parent.dispose();
				}
			}
		}
		
		public void checkItem(boolean isChecked) {
			this.isChecked = isChecked;
			if (item != null) {
				item.setChecked(isChecked);
				item.setGrayed(false);
				recurseParent(item, isChecked);				
			}
		}
		
		@Override
		public String toString() {
			JsonElement e1 = course.getAsJsonObject().get("fzmc");
			JsonArray times = course.get("kkapList").getAsJsonArray();
			String arrengement = "";
			for (JsonElement time : times) {
				JsonObject t = time.getAsJsonObject();
				arrengement += String.format("%s周\t%s %s节\t%s\r\n", 
						t.get("kkzc").getAsString(), 
						WEEK[t.get("xq").getAsInt()], 
						t.get("skjcmc").getAsString(), 
						t.get("jsmc").getAsString());
			}
			return String.format("课程名称：%s\r\n"
					+ "课程号： %s\t"
					+ "学分：%d\t"
					+ "上课老师：%s\r\n"
					+ "状态：%s\t"
					+ "剩余人数： %s\r\n"
					+ "课程安排：\r\n%s", 
					course.getAsJsonObject().get("kcmc").getAsString() + (e1.isJsonNull() ? "" : "[" + e1.getAsString() + "]"),
					course.getAsJsonObject().get("kch").getAsString(),
					course.getAsJsonObject().get("xf").getAsInt(), 
					course.getAsJsonObject().get("skls").isJsonNull() ? "None" : course.getAsJsonObject().get("skls").getAsString(), 
					Course.this.status ? "已选" : "待选", 
					course.getAsJsonObject().get("syrs").isJsonNull() ? "NaN" : course.getAsJsonObject().get("syrs").getAsString(), 
					arrengement);
		}
	}
	
	private CourseData courseData;
	private Tree tree;
	private ScrolledComposite scroll;
	private Group info;
	private ArrayList<Composite> weekList = new ArrayList<>();
	private ArrayList<Course> courses = new ArrayList<>();
	private ArrayList<Course> selected = new ArrayList<>();
	private ColorPicker picker;

	public CourseManager(ScrolledComposite scroll, Tree tree, Group group, CourseData courseData) {
		this.tree = tree;
		this.scroll = scroll;
		this.info = group;
		this.courseData = courseData;
		picker = new ColorPicker(scroll.getDisplay());
	}
	
	public ArrayList<Course> getCourses() {
		return courses;
	}
	
	public ArrayList<Course> getSelected() {
		return selected;
	}
	
	private int computeMarks() {
		int total = 0;
		for (Course course : selected) {
			total += course.course.get("xf").getAsInt();
		}
		return total;
	}
	
	private void computeSize() {
		SashForm parent = (SashForm) weekList.get(0).getParent().getParent();
		double widthSum = 0;
		int[] weight = new int[8];
		Control[] controls = parent.getChildren();
		for (int i = 1; i < WEEK.length; i++) {
			if (controls[i] instanceof SashForm) {
				Composite target = (Composite) controls[i];
				double requiredWidth = target.getChildren()[1].computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
				weight[i] = (int) requiredWidth;
				widthSum += requiredWidth;
			}
		}
		weight[0] = 10;
		widthSum += 10;
		parent.setWeights(weight);
		scroll.setMinWidth((int) widthSum + 60);
	}
	
	private void recurseParent(TreeItem item, boolean state) {
		TreeItem parent;
		TreeItem head = item;
		while ((parent = head.getParentItem()) != null) {
			if (state) {
				parent.setChecked(true);
				parent.setGrayed(false);
				for (TreeItem item1 : parent.getItems()) {
					if (!item1.getChecked() || item1.getGrayed()) {
						parent.setGrayed(true);
						break;
					}
				}
			} else {
				parent.setGrayed(true);
				parent.setChecked(false);
				for (TreeItem item1 : parent.getItems()) {
					if (item1.getChecked()) {
						parent.setChecked(true);
						break;
					}
				}
			}
			head = parent;
		}
	}
	
	private ArrayList<Course> search(String name, ArrayList<Course> source) {
		if (name == null) {
			return source;
		}
		ArrayList<Course> target = new ArrayList<>();
		for (Course course : source) {
			JsonObject jsonObject = course.course;
			if (jsonObject.get("kcmc").getAsString().contains(name)
					|| (!jsonObject.get("fzmc").isJsonNull() && jsonObject.get("fzmc").getAsString().contains(name))
					|| jsonObject.get("kch").getAsString().contains(name) 
					|| jsonObject.get("jx0404id").getAsString().equals(name)
					|| (jsonObject.get("skls") == null ? false : jsonObject.get("skls").toString().contains(name))) {
				target.add(course);
			}
		}
		return target;
	}
	
	public void searchCourse(String string, boolean isFromSelected) {
		for (Course course : courses) {
			course.disposeItem();
		}
		
		ArrayList<Course> source;
		if (isFromSelected) {
			source = selected;
		} else {
			source = courses;
		}
		for (Course course : search(string, source)) {
			course.displayItem();
		}
		
		for (TreeItem item : tree.getItems()) {
			item.setExpanded(true);
			if (isFromSelected) {
				for (TreeItem treeItem : item.getItems()) {
					treeItem.setExpanded(true);
				}
			}
		}
	}
	
	public void updateData() {
		tree.removeAll();
		if (scroll.getContent() != null) {
			scroll.getContent().dispose();
		}
		courses = new ArrayList<>();
		selected = new ArrayList<>();
		weekList = new ArrayList<>();
		System.gc();
		init();
		searchCourse(null, false);
	}
	
	private void init() {
		for (Entry<String, JsonElement> entry : courseData.getCourse().entrySet()) {
			for (JsonElement course : entry.getValue().getAsJsonArray()) {
				Course newCourse = new Course((JsonObject) course, CourseRepo.valueOf(entry.getKey()));
				courses.add(newCourse);
			}
		}
		
		for (JsonElement selected : courseData.getSelected()) {
			for (Course target : search(selected.getAsJsonObject().get("id").getAsString(), courses)) {
				target.isSelected = true;
				target.isChecked = true;
				target.status = selected.getAsJsonObject().get("status").getAsBoolean();
				this.selected.add(target);
				break;
			}
		}

		tree.setFont(NORMAL_TREE);
		
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
		
		tree.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.item instanceof TreeItem) {
					TreeItem item = (TreeItem) e.item;
					if (e.detail == SWT.CHECK) {
						boolean checked = item.getChecked();
						item.setGrayed(false);
						Stack<TreeItem> items = new Stack<>();
						items.push(item);
						while (!items.isEmpty()) {
							TreeItem item1 = items.pop();
							item1.setChecked(checked);
							item1.setGrayed(false);
							if (item1.getData() instanceof Course) {
								if (checked) {
									((Course) item1.getData()).layoutLable();
									((Course) item1.getData()).isChecked = true;
								} else {
									((Course) item1.getData()).disposeLable();
									((Course) item1.getData()).isChecked = false;
//									((Course) item1.getData()).isSelected = false;
								}
							}
							items.addAll(Arrays.asList(item1.getItems()));
						}
						recurseParent(item, checked);
						computeSize();
					}
					Text text = null;
					for (Control control : info.getChildren()) {
						if (control instanceof Text) {
							text = (Text) control;
						}
					}
					if (item.getData() instanceof Course) {
						text.setText(item.getData().toString());
					} else {
						text.setText("");
					}
				}
			}
		});

		for (CourseRepo repo : CourseRepo.values()) {
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setData(repo);
			item.setText(repo.getName());
		}
		
		SashForm ver = new SashForm(scroll, SWT.HORIZONTAL | SWT.BORDER);
//		ver.setEnabled(false);

		for (String string : WEEK) {
			SashForm hor = new SashForm(ver, SWT.VERTICAL);
//			hor.setEnabled(false);

			Label lbl = new Label(hor, SWT.CENTER);
			lbl.setText(string);

			Composite composite = new Composite(hor, SWT.NONE);
			composite.setLayout(new FormLayout());

			for (int i = 0; i < 10; i++) {
				Label line = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
				line.setData("static");
				
				FormData fd_line = new FormData();
				fd_line.left = new FormAttachment(0, 0);
				fd_line.right = new FormAttachment(100, 0);
				fd_line.top = new FormAttachment(i * 10, 0);
				line.setLayoutData(fd_line);
				if (string.equals("")) {
					Label num = new Label(composite, SWT.CENTER);
					FormData fd_num = new FormData();
					fd_num.left = new FormAttachment(0, 0);
					fd_num.right = new FormAttachment(100, 0);
					fd_num.top = new FormAttachment(i * 10 + 4, 0);
					num.setLayoutData(fd_num);
					num.setText(String.valueOf(i + 1));
				}
			}
			hor.setWeights(new int[] { 1, 40 });
			weekList.add(composite);
		}

		scroll.setContent(ver);

		ver.setWeights(new int[] { 5, 32, 32, 32, 32, 32, 32, 32 });
		
		for (Control control : info.getChildren()) {
			if (control instanceof Label) {
				Label label = (Label) control;
				label.setText("总学分：" + String.valueOf(computeMarks()));
			}
			control.setFont(NORMAL);
		}
		
		for (Course course : selected) {
			course.layoutLable();
		}
	}

	public void save() {
		courseData.selected = new JsonArray();
		for (Course course : selected) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("id", course.course.get("jx0404id").getAsString());
			jsonObject.addProperty("status", course.status);
			courseData.selected.add(jsonObject);
		}
	}
}
