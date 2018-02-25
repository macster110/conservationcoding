package colourSliders;

import colourSliders.ColourArray.ColourArrayType;
import colourSliders.skin.ColourSliderSkin;
import javafx.geometry.Orientation;
import javafx.scene.control.Skin;

/**
 * The colour range slider shows a linear colour gradient between two thumbs with the rest of the slider coloured by 
 * the min and max of the colour gradient. The slider is generally used to allow users to change settings for anything requiring a 
 * colour scale, e.g. a spectrogram. 
 * @author Jamie Macaulay	
 *
 */
public class ColourRangeSlider extends RangeSlider {

	private ColourSliderSkin colourSliderSkin;

	/**
	 * Create the colour range slider. 
	 */
	public ColourRangeSlider() {
		super();
	}

	/**
	 * Create the colour range slider.
	 * @param min - minimum value of slider
	 * @param max -maximum value of slider
	 * @param lowValue - value of low thumb
	 * @param highValue -value of high thumb
	 */
	public ColourRangeSlider(double min, double max, double lowValue,
			double highValue) {
		super(min, max, lowValue, highValue);
	}
	
    public ColourRangeSlider(Orientation orientation) {
		super();
		this.setOrientation(orientation);
	}

	@Override
    protected Skin<?> createDefaultSkin() {
    	if (colourSliderSkin==null) return colourSliderSkin=new ColourSliderSkin(this);
    	else return colourSliderSkin;
    }

    /**
     * Get the colour slide3r skin.
     * @return reference to the ColourSliderSkin for the colour slider. 
     */
	public ColourSliderSkin getColourSliderSkin() {
		return colourSliderSkin;
	}
	
	/**
	 * Set the colour array for the colour range slider. This is a convenience function which call directly into 
	 * ColourSliderSkin. 
	 * @param colourMap - the colour map for the slider- the colour map is shown between the two slider thumbs
	 */
	public void setColourArrayType(ColourArrayType colourMap){
		if (colourSliderSkin==null) createDefaultSkin();
		colourSliderSkin.setColourScale(colourMap);
	}

    
}
