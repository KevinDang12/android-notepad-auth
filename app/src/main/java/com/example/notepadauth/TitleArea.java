package com.example.notepadauth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class TitleArea extends androidx.appcompat.widget.AppCompatEditText {

    /** Color for the line border */
    private Paint redLine;

    /**
     * Draw the red border line for the instance of the Title Area
     * @param context
     */
    public TitleArea(@NonNull Context context) {
        super(context);
        this.setUpPaint();
    }

    /**
     * Draw the red border line for the instance of the Title Area
     * @param context
     * @param attrs
     */
    public TitleArea(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setUpPaint();
    }

    /**
     * Draw the red border line for the instance of the Title Area
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public TitleArea(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setUpPaint();
    }

    /**
     * Set ups paint for drawing lines on canvas
     */
    private void setUpPaint() {
        this.redLine = new Paint();
        this.redLine.setStyle(Paint.Style.STROKE);
        int red = ContextCompat.getColor(super.getContext(), R.color.red);
        this.redLine.setColor(red);
        this.redLine.setStrokeWidth(1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int paddingLeftX = this.getPaddingLeft();
        canvas.drawLine(paddingLeftX, getTop(), paddingLeftX, this.getHeight(), this.redLine);
    }
}
