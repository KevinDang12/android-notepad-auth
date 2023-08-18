package com.example.notepadauth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * A customized EditText View that fills up the entire screen, allowing the user to type anywhere.
 * Has a customized look with lines.
 */
public class NotePadArea extends androidx.appcompat.widget.AppCompatEditText {

    /** Color for the text lines */
    private Paint textLine;

    /** Color for the line border */
    private Paint redLine;

    /** Minimum number of lines to display on the notepad area */
    final private static int MIN_LINES = 10;

    /**
     * A customized EditText View that fills up the entire screen, allowing the user to type anywhere.
     * Has a customized look with lines.
     * @param context The context storing information about the app environment.
     */
    public NotePadArea(@NonNull Context context) {
        super(context);
        this.setUpPaint();
        this.fillScreen();
    }

    /**
     * A customized EditText View that fills up the entire screen, allowing the user to type anywhere.
     * Has a customized look with lines.
     * @param context The context storing information about the app environment.
     * @param attrs Attributes of the component.
     */
    public NotePadArea(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setUpPaint();
        this.fillScreen();
    }

    /**
     * A customized EditText View that fills up the entire screen, allowing the user to type anywhere.
     * Has a customized look with lines.
     * @param context The context storing information about the app environment.
     * @param attrs Attributes of the component.
     * @param defStyleAttr The style attribute.
     */
    public NotePadArea(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setUpPaint();
        this.fillScreen();
    }

    /**
     * Set ups paint for drawing lines on canvas.
     */
    private void setUpPaint() {
        this.textLine = new Paint();
        this.textLine.setStyle(Paint.Style.STROKE);
        int color = ContextCompat.getColor(super.getContext(), R.color.dark_grey);
        this.textLine.setColor(color);
        this.textLine.setStrokeWidth(1);

        this.redLine = new Paint();
        this.redLine.setStyle(Paint.Style.STROKE);
        int red = ContextCompat.getColor(super.getContext(), R.color.red);
        this.redLine.setColor(red);
        this.redLine.setStrokeWidth(1);
    }

    /**
     * Set up notepad area to fill screen
     */
    public void fillScreen() {
        this.setMinLines(MIN_LINES);
        StringBuilder lineToAdd = new StringBuilder();

        // if the notepad area has less than ten lines, add more lines until there are ten lines
        int numOfLines = this.getLineCount();

        // if numOfLines on the notepad area is equal to 0, add starting lines on the notepad area
        if (numOfLines != 0) {
            if (numOfLines < MIN_LINES) {
                for (int i = numOfLines; i < MIN_LINES; i++) {
                    lineToAdd.append("\n");
                }
            }
            this.append(lineToAdd.toString());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        // The left edge of this component
        int startX = this.getLeft();
        // The right edge of this component
        int endX = this.getRight();

        int numLines = this.getLineCount();
        int lineHeight = this.getLineHeight();
        int numOfLinesToDraw = numLines + 1;

        int startY = 0;
        int paddingTop = this.getPaddingTop();

        for (int i = 0; i < numOfLinesToDraw; i++) {
            canvas.drawLine(startX, startY + paddingTop + (i*lineHeight), endX, startY + paddingTop + (i*lineHeight), this.textLine);
        }

        int paddingLeftX = this.getPaddingLeft();
        canvas.drawLine(paddingLeftX, getTop(), paddingLeftX, this.getHeight(), this.redLine);
    }
}
