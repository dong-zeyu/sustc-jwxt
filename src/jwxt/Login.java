package jwxt;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

public class Login extends Dialog {

	protected Object result;
	protected Shell shell;
	private Text text_1;
	private Text text;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public Login(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM);
		shell.setSize(450, 230);
		shell.setText(getText());

		Label lblUserName = new Label(shell, SWT.NONE);
		lblUserName.setAlignment(SWT.RIGHT);
		lblUserName.setBounds(65, 50, 71, 17);
		lblUserName.setText("User Name:");

		text = new Text(shell, SWT.BORDER);
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.ESC) {
					result = null;
					shell.dispose();
				} else if (e.character == SWT.CR || e.character == SWT.LF) {
					text_1.setFocus();
				}
			}
		});
		text.setBounds(142, 47, 220, 23);

		Label lblPassword = new Label(shell, SWT.NONE);
		lblPassword.setAlignment(SWT.RIGHT);
		lblPassword.setBounds(65, 94, 71, 17);
		lblPassword.setText("Password:");

		text_1 = new Text(shell, SWT.BORDER | SWT.PASSWORD);
		text_1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR || e.character == SWT.LF) {
					result = new String[] { text.getText(), text_1.getText() };
					shell.dispose();
				} else if (e.character == SWT.ESC) {
					result = null;
					shell.dispose();
				}
			}
		});
		text_1.setBounds(142, 91, 220, 23);

		Button btnLogin = new Button(shell, SWT.NONE);
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				result = new String[] { text.getText(), text_1.getText() };
				shell.dispose();
			}
		});
		btnLogin.setBounds(142, 134, 80, 27);
		btnLogin.setText("Login");

		Button btnCannel = new Button(shell, SWT.NONE);
		btnCannel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result = null;
				shell.dispose();
			}
		});
		btnCannel.setBounds(282, 134, 80, 27);
		btnCannel.setText("Cannel");

	}
}
