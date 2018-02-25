package colourSliders;

import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.tk.Toolkit;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextBoundsType;

/**
 * Created by pedro_000 on 6/28/2014.
 */
public class Utils1 {

    /* Using TextLayout directly for simple text measurement.
     * Instead of restoring the TextLayout attributes to default values
     * (each renders the TextLayout unable to efficiently cache layout data).
     * It always sets all the attributes pertinent to calculation being performed.
     * Note that lineSpacing and boundsType are important when computing the height
     * but irrelevant when computing the width.
     *
     * Note: This code assumes that TextBoundsType#VISUAL is never used by controls.
     * */
    private static final TextLayout layout = Toolkit.getToolkit().getTextLayoutFactory().createLayout();

    public static double computeTextWidth(Font font, String text, double wrappingWidth) {
        layout.setContent(text != null ? text : "", font.impl_getNativeFont());
        layout.setWrapWidth((float)wrappingWidth);
        return layout.getBounds().getWidth();
    }

    public static double computeTextHeight(Font font, String text, double wrappingWidth, double lineSpacing, TextBoundsType boundsType) {
        layout.setContent(text != null ? text : "", font.impl_getNativeFont());
        layout.setWrapWidth((float)wrappingWidth);
        layout.setLineSpacing((float)lineSpacing);
        if (boundsType == TextBoundsType.LOGICAL_VERTICAL_CENTER) {
            layout.setBoundsType(TextLayout.BOUNDS_CENTER);
        } else {
            layout.setBoundsType(0);
        }
        return layout.getBounds().getHeight();
    }
    
	/**
	 * Clamps a value so that it has to sit between two values
	 * @param min min allowed value
	 * @param value desired value
	 * @param max max allowed value
	 * @return clamped value between min and max. 
	 */
	public static double clamp(double min, double value, double max) {	
		return Math.max(min, Math.min(max, value));
	}
	
	/**
	 * Returns the nearest value, less or more. If exactly in the middle
	 * it will return more
	 * @param less lower value
	 * @param value value to compare
	 * @param more upper value
	 * @return closest of less and more. 
	 */
	public static double nearest(double less, double value, double more) {
		double r1 = value-less;
		double r2 = more-value;
		return r1<r2 ? less:more;
	}
	
	
	/**
	 * Converts a colour to a hex string. 
	 * @param color - the colour to convert 
	 * @return hex string of colour. 
	 */
	public static String color2Hex(Color color){
        int r =  (int) (color.getRed() * 255);
        int g =  (int) (color.getGreen() * 255);
        int b =  (int) (color.getBlue() * 255);
        String str = String.format("#%02X%02X%02X;", r, g, b);
        return str; 
	}
	

}