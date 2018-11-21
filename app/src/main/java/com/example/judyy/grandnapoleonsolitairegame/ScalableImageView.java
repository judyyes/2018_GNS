package com.example.judyy.grandnapoleonsolitairegame;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * A customized ImageView class that scale with screen size, designed to display card image.
 */

public class ScalableImageView extends AppCompatImageView {

    public ScalableImageView(Context context) {
        super(context);
    }

    public ScalableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = (int) ((double) getContext().getResources().getDisplayMetrics().widthPixels/13.5);
        int measuredHeight = (int) ((double) getContext().getResources().getDisplayMetrics().heightPixels/5.5);
        setMeasuredDimension(measuredWidth,measuredHeight);
    }
}
