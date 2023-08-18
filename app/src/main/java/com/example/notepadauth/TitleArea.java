package com.example.notepadauth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

public class TitleArea extends androidx.appcompat.widget.AppCompatEditText {

    private Paint redLine;

    public TitleArea(@NonNull Context context) {
        super(context);
        this.setUpPaint();
    }

    public TitleArea(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setUpPaint();
    }

    public TitleArea(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setUpPaint();
    }

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
