package per.dizzam.sustc.jwxt;

@SuppressWarnings("serial")
public class StatusException extends Exception {

	public StatusException(String msg) {
		super(msg);
	}

	public StatusException(String msg, Throwable e) {
		super(msg, e);
	}
}
