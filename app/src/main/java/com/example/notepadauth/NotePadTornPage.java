package com.example.notepadauth;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Add a torn page image at the bottom of the notepad page
 */
public class NotePadTornPage extends androidx.appcompat.widget.AppCompatImageView {

    /** Color for the text lines */
    private Paint textLine;

    /** Color for the line border */
    private Paint redLine;

    /** Starting length of drawing the text lines */
    private int length = 0;

    /** Height between each text lines */
    private int lineHeight;

    /**
     * Draw the lines across the instance of the torn notepad page
     * @param context
     */
    public NotePadTornPage(@NonNull Context context) {
        super(context);
        this.setUpPaint();
    }

    /**
     * Draw the lines across the instance of the torn notepad page
     * @param context
     * @param attrs
     */
    public NotePadTornPage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setUpPaint();
    }

    /**
     * Draw the lines across the instance of the torn notepad page
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public NotePadTornPage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setUpPaint();
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
     * Set the height of the line on the notepad
     * @param lineHeight The height of the line
     */
    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Bitmap bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas bitmapCanvas = new Canvas(bitmap);
        super.onDraw(bitmapCanvas);

        float dip = 50f;
        Resources r = getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );

        int x = (int) px;
        int y = 0;

        int pixel = bitmap.getPixel(x, y);
        while (pixel == Color.WHITE) {
            y++;
            pixel = bitmap.getPixel(x, y);
        }

        if (y != 0) {
            this.length = y;
        }

        canvas.drawLine(x, 0, x, this.length, this.redLine);

        int height = this.getHeight();
        int width = bitmap.getWidth();

        int yHeight = 0;
        int xPixel;

        // for every iteration of the line height of the component
        for (int i = 1; yHeight < height; i++) {

            yHeight = lineHeight * i;
            int currentX = 0;
            int startX = 0;

            // break out of loop if the height exceeds the component height
            if (yHeight >= height) {
                break;
            }

            xPixel = bitmap.getPixel(startX, yHeight);

            // Trace component is set to TRUE if starting pixel is white, else FALSE
            boolean tracingLine = xPixel == Color.WHITE;

            // While starting and ending x does not exceed the width of the screen
            while (currentX < width) {
                xPixel = bitmap.getPixel(currentX, yHeight);

                // find the ending x coordinate for the line
                if (tracingLine) {

                    // increment if the current pixel is a white color
                    if (xPixel == Color.WHITE) {
                        currentX++;

                    }

                    // draw line across the screen once it reaches the end of it
                    if (currentX == width - 1) {

                        canvas.drawLine(startX, yHeight, currentX, yHeight, this.textLine);
                        break;

                    }

                    // draw line to the end of the x coordinate if the pixel is no longer white
                    if (xPixel != Color.WHITE) {
                        canvas.drawLine(startX, yHeight, currentX, yHeight, this.textLine);
                        tracingLine = false;
                    }
                }

                // find the next starting point of the line
                else {

                    // Find the starting point if the pixel is white
                    if (xPixel != Color.WHITE) {
                        currentX++;

                        // assign the starting x coordinate for the line
                    } else {
                        startX = currentX;
                        tracingLine = true;
                    }
                }
            }
        }
    }
}
