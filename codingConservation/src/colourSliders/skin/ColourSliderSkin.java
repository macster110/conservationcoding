package colourSliders.skin;


import colourSliders.ColourArray;
import colourSliders.ColourArray.ColourArrayType;
import colourSliders.RangeSlider;
import colourSliders.Utils1;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;


public class ColourSliderSkin extends RangeSliderSkin {
	
	public double trackWidth=19;
	
	/**
	 * The slider consists of three colour components. The track, the range bar and the top bar. The track is lowest in z order and is coloured at the end of the colour scale. 
	 * The range bar shows all colours between the start and end of the  colour map. The top bar shows the start colour. Range bar and the track are already defined in RangeSliderSkin 
	 * but the top bar has to be defined here. 
	 */
	private StackPane topBar;

	public ColourSliderSkin(RangeSlider rangeSlider) {
		super(rangeSlider);	
		
		if (getSkinnable().getOrientation()==Orientation.VERTICAL){
			rangeSlider.setPrefWidth(trackWidth);
			getSkinnable().setPrefWidth(trackWidth);
			getTrack().setPrefWidth(trackWidth);
		}
		else{
			rangeSlider.setPrefHeight(trackWidth);
			getSkinnable().setPrefHeight(trackWidth);
			getTrack().setPrefHeight(trackWidth);
		}

		setColourScale(ColourArrayType.GREY);
	}
	
	@Override
	protected void initRangeBar() {
		super.initRangeBar();
	}
	
	/**
	 * Create the top bar. 
	 */
	public void initTopBar(){
		topBar=new StackPane();
		//TODO-need to sort for horizontal 
		if (getSkinnable().getOrientation()==Orientation.VERTICAL){
			topBar.layoutXProperty().bind(track.layoutXProperty());
		}
		else {
			topBar.layoutYProperty().bind(track.layoutYProperty());
		}
//		topBar.setStyle("-fx-background-color: red;");
		getChildren().add(topBar);
	}
	
	/**
	 * Manage the order of the nodes which are added to the pane. Important as some 
	 * Nodes overlap others. 
	 */
	@Override
	protected void initSliderNodes(){
		initTrack(); //background
		initTopBar(); //on top of background
		initRangeBar(); //gradient between thunbs
		initFirstThumb(); //thumbs on top of everything
		initSecondThumb();
		//show tick marks if necessary
		setShowTickMarks(getSkinnable().isShowTickMarks(), getSkinnable().isShowTickLabels());
		
		initHighLabel();
		initLowLabel();
	}
	
	@Override
	protected void layoutChildren(final double x, final double y,
			final double w, final double h) {
		super.layoutChildren(x, y, w, h);
		//		topBar.resizeRelocate(0, 0, 
		//				trackWidth, rangeStart+1);
		//TODO-need to sort for horizontal 
		if (getSkinnable().getOrientation()==Orientation.VERTICAL){
			topBar.layoutYProperty().setValue(0);
			topBar.resize(trackWidth, rangeStart+1);
		}
		else {
			topBar.layoutXProperty().setValue(0);
			//topBar.layoutYProperty().setValue(-3);
			topBar.resize(rangeStart+1, trackWidth);
		}
	};
	

	@Override
	protected void handleControlPropertyChanged(String p) {
		super.handleControlPropertyChanged(p);
		if ("HIGH_VALUE".equals(p)) { //$NON-NLS-1$
//			topBar.resizeRelocate(0, 0, 
//					trackWidth, rangeStart+1);
			//TODO-need to sort for horizontal 
			if (getSkinnable().getOrientation()==Orientation.VERTICAL){
				topBar.layoutYProperty().setValue(0);
				topBar.resize(trackWidth, rangeStart+1);
			}
			else {
				topBar.layoutXProperty().setValue(0);
				topBar.resize(rangeStart+1, trackWidth);
			}
		}
	}
	
	/**
	 * Set the colours for the colour range slider. The slider consists of the range bar which contains the colour gradient, 
	 * the top bar which contains the top colour and the track which contains the background colour. 
	 */
	public void setColourScale(ColourArrayType colourMap){
		
		Color trackCol;
		Color topBarCol;
//		switch (colourMap){
//		case GREY:
//			trackCol=Color.WHITE;
//			topBarCol=Color.BLACK;
//			break;
//		case REVERSEGREY:
//			trackCol=Color.BLACK;
//			topBarCol=Color.WHITE;
//			break;
//		case BLUE:
//			trackCol=Color.BLACK;
//			topBarCol=Color.BLUE;
//			break;
//		case GREEN:
//			trackCol=Color.BLACK;
//			topBarCol=Color.GREEN;
//			break;
//		case RED:
//			trackCol=Color.BLACK;
//			topBarCol=Color.RED;
//			break;
//		case HOT:
//			trackCol=Color.BLACK;
//			topBarCol=Color.RED;
//			break;
//		case FIRE:
//			trackCol=Color.BLACK;
//			topBarCol=Color.WHITE;
//			break;
//		case PATRIOTIC:
//			trackCol=Color.BLUE;
//			topBarCol=Color.RED;
//			break;
//		default:
//			trackCol=Color.WHITE;
//			topBarCol=Color.BLACK;
//			break;
//		}
		
		//set the colour gradient
		Color[] colorList=ColourArray.getColorList(colourMap);
		trackCol=colorList[0]; 
		topBarCol=colorList[colorList.length-1]; 
		
		//set the solid colours for the track and top bar. 
//		getTrack().setBackground(new Background(new BackgroundFill(trackCol, CornerRadii.EMPTY, Insets.EMPTY)));
        //28/03/2017 - had to change to css as adding to a scroll pane seemed ot override background. 
		getTrack().setStyle("-fx-background-color: " + Utils1.color2Hex(trackCol));

		getTopBar().setBackground(new Background(new BackgroundFill(topBarCol, CornerRadii.EMPTY, Insets.EMPTY)));

		//set the colour gradient
		Stop[] stops =new Stop[colorList.length];
		for (int j=0; j<colorList.length; j++){
			stops[j]=new Stop((double) j/(colorList.length-1),colorList[j]);
		};
		
		LinearGradient linearGradient;
		if (getSkinnable().getOrientation()==Orientation.VERTICAL) {
			 linearGradient=new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE, stops);
		}
		else {
			 linearGradient=new LinearGradient(1,0, 0, 0, true, CycleMethod.NO_CYCLE, stops);

		}
		getRangeBar().setBackground(new Background(new BackgroundFill(linearGradient, CornerRadii.EMPTY, Insets.EMPTY)));
	}

	/**
	 * Get the top bar. Overlays the track and used to display the last colour in the colourmap. 
	 * @return
	 */
	public StackPane getTopBar() {
		return this.topBar;
	}
		

}
