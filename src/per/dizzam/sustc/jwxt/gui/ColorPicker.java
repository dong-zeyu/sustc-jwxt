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
		float saturation;
		float light;
		if (isLight) {
			saturation = 1f;
			light = 1f;
		} else {
			saturation = 0f;
			light = 0.7f;
		}
		return new Color(device, new RGB(getCurrentHue(), saturation, light));
	}

	public float getCurrentHue() {
		hue += 30 * Math.PI;
		hue = hue % 360;
		return hue;
	}

	public Color changeLighten(float hue, boolean isLight) {
		float saturation;
		float light;
		if (isLight) {
			light = 1f;
			saturation = 1f;
		} else {
			saturation = 0f;
			light = 0.7f;
		}
		return new Color(device, new RGB(hue, saturation, light));
	}
}
