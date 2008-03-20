package org.openintents.widget;

import java.util.Map;

import org.openintents.R;

import android.content.Context;
import android.content.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Basic slider.
 * 
 * @author Peli
 *
 */
public class Slider extends View {

	/** Minimum slider value */
	public int min;
	
	/** Maximum slider value. */
	public int max;
	
	/** Current slider position */
	public int pos;
	
	/** Default widget width */
	public int defaultWidth;
	
	/** Default widget height */
	public int defaultHeight;
	
	/** Width of marker */
	public int markerWidth;
	
	/** Background drawable element */
	public Drawable background;
	
	/** Knob drawable element */
	public Drawable knob;
	
	
	private Paint mPaint; 
	private Rect mRect;
	
	/**
	 * State of touch.
	 */
	public int mTouchState;
	
	public static final int STATE_RELEASED = 0;
	public static final int STATE_TOUCHING = 1;
	
	/** Offset within marker to position clicked. */
	private int mDeltaX;
	
	/** Last x (for determining invalidating area). */
	private int mOldX;
	
	private OnPositionChangedListener mPositionListener;

	public Slider(Context context) {
		super(context);
		initSlider();
	}
	
	/**
     * Construct object, initializing with any attributes we understand from a
     * layout file. 
     * 
     * These attributes are defined in res/values/attrs.xml .
     * 
     * @see android.view.View#View(android.content.Context, android.util.AttributeSet, java.util.Map)
     */
    public Slider(Context context, AttributeSet attrs, Map inflateParams) {
        super(context, attrs, inflateParams);

        initSlider();
        
        Resources.StyledAttributes a = context.obtainStyledAttributes(attrs,
                R.styleable.Slider);

        min = a.getInt(R.styleable.Slider_min, 0);
        max = a.getInt(R.styleable.Slider_max, 100);
        pos = a.getInt(R.styleable.Slider_pos, 0);
        
        background = null;
        knob = null;
        
        a.recycle();
    }
	
	/**
	 * Initializes variables.
	 */
	void initSlider() {

		min = 0;
		max = 100;
		pos = 0;
		
		defaultWidth = 200;
		defaultHeight = 50;
		
		markerWidth = 20;
		
		mTouchState = STATE_RELEASED;
		
		mPositionListener = null;
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
         
        mRect = new Rect();
        
        setFocusable(true);
		
	}
	
	/**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }
	
    /**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Default width:
            result = defaultWidth;
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * Determines the height of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Default height
            result = defaultHeight;
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

	/** 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		// Get widget dimensions
		/*
		Rect r = canvas.getClipBounds();
		int width = r.width();
		int height = r.height();
		*/
		int width = getWidth();
		int height = getHeight();
		
		int verticalcenter = height / 2;
		int viewpos = (width - markerWidth) * (pos - min) / (max - min);
		
		if (background != null) {
			// show drawable
			mRect.set(0, 0, width, height);
			background.setBounds(mRect);
			background.draw(canvas);
		} else {
			// Draw a background line
			int lineheight = height/4;
			mPaint.setColor(Color.BLUE);
			mPaint.setStrokeWidth(lineheight);
			canvas.drawLine(0, verticalcenter, width, verticalcenter, mPaint);
		}		
		
		if (knob != null) {
			// show drawable
			// show drawable
			mRect.set(viewpos, 0, viewpos + markerWidth, height);
			knob.setBounds(mRect);
			knob.draw(canvas);
		} else {
			// Draw current marker position
			mPaint.setColor(Color.GREEN);
			canvas.drawCircle(viewpos + markerWidth/2, verticalcenter, markerWidth/2, mPaint);
		}		
	}
	
	/**
	 * Sets new position of slider.
	 * @param pos new position
	 */
	public void setPosition(int pos) {
		if (mTouchState == STATE_TOUCHING) {
			// Currently user is interacting, so we
			// ignore this message.
			return;
		}
		int posOld = this.pos;
		this.pos = pos;
		
		// Invalidate only the region that changed
		int width = getWidth();
		int height = getHeight();
		int viewposOld = (width - markerWidth) * (posOld - min) / (max - min);
		int viewpos = (width - markerWidth) * (pos - min) / (max - min);
		
		int invalLeft = Math.min(viewposOld, viewpos);
		int invalRight = Math.max(viewposOld, viewpos) + markerWidth;
		invalidate(invalLeft, 0, invalRight, height);
	}
	
	/** Set background graphics. */
	public void setBackground(Drawable background)
	{
		this.background = background;
		if (background != null) {
			defaultWidth = background.getIntrinsicWidth();
			if (defaultWidth < 1) defaultWidth = 20;
			
			defaultHeight = background.getIntrinsicWidth();
			if (defaultHeight < 1) defaultHeight = 20;
		}
	}

	/** Set background graphics. */
	public void setKnob(Drawable knob)
	{
		this.knob = knob;
		if (knob != null) {
			markerWidth = knob.getIntrinsicWidth();
			if (markerWidth < 1) markerWidth = 20;
		}
	}

	/**
	 * Handle touch events.
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent me) {
		int action = me.getAction();
		int width = getWidth();
		int height = getHeight();
		int oldPos = pos;
		
		float x = me.getX();
		// float y = me.getY();
		
		boolean updatePosition = false;
		boolean changeComplete = false;
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mTouchState = STATE_TOUCHING;
			int viewpos = (width - markerWidth) * (pos - min) / (max - min);
			
			if ((x >= viewpos) 
					&& (x <= viewpos + markerWidth)) {
				// Click within the marker.
				mDeltaX = (int)x - viewpos;
				mOldX = (int) x;
			} else {
				// Click outside marker:
				// Place marker to new position:
				// (Assume we are in center of marker)
				mDeltaX = markerWidth / 2;
				updatePosition = true;
				mOldX = viewpos;
				
			}
			break;
		case MotionEvent.ACTION_MOVE:
			updatePosition = true;
			break;
		case MotionEvent.ACTION_UP:
			mTouchState = STATE_RELEASED;
			updatePosition = true;
			changeComplete = true;
			break;
		default:
			break;
		}
		
		if (updatePosition) {
			pos = min + ((int) x - mDeltaX) * (max - min) / (width - markerWidth);
			if (pos < min) {
				pos = min;
			}
			if (pos > max) {
				pos = max;
			}
			if (pos != oldPos) {
				mPositionListener.onPositionChanged(this, oldPos, pos);
	
				// Only invalidate changes:
				int invalLeft = Math.min((int) x, mOldX) - mDeltaX;
				int invalRight = Math.max((int) x, mOldX) - mDeltaX + markerWidth;
				invalidate(invalLeft, 0, invalRight, height);
				
				mOldX = (int) x;
			}
		}
		
		if (changeComplete) {
			mPositionListener.onPositionChangeCompleted();
		}
		
		//return super.onTouchEvent(me);
		return true;
	}

	public void setOnPositionChangedListener(OnPositionChangedListener positionListener) {
		mPositionListener = positionListener;
	}

	/** 
	 * Interface for notifications of position change of slider. 
	 * 
	 */
	public static interface OnPositionChangedListener {
		
		/**
		 * This method is called when the user changed the position of the slider.
		 * 
		 * This works in touch mode, by dragging the slider with the finger.
		 */
		void onPositionChanged(Slider slider, int oldPosition, int newPosition);
		
		/**
		 * This method is called when the user released the slider.
		 */
		void onPositionChangeCompleted();
	}
	
	/////////////
	
	
	
}
