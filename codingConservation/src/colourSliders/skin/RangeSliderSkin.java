package colourSliders.skin;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;




import com.sun.javafx.css.StyleManager;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

import colourSliders.RangeSlider;
import colourSliders.RangeSliderBehavior;
import colourSliders.RangeSliderBehavior.FocusedChild;

import static colourSliders.RangeSliderBehavior.FocusedChild.HIGH_THUMB;
import static colourSliders.RangeSliderBehavior.FocusedChild.LOW_THUMB;
import static colourSliders.RangeSliderBehavior.FocusedChild.NONE;
import static colourSliders.RangeSliderBehavior.FocusedChild.RANGE_BAR;

public class RangeSliderSkin extends BehaviorSkinBase<RangeSlider, RangeSliderBehavior> {

	static {
		// refer to ControlsFXControl for why this is necessary
		StyleManager.getInstance().addUserAgentStylesheet(
				RangeSlider.class.getResource("rangeslider.css").toExternalForm()); //$NON-NLS-1$
	}

	/** Track if slider is vertical/horizontal and cause re layout */
	private NumberAxis tickLine = null;
	private double trackToTickGap = 2;

	private boolean showTickMarks;
	private double thumbWidth;
	private double thumbHeight;

	private Orientation orientation;

	protected StackPane track;
	protected double trackStart;
	protected double trackLength;
	protected double lowThumbPos;
	protected double rangeEnd;
	protected double rangeStart;
	protected ThumbPane lowThumb;
	protected ThumbPane highThumb;
	protected StackPane rangeBar; // the bar between the two thumbs, can be dragged

	// temp fields for mouse drag handling
	private double preDragPos;          // used as a temp value for low and high thumbs
	private Point2D preDragThumbPoint;  // in skin coordinates

	private FocusedChild currentFocus = LOW_THUMB;

	/**
	 * Label which shows when thumb is moving
	 */
	private Label lowLabel;

	/**
	 * Label which shows when thumb is moving
	 */
	private Label highLabel;

	/**
	 * True to how labels if the thumbs are moving. 
	 */
	private boolean showLabels=true;

	/**
	 * The format for the lower thumb label
	 */
	private RangeSliderLabel lowLabelFormat = new DefaultSliderLabel();

	/**
	 * The format for the higher thumb label
	 */
	private RangeSliderLabel highLabelFormat = new DefaultSliderLabel();


