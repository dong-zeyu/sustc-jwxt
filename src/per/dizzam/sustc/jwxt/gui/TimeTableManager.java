package per.dizzam.sustc.jwxt.gui;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TimeTableManager {
	
	private static final String[] WEEK = new String[] { "", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
	
	private ScrolledComposite scroll;
	private ArrayList<Composite> weekList = new ArrayList<>();
	private ArrayList<JsonObject> courses = new ArrayList<>();
	private ColorPicker picker;

	public TimeTableManager(Composite parent) {
		picker = new ColorPicker(parent.getDisplay());
		
		scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setMinWidth(0);

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
	
	public void addCourse(JsonObject course) throws IllegalArgumentException {
		check(course);
		courses.add(course);
		JsonArray times = course.get("kkapList").getAsJsonArray();
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
			label.setBackground(picker.pick(false));
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
			composite.getChildren();
			fd_l.left = new FormAttachment(0, left);
			fd_l.right = new FormAttachment(0, left + 15);
			label.setLayoutData(fd_l);
//			FontData fd = new FontData("MyFont", 10, SWT.ITALIC);
//			label.setFont(new Font(scroll.getDisplay(), fd));
			label.moveAbove(null);
			label.requestLayout();
			label.addMouseTrackListener(new MouseTrackAdapter() {

				@Override
				public void mouseEnter(MouseEvent e) {
					label.setBackground(picker.changeLighten(label.getBackground(), true));
				}

				@Override
				public void mouseExit(MouseEvent e) {
					label.setBackground(picker.changeLighten(label.getBackground(), false));
				}
			});
		}
	}

	private void check(JsonObject course) throws IllegalArgumentException {
		
	}
}
