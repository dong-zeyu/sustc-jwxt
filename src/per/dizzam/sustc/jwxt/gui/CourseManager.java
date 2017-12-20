package per.dizzam.sustc.jwxt.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import per.dizzam.sustc.jwxt.CourseData;
import per.dizzam.sustc.jwxt.CourseRepo;

public class CourseManager {
	
	private static final String[] WEEK = new String[] { "", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
	
	class Course {
		
		private ArrayList<Label> labels;
		private JsonObject course;
		private float hue;
		private boolean isSelected;
		
		public Course(JsonObject course) {
			labels = new ArrayList<>();
			this.course = course;
			hue = picker.getCurrentHue();
			layout();
		}
		
		private void layout() {
			JsonArray times = course.get("kkapList").getAsJsonArray();
			Color color = picker.changeLighten(hue, false);
			for (JsonElement time : times) {
				JsonObject t = time.getAsJsonObject();
				int week = t.get("xq").getAsInt();
				int from = Integer.valueOf(t.get("skjcmc").getAsString().split("-")[0]);
				int to = Integer.valueOf(t.get("skjcmc").getAsString().split("-")[1]);
				Composite composite = weekList.get(week);
				FormData fd_l = new FormData();
				Label label = new Label(composite, SWT.WRAP);
				label.setText(course.get("kcmc").getAsString());
				label.setData(course);
				label.setBackground(color);
				fd_l.top = new FormAttachment((from - 1) * 10, 2);
				fd_l.bottom = new FormAttachment(to * 10, -1);
				int left = 0;
				for (Control control : composite.getChildren()) {
					if (control.getData() != null && control.getData() instanceof JsonObject && !control.equals(label)) {
						FormData data = (FormData) control.getLayoutData();
						if (data.top.numerator < fd_l.bottom.numerator && data.bottom.numerator > fd_l.top.numerator
								&& data.right.offset > left) {
							left = data.right.offset;
						}
					}
				}
				fd_l.left = new FormAttachment(0, left);
				fd_l.right = new FormAttachment(0, left + 15);
				label.setLayoutData(fd_l);
//				FontData fd = new FontData("MyFont", 10, SWT.ITALIC);
//				label.setFont(new Font(scroll.getDisplay(), fd));
				label.moveAbove(null);
				computeSize(composite);
				label.requestLayout();
				label.addMouseTrackListener(new MouseTrackAdapter() {

					@Override
					public void mouseEnter(MouseEvent e) {
						for (Label l : labels) {
							l.setBackground(picker.changeLighten(hue, true));							
						}
					}

					@Override
					public void mouseExit(MouseEvent e) {
						if (!isSelected) {
							for (Label l : labels) {
								l.setBackground(picker.changeLighten(hue, false));							
							}							
						}
					}
				});
				label.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseDown(MouseEvent e) {
						if (isSelected) {
							isSelected = false;
						} else {
							isSelected = true;
						}
					}
				});
				labels.add(label);
			}
		}
		
		public JsonObject getCourse() {
			return course;
		}

		public void dispose() {
			for (Label label : labels) {
				label.dispose();
			}
		}
	}
	
	private CourseData courseData;
	private Tree tree;
	private ScrolledComposite scroll;
	private ArrayList<Composite> weekList = new ArrayList<>();
	private ArrayList<Course> courses = new ArrayList<>();
	private ColorPicker picker;

	public CourseManager(ScrolledComposite scroll, Tree tree, CourseData courseData) {
		this.tree = tree;
		this.scroll = scroll;
		this.courseData = courseData;
		picker = new ColorPicker(scroll.getDisplay());
		init();
	}
	
	public void addCourse(JsonObject course) throws IllegalArgumentException {
		courses.add(new Course(course));
	}

	private void computeSize(Composite target) {
		scroll.setMinWidth(target.computeSize(SWT.DEFAULT, SWT.DEFAULT).x * 7 + 20);
//		int max = 0;
//		for (Control control : target.getChildren()) {
//			int tmp = ((FormData)control.getLayoutData()).right.offset;
//			max = max < tmp ? tmp : max;
//		}
//		if (target.getBounds().width < max) {
//			scroll.setMinWidth(max * 7 + 20);
//		}
	}

	public void removeCourse(JsonObject course) {
		for (Course course1 : courses) {
			if (course1.getCourse().equals(course)) {
				courses.remove(course1);
				course1.dispose();
				break;
			}
		}
	}
	
	public void updateData(String string) {
		tree.removeAll();
		JsonObject result = string == null ? courseData.getCourse() : courseData.search(string);
		for (Entry<String, JsonElement> entry : result.entrySet()) {
			for (JsonElement element : entry.getValue().getAsJsonArray()) {
				TreeItem root = null;
				for (TreeItem item : tree.getItems()) {
					if (item.getText().equals(CourseRepo.valueOf(entry.getKey()).getName())) {
						root = item;
					}
				}
				if (root == null) {
					TreeItem newItem = new TreeItem(tree, SWT.CHECK);
					newItem.setText(CourseRepo.valueOf(entry.getKey()).getName());
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
				item.setData(element);
			}
		}
		for (TreeItem item : tree.getItems()) {
			item.setExpanded(true);
		}
	}
	
	private void init() {

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
					boolean checked = item.getChecked();
					item.setChecked(checked);
					item.setGrayed(false);
					Stack<TreeItem> items = new Stack<>();
					items.push(item);
					while (!items.isEmpty()) {
						TreeItem item1 = items.pop();
						item1.setChecked(checked);
						item1.setGrayed(false);
						if (item1.getItems().length == 0 && item1.getData() instanceof JsonObject) {
							if (checked) {
								addCourse((JsonObject) item1.getData());
							} else {
								removeCourse((JsonObject) item1.getData());
							}
						}
						items.addAll(Arrays.asList(item1.getItems()));
					}
					TreeItem parent;
					TreeItem head = item;
					while ((parent = head.getParentItem()) != null) {
						if (checked) {
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
			}
		});

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

		ver.setWeights(new int[] { 2, 9, 9, 9, 9, 9, 9, 9 });
	}
}
