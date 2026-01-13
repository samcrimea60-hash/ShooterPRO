package com.shooterpro.app.ui.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SimpleChartView extends View {
    
    private List<Float> data = new ArrayList<>();
    private String title = "";
    private Paint paint = new Paint();
    private float padding = 50;
    
    public SimpleChartView(Context context) {
        super(context);
        init();
    }
    
    public SimpleChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }
    
    public void setData(List<Float> data) {
        this.data = new ArrayList<>(data);
        invalidate();
    }
    
    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (data.isEmpty()) {
            paint.setTextSize(36);
            paint.setColor(Color.WHITE);
            canvas.drawText("Нет данных", getWidth() / 2, getHeight() / 2, paint);
            return;
        }
        
        drawBackground(canvas);
        drawChart(canvas);
        drawTitle(canvas);
    }
    
    private void drawBackground(Canvas canvas) {
        paint.setColor(Color.parseColor("#FF2A3B5A"));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }
    
    private void drawChart(Canvas canvas) {
        if (data.size() < 2) return;
        
        float maxValue = getMaxValue();
        if (maxValue == 0) maxValue = 1;
        
        float chartWidth = getWidth() - 2 * padding;
        float chartHeight = getHeight() - 2 * padding;
        float xStep = chartWidth / (data.size() - 1);
        
        // Рисуем линию
        paint.setColor(Color.parseColor("#FF4FD1C7"));
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);
        
        Path path = new Path();
        
        for (int i = 0; i < data.size(); i++) {
            float value = data.get(i);
            float normalized = value / maxValue;
            float x = padding + i * xStep;
            float y = padding + chartHeight - (normalized * chartHeight);
            
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
            
            // Точки
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(x, y, 8, paint);
            paint.setStyle(Paint.Style.STROKE);
        }
        
        canvas.drawPath(path, paint);
        
        // Подписи
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(20);
        paint.setColor(Color.WHITE);
        
        for (int i = 0; i < data.size(); i++) {
            float value = data.get(i);
            float x = padding + i * xStep;
            float y = padding + chartHeight - ((value / maxValue) * chartHeight) - 15;
            canvas.drawText(String.format("%.1f", value), x - 15, y, paint);
        }
    }
    
    private void drawTitle(Canvas canvas) {
        if (!title.isEmpty()) {
            paint.setTextSize(24);
            paint.setColor(Color.parseColor("#FF4FD1C7"));
            canvas.drawText(title, getWidth() / 2, padding - 10, paint);
        }
    }
    
    private float getMaxValue() {
        float max = 0;
        for (float value : data) {
            if (value > max) max = value;
        }
        return Math.max(max, 10.9f);
    }
}
