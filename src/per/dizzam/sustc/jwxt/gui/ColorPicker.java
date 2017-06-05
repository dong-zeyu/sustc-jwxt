package per.dizzam.sustc.jwxt.gui;

import java.util.Arrays;

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
			saturation = 0.9f;
		} else {
			saturation = 0.3f;
		}
		return new Color(device, new RGB(hue, saturation, 1));
	}
	
	public Color changeLighten(Color pervious, boolean isLight) {
		float saturation;
		if (isLight) {
			saturation = 0.9f;
		} else {
			saturation = 0.3f;
		}
		return new Color(device,
				new RGB(rgb2hsb(pervious.getRed(), pervious.getGreen(), pervious.getBlue()), saturation, 1));
	}
	
	public static float rgb2hsb(int rgbR, int rgbG, int rgbB) {  
	    assert 0 <= rgbR && rgbR <= 255;  
	    assert 0 <= rgbG && rgbG <= 255;  
	    assert 0 <= rgbB && rgbB <= 255;  
	    int[] rgb = new int[] { rgbR, rgbG, rgbB };  
	    Arrays.sort(rgb);
	    int max = rgb[2];  
	    int min = rgb[0];
	    
	    float hsbH = 0;  
	    if (max == rgbR && rgbG >= rgbB) {  
	        hsbH = (rgbG - rgbB) * 60f / (max - min) + 0;  
	    } else if (max == rgbR && rgbG < rgbB) {  
	        hsbH = (rgbG - rgbB) * 60f / (max - min) + 360;  
	    } else if (max == rgbG) {  
	        hsbH = (rgbB - rgbR) * 60f / (max - min) + 120;  
	    } else if (max == rgbB) {  
	        hsbH = (rgbR - rgbG) * 60f / (max - min) + 240;  
	    }
	    return hsbH;
	}
}
