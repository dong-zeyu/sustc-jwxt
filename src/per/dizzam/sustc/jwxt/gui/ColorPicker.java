package per.dizzam.sustc.jwxt.gui;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.RGB;

public class ColorPicker {

	private Device device;
	private float hue;

	public ColorPicker(Device device) {
		this.device = device;
		hue = 0;
	}
	
	public Color pick(boolean isLight) {
		hue += 12 * Math.PI;
		hue = hue % 360;
		float saturation;
		if (isLight) {
			saturation = 1f;
		} else {
			saturation = 0.5f;
		}
		return new Color(device, new RGB(hue, saturation, 1));
	}
}