	public RangeSliderSkin(final RangeSlider rangeSlider) {
		super(rangeSlider, new RangeSliderBehavior(rangeSlider));
		orientation = getSkinnable().getOrientation();
		initSliderNodes();
		registerChangeListener(rangeSlider.lowValueProperty(), "LOW_VALUE"); //$NON-NLS-1$
		registerChangeListener(rangeSlider.highValueProperty(), "HIGH_VALUE"); //$NON-NLS-1$
		registerChangeListener(rangeSlider.minProperty(), "MIN"); //$NON-NLS-1$
		registerChangeListener(rangeSlider.maxProperty(), "MAX"); //$NON-NLS-1$
		registerChangeListener(rangeSlider.orientationProperty(), "ORIENTATION"); //$NON-NLS-1$
		registerChangeListener(rangeSlider.showTickMarksProperty(), "SHOW_TICK_MARKS"); //$NON-NLS-1$
		registerChangeListener(rangeSlider.showTickLabelsProperty(), "SHOW_TICK_LABELS"); //$NON-NLS-1$
		registerChangeListener(rangeSlider.majorTickUnitProperty(), "MAJOR_TICK_UNIT"); //$NON-NLS-1$
		registerChangeListener(rangeSlider.minorTickCountProperty(), "MINOR_TICK_COUNT"); //$NON-NLS-1$
		lowThumb.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus) {
				if (hasFocus) {
					currentFocus = LOW_THUMB;
				}
			}
		});
		highThumb.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus) {
				if (hasFocus) {
					currentFocus = HIGH_THUMB;
				}
			}
		});
		rangeBar.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus) {
				if (hasFocus) {
					currentFocus = RANGE_BAR;
				}
			}
		});
		rangeSlider.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus) {
				if (hasFocus) {
					lowThumb.setFocus(true);
				} else {
					lowThumb.setFocus(false);
					highThumb.setFocus(false);
					currentFocus = NONE;
				}
			}
		});

		EventHandler<KeyEvent> keyEventHandler = new EventHandler<KeyEvent>() {
			@Override public void handle(KeyEvent event) {
				if (KeyCode.TAB.equals(event.getCode())) {
					if (lowThumb.isFocused()) {
						if (event.isShiftDown()) {
							lowThumb.setFocus(false);
							//							new TraversalEngine(rangeSlider, false).trav(rangeSlider, Direction.PREVIOUS);
						} else {
							lowThumb.setFocus(false);
							highThumb.setFocus(true);
						}
						event.consume();
					} else if (highThumb.isFocused()) {
						if(event.isShiftDown()) {
							highThumb.setFocus(false);
							lowThumb.setFocus(true);
						} else {
							highThumb.setFocus(false);
							//							new TraversalEngine(rangeSlider, false).trav(rangeSlider, Direction.NEXT);
						}
						event.consume();
					}
				}
			}
		};
		getSkinnable().addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);  
		// set up a callback on the behavior to indicate which thumb is currently 
		// selected (via enum).
		getBehavior().setSelectedValue(new Callback<Void, FocusedChild>() {
			@Override public FocusedChild call(Void v) {
				return currentFocus;
			}
		});
	}

	/**
	 * Create all the visible nodes for the slider. 
	 */
	protected void initSliderNodes(){
		initTrack();
		initRangeBar();
		initFirstThumb();
		initSecondThumb();
		setShowTickMarks(getSkinnable().isShowTickMarks(), getSkinnable().isShowTickLabels());
		initHighLabel();
		initLowLabel();
		//		setShowTickMarks(getSkinnable().isShowTickMarks(), getSkinnable().isShowTickLabels());

	}


	protected void initHighLabel(){
		highLabel = new Label(); 
		highLabel.setVisible(true);

		final StackPane highLabelPane=new StackPane(highLabel);
		getChildren().add(highLabelPane);
		//highLabel.setMinWidth(50);
		//highLabel.setPrefWidth(50);
		highLabelPane.toBack();
		highLabel.setMinWidth(Region.USE_PREF_SIZE);
		highLabel.setVisible(false);

		highLabelPane.layoutXProperty().bind(highThumb.layoutXProperty().subtract(highLabel.widthProperty()).add(10));
		highLabelPane.layoutYProperty().bind(highThumb.layoutYProperty().add(highThumb.heightProperty().divide(2)));
		highLabelPane.layoutYProperty().addListener((obs, oldval, newval)->{
			highLabel.setText(highLabelFormat.getText(getSkinnable().getHighValue()));
		});

		getSkinnable().requestLayout();

	}

	public void setHighLabelFormat(RangeSliderLabel rangSliderLabel){
		this.highLabelFormat=rangSliderLabel;
	}	

	protected void initLowLabel(){
		lowLabel = new Label(); 
		lowLabel.setVisible(true);

		final StackPane lowLabelPane=new StackPane(lowLabel);
		getChildren().add(lowLabelPane);
		//lowLabel.setMinWidth(50);
		//lowLabel.setPrefWidth(50);
		lowLabelPane.toBack();
		lowLabel.setMinWidth(Region.USE_PREF_SIZE);
		lowLabel.setVisible(false);

		lowLabelPane.layoutXProperty().bind(lowThumb.layoutXProperty().subtract(lowLabel.widthProperty()).add(10));
		lowLabelPane.layoutYProperty().bind(lowThumb.layoutYProperty().add(lowThumb.heightProperty().divide(2)));
		lowLabelPane.layoutYProperty().addListener((obs, oldval, newval)->{
			lowLabel.setText(lowLabelFormat.getText(getSkinnable().getLowValue()));
		});

		getSkinnable().requestLayout();

	}

	public void setLowLabelFormat(RangeSliderLabel rangSliderLabel){
		this.lowLabelFormat=rangSliderLabel;
	}

	protected void initTrack(){
		getChildren().clear();
		track = new StackPane();
		track.getStyleClass().setAll("track"); //$NON-NLS-1$
		getChildren().add(track);

		track.setOnMousePressed( new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override public void handle(javafx.scene.input.MouseEvent me) {
				if (!lowThumb.isPressed() && !highThumb.isPressed()) {
					if (isHorizontal()) {
						getBehavior().trackPress(me, (me.getX() / trackLength));
					} else {
						getBehavior().trackPress(me, (me.getY() / trackLength));
					}
				}
			}
		});

		track.setOnMouseReleased( new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override public void handle(javafx.scene.input.MouseEvent me) {
				//Nothing being done with the second param in sliderBehavior
				//So, passing a dummy value
				getBehavior().trackRelease(me, 0.0f);
			}
		});

	}

	protected void initFirstThumb() {
		lowThumb = new ThumbPane();
		lowThumb.getStyleClass().setAll("low-thumb"); //$NON-NLS-1$		
		lowThumb.setFocusTraversable(true);
		getChildren().add(lowThumb);

		lowThumb.setOnMousePressed(new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override public void handle(javafx.scene.input.MouseEvent me) {
				highThumb.setFocus(false);
				lowThumb.setFocus(true);
				getBehavior().lowThumbPressed(me, 0.0f);
				preDragThumbPoint = lowThumb.localToParent(me.getX(), me.getY());
				preDragPos = (getSkinnable().getLowValue() - getSkinnable().getMin()) /
						(getSkinnable().getMax() - getSkinnable().getMin());

				lowLabel.setVisible(true);
				//lowLabel.setMinWidth(50);

			}
		});

		lowThumb.setOnMouseReleased(new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override public void handle(javafx.scene.input.MouseEvent me) {
				getBehavior().lowThumbReleased(me);

				lowLabel.setVisible(false);
				//lowLabel.setMinWidth(0);

			}
		});

		lowThumb.setOnMouseDragged(new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override public void handle(javafx.scene.input.MouseEvent me) {
				Point2D cur = lowThumb.localToParent(me.getX(), me.getY());
				double dragPos = (isHorizontal())?
						cur.getX() - preDragThumbPoint.getX() : -(cur.getY() - preDragThumbPoint.getY());
						getBehavior().lowThumbDragged(me, preDragPos + dragPos / trackLength);
			}
		});
	}

	public StackPane getTrack() {
		return track;
	}

	public void setTrack(StackPane track) {
		this.track = track;
	}

	protected void initSecondThumb() {
		highThumb = new ThumbPane();
		highThumb.getStyleClass().setAll("high-thumb"); //$NON-NLS-1$
		highThumb.setFocusTraversable(true);
		if (!getChildren().contains(highThumb)) {
			getChildren().add(highThumb);
		}

		highThumb.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) {
				lowThumb.setFocus(false);
				highThumb.setFocus(true);
				((RangeSliderBehavior) getBehavior()).highThumbPressed(e, 0.0D);
				preDragThumbPoint = highThumb.localToParent(e.getX(), e.getY());
				preDragPos = (((RangeSlider) getSkinnable()).getHighValue() - ((RangeSlider) getSkinnable()).getMin()) / 
						(((RangeSlider) getSkinnable()).getMax() - ((RangeSlider) getSkinnable()).getMin());

				highLabel.setVisible(true);
			}

		}
				);
		highThumb.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) {
				((RangeSliderBehavior) getBehavior()).highThumbReleased(e);

				highLabel.setVisible(false);

			}

		}
				);
		highThumb.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) {
				boolean orientation = ((RangeSlider) getSkinnable()).getOrientation() == Orientation.HORIZONTAL;
				double trackLength = orientation ? track.getWidth() : track.getHeight();

				Point2D point2d = highThumb.localToParent(e.getX(), e.getY());
				double d = ((RangeSlider) getSkinnable()).getOrientation() != Orientation.HORIZONTAL ? -(point2d.getY() - preDragThumbPoint.getY()) : point2d.getX() - preDragThumbPoint.getX();
				((RangeSliderBehavior) getBehavior()).highThumbDragged(e, preDragPos + d / trackLength);
			}
		});
	}

	protected void initRangeBar() {
		rangeBar = new StackPane();
		//       rangeBar.cursorProperty().bind(new ObjectBinding<Cursor>() {
		//           { bind(rangeBar.hoverProperty()); }
		//
		//           @Override protected Cursor computeValue() {
		//               return rangeBar.isHover() ? Cursor.HAND : Cursor.DEFAULT;
		//           }
		//       });
		rangeBar.getStyleClass().setAll("range-bar"); //$NON-NLS-1$


		rangeBar.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) {
				rangeBar.requestFocus();
				preDragThumbPoint = rangeBar.localToParent(e.getX(), e.getY());
				//               preDragPos = isHorizontal() ? e.getX() : -e.getY();
			}
		});

		rangeBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) {
				Point2D newLocation = rangeBar.localToParent(e.getX(), e.getY());
				//              delta = (isHorizontal() ? e.getX() : -e.getY()) - preDragPos;
				double delta=(isHorizontal() ? (newLocation.getX() - preDragThumbPoint.getX())/track.getWidth() : (-newLocation.getY()+preDragThumbPoint.getY())/track.getHeight());
				//				System.out.println("track.getWidth: "+track.getWidth()+" track.getHeight() "+track.getHeight());
				((RangeSliderBehavior) getBehavior()).moveRange(delta);
				preDragThumbPoint=newLocation; 
			}
		});
		
		rangeBar.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) {
				((RangeSliderBehavior) getBehavior()).rangeReleased(e);
			}
		});

		getChildren().add(rangeBar);
	}

	public StackPane getRangeBar() {
		return rangeBar;
	}

	public void setRangeBar(StackPane rangeBar) {
		this.rangeBar = rangeBar;
	}

	protected void setShowTickMarks(boolean ticksVisible, boolean labelsVisible) {
		showTickMarks = (ticksVisible || labelsVisible);
		RangeSlider rangeSlider = getSkinnable();
		if (showTickMarks) {
			if (tickLine == null) {
				tickLine = new NumberAxis();
				tickLine.setAnimated(false);
				tickLine.setAutoRanging(false);
				tickLine.setSide(isHorizontal() ? Side.BOTTOM : Side.RIGHT);
				tickLine.setUpperBound(rangeSlider.getMax());
				tickLine.setLowerBound(rangeSlider.getMin());
				tickLine.setTickUnit(rangeSlider.getMajorTickUnit());
				tickLine.setTickMarkVisible(ticksVisible);
				tickLine.setTickLabelsVisible(labelsVisible);
				tickLine.setMinorTickVisible(ticksVisible);
				// add 1 to the slider minor tick count since the axis draws one
				// less minor ticks than the number given.
				tickLine.setMinorTickCount(Math.max(rangeSlider.getMinorTickCount(),0) + 1);
				// TODO change slider API to Integer from Number
				//            if (slider.getLabelFormatter() != null)
				//                tickLine.setFormatTickLabel(slider.getLabelFormatter());
				//            tickLine.dataChanged();
				//				getChildren().clear();
				getChildren().addAll(tickLine);
			} 
			else {
				tickLine.setTickLabelsVisible(labelsVisible);
				tickLine.setTickMarkVisible(ticksVisible);
				tickLine.setMinorTickVisible(ticksVisible);
			}
		} 
		//		else  {
		//			getChildren().clear();
		//			getChildren().addAll(track, lowThumb);
		//			//           tickLine = null;
		//		}

		getSkinnable().requestLayout();
	}

	@Override protected void handleControlPropertyChanged(String p) {
		super.handleControlPropertyChanged(p);
		if ("ORIENTATION".equals(p)) { //$NON-NLS-1$
			orientation = getSkinnable().getOrientation();
			if (showTickMarks && tickLine != null) {
				tickLine.setSide(isHorizontal() ? Side.BOTTOM : Side.RIGHT);
			}
			getSkinnable().requestLayout();
		} else if ("MIN".equals(p) ) { //$NON-NLS-1$
			if (showTickMarks && tickLine != null) {
				tickLine.setLowerBound(getSkinnable().getMin());
			}
			getSkinnable().requestLayout();
		} else if ("MAX".equals(p)) { //$NON-NLS-1$
			if (showTickMarks && tickLine != null) {
				tickLine.setUpperBound(getSkinnable().getMax());
			}
			getSkinnable().requestLayout();
		} else if ("SHOW_TICK_MARKS".equals(p) || "SHOW_TICK_LABELS".equals(p)) { //$NON-NLS-1$ //$NON-NLS-2$
			setShowTickMarks(getSkinnable().isShowTickMarks(), getSkinnable().isShowTickLabels());
		}  else if ("MAJOR_TICK_UNIT".equals(p)) { //$NON-NLS-1$
			if (tickLine != null) {
				tickLine.setTickUnit(getSkinnable().getMajorTickUnit());
				getSkinnable().requestLayout();
			}
		} else if ("MINOR_TICK_COUNT".equals(p)) { //$NON-NLS-1$
			if (tickLine != null) {
				tickLine.setMinorTickCount(Math.max(getSkinnable().getMinorTickCount(),0) + 1);
				getSkinnable().requestLayout();
			}
		} else if ("LOW_VALUE".equals(p)) { //$NON-NLS-1$
			positionLowThumb();
			rangeBar.resizeRelocate(rangeStart, rangeBar.getLayoutY(), 
					rangeEnd - rangeStart, rangeBar.getHeight());
		} else if ("HIGH_VALUE".equals(p)) { //$NON-NLS-1$
			positionHighThumb();
			rangeBar.resize(rangeEnd-rangeStart, rangeBar.getHeight());
		} else if ("SHOW_TICK_MARKS".equals(p) || "SHOW_TICK_LABELS".equals(p)) { //$NON-NLS-1$ //$NON-NLS-2$
			if (!getChildren().contains(highThumb))
				getChildren().add(highThumb);
		}
		super.handleControlPropertyChanged(p);
	}

	/**
	 * Called when ever either min, max or lowValue changes, so lowthumb's layoutX, Y is recomputed.
	 */
	private void positionLowThumb() {
		RangeSlider s = getSkinnable();
		boolean horizontal = isHorizontal();
		double lx = (horizontal) ? trackStart + (((trackLength * ((s.getLowValue() - s.getMin()) /
				(s.getMax() - s.getMin()))) - thumbWidth/2)) : lowThumbPos;
		double ly = (horizontal) ? lowThumbPos :
			getSkinnable().getInsets().getTop() + trackLength - (trackLength * ((s.getLowValue() - s.getMin()) /
					(s.getMax() - s.getMin()))); //  - thumbHeight/2
		lowThumb.setLayoutX(lx);
		lowThumb.setLayoutY(ly);
		if (horizontal) rangeStart = lx + thumbWidth; else rangeEnd = ly;
	}

	/**
	 * Called when ever either min, max or highValue changes, so highthumb's layoutX, Y is recomputed.
	 */
	private void positionHighThumb() {
		RangeSlider slider = (RangeSlider) getSkinnable();
		boolean orientation = ((RangeSlider) getSkinnable()).getOrientation() == Orientation.HORIZONTAL;

		double thumbWidth = lowThumb.getWidth();
		double thumbHeight = lowThumb.getHeight();
		highThumb.resize(thumbWidth, thumbHeight);

		double pad = 0;//track.impl_getBackgroundFills() == null || track.impl_getBackgroundFills().length <= 0 ? 0.0D : track.impl_getBackgroundFills()[0].getTopLeftCornerRadius();
		double trackStart = orientation ? track.getLayoutX() : track.getLayoutY();
		trackStart += pad;
		double trackLength = orientation ? track.getWidth() : track.getHeight();
		trackLength -= 2 * pad;

		double x = orientation ? trackStart + (trackLength * ((slider.getHighValue() - slider.getMin()) / (slider.getMax() - slider.getMin())) - thumbWidth / 2D) : lowThumb.getLayoutX();
		double y = orientation ? lowThumb.getLayoutY() : (getSkinnable().getInsets().getTop() + trackLength) - trackLength * ((slider.getHighValue() - slider.getMin()) / (slider.getMax() - slider.getMin()));
		highThumb.setLayoutX(x);
		highThumb.setLayoutY(y);
		if (orientation) rangeEnd = x; else rangeStart = y + thumbWidth;
	}

	@Override protected void layoutChildren(final double x, final double y,
			final double w, final double h) {
		// resize thumb to preferred size
		thumbWidth = lowThumb.prefWidth(-1);
		thumbHeight = lowThumb.prefHeight(-1);
		lowThumb.resize(thumbWidth, thumbHeight);
		// we are assuming the is common radius's for all corners on the track
		double trackRadius = track.getBackground() == null ? 0 : track.getBackground().getFills().size() > 0 ?
				track.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius() : 0;

				if (isHorizontal()) {
					double tickLineHeight =  (showTickMarks) ? tickLine.prefHeight(-1) : 0;
					double trackHeight = track.prefHeight(-1);
					double trackAreaHeight = Math.max(trackHeight,thumbHeight);
					double totalHeightNeeded = trackAreaHeight  + ((showTickMarks) ? trackToTickGap+tickLineHeight : 0);
					double startY = y + ((h - totalHeightNeeded)/2); // center slider in available height vertically
					trackLength = w - thumbWidth;
					trackStart = x + (thumbWidth/2);
					double trackTop = (int)(startY + ((trackAreaHeight-trackHeight)/2));
					lowThumbPos = (int)(startY + ((trackAreaHeight-thumbHeight)/2));

					positionLowThumb();
					// layout track
					track.resizeRelocate(trackStart - trackRadius, trackTop , trackLength + trackRadius + trackRadius, trackHeight);
					positionHighThumb();
					// layout range bar
					rangeBar.resizeRelocate(rangeStart, trackTop, rangeEnd - rangeStart, trackHeight);
					// layout tick line
					if (showTickMarks) {
						tickLine.setLayoutX(trackStart);
						tickLine.setLayoutY(trackTop+trackHeight+trackToTickGap);
						tickLine.resize(trackLength, tickLineHeight);
						tickLine.requestAxisLayout();
					} else {
						if (tickLine != null) {
							tickLine.resize(0,0);
							tickLine.requestAxisLayout();
						}
						tickLine = null;
					}
				} else {
					double tickLineWidth = (showTickMarks) ? tickLine.prefWidth(-1) : 0;
					double trackWidth = track.prefWidth(-1);
					double trackAreaWidth = Math.max(trackWidth,thumbWidth);
					double totalWidthNeeded = trackAreaWidth  + ((showTickMarks) ? trackToTickGap+tickLineWidth : 0) ;
					double startX = x + ((w - totalWidthNeeded)/2); // center slider in available width horizontally
					trackLength = h - thumbHeight;
					trackStart = y + (thumbHeight/2);
					double trackLeft = (int)(startX + ((trackAreaWidth-trackWidth)/2));
					lowThumbPos = (int)(startX + ((trackAreaWidth-thumbWidth)/2));

					positionLowThumb();
					// layout track
					track.resizeRelocate(trackLeft, trackStart - trackRadius, trackWidth, trackLength + trackRadius + trackRadius);
					positionHighThumb();
					// layout range bar
					rangeBar.resizeRelocate(trackLeft, rangeStart, trackWidth, rangeEnd - rangeStart);
					// layout tick line
					if (showTickMarks) {
						tickLine.setLayoutX(trackLeft+trackWidth+trackToTickGap);
						tickLine.setLayoutY(trackStart);
						tickLine.resize(tickLineWidth, trackLength);
						tickLine.requestAxisLayout();
					} else {
						if (tickLine != null) {
							tickLine.resize(0,0);
							tickLine.requestAxisLayout();
						}
						tickLine = null;
					}
				}
	}

	private double minTrackLength() {
		return 2*lowThumb.prefWidth(-1);
	}

	@Override protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		if (isHorizontal()) {
			return (leftInset + minTrackLength() + lowThumb.minWidth(-1) + rightInset);
		} else {
			return (leftInset + lowThumb.prefWidth(-1) + rightInset);
		}
	}

	@Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		if (isHorizontal()) {
			return (topInset + lowThumb.prefHeight(-1) + bottomInset);
		} else {
			return (topInset + minTrackLength() + lowThumb.prefHeight(-1) + bottomInset);
		}
	}

	@Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		if (isHorizontal()) {
			if(showTickMarks) {
				return Math.max(140, tickLine.prefWidth(-1));
			} else {
				return 140;
			}
		} else {
			//return (padding.getLeft()) + Math.max(thumb.prefWidth(-1), track.prefWidth(-1)) + padding.getRight();
			return leftInset + Math.max(lowThumb.prefWidth(-1), track.prefWidth(-1)) +
					((showTickMarks) ? (trackToTickGap+tickLine.prefWidth(-1)) : 0) + rightInset;
		}
	}

	@Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		if (isHorizontal()) {
			return getSkinnable().getInsets().getTop() + Math.max(lowThumb.prefHeight(-1), track.prefHeight(-1)) +
					((showTickMarks) ? (trackToTickGap+tickLine.prefHeight(-1)) : 0)  + bottomInset;
		} else {
			if(showTickMarks) {
				return Math.max(140, tickLine.prefHeight(-1));
			} else {
				return 140;
			}
		}
	}

	@Override protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		if (isHorizontal()) {
			return Double.MAX_VALUE;
		} else {
			return getSkinnable().prefWidth(-1);
		}
	}

	@Override protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		if (isHorizontal()) {
			return getSkinnable().prefHeight(width);
		} else {
			return Double.MAX_VALUE;
		}
	}

	private boolean isHorizontal() {
		return orientation == null || orientation == Orientation.HORIZONTAL;
	}

	private static class ThumbPane extends StackPane {

		public void setFocus(boolean value) {
			setFocused(value);
		}
	}

	/**
	 * Set the colour of the track between the two thumbs. 
	 */
	public void setTrackColor(Color color){
		rangeBar.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
	}
}
