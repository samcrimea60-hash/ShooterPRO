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

public class LineChartView extends View {
    
    private List<Float> dataPoints = new ArrayList<>();
    private String title = "";
    private Paint linePaint, pointPaint, gridPaint, textPaint;
    private float padding = 50;
    private float pointRadius = 8;
    
    public LineChartView(Context context) {
        super(context);
        init();
    }
    
    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        // Настройка кистей
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#FF4FD1C7"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4);
        
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.parseColor("#FF4FD1C7"));
        pointPaint.setStyle(Paint.Style.FILL);
        
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#FF4A5568"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#FFE2E8F0"));
        textPaint.setTextSize(28);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }
    
    public void setData(List<Float> data) {
        this.dataPoints = new ArrayList<>(data);
        invalidate();
    }
    
    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (dataPoints.isEmpty()) {
            drawNoData(canvas);
            return;
        }
        
        drawGrid(canvas);
        drawLine(canvas);
        drawPoints(canvas);
        drawTitle(canvas);
    }
    
    private void drawNoData(Canvas canvas) {
        textPaint.setTextSize(36);
        canvas.drawText("Нет данных для графика", 
                       getWidth() / 2, getHeight() / 2, textPaint);
    }
    
    private void drawGrid(Canvas canvas) {
        float chartWidth = getWidth() - 2 * padding;
        float chartHeight = getHeight() - 2 * padding;
        
        // Вертикальные линии
        int verticalLines = Math.min(10, dataPoints.size());
        float xStep = chartWidth / (verticalLines - 1);
        
        for (int i = 0; i < verticalLines; i++) {
            float x = padding + i * xStep;
            canvas.drawLine(x, padding, x, padding + chartHeight, gridPaint);
        }
        
        // Горизонтальные линии (шкала баллов)
        int horizontalLines = 6;
        float yStep = chartHeight / (horizontalLines - 1);
        
        for (int i = 0; i < horizontalLines; i++) {
            float y = padding + i * yStep;
            canvas.drawLine(padding, y, padding + chartWidth, y, gridPaint);
            
            // Подписи значений
            float value = 10.9f - (i * (10.9f / (horizontalLines - 1)));
            textPaint.setTextSize(24);
            canvas.drawText(String.format("%.1f", value), 
                          padding - 30, y + 10, textPaint);
        }
    }
    
    private void drawLine(Canvas canvas) {
        if (dataPoints.size() < 2) return;
        
        float chartWidth = getWidth() - 2 * padding;
        float chartHeight = getHeight() - 2 * padding;
        
        float maxValue = getMaxValue();
        float minValue = getMinValue();
        float valueRange = maxValue - minValue;
        
        if (valueRange == 0) valueRange = 1;
        
        Path path = new Path();
        float xStep = chartWidth / (dataPoints.size() - 1);
        
        for (int i = 0; i < dataPoints.size(); i++) {
            float value = dataPoints.get(i);
            float normalized = (value - minValue) / valueRange;
            float x = padding + i * xStep;
            float y = padding + chartHeight - (normalized * chartHeight);
            
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        
        canvas.drawPath(path, linePaint);
    }
    
    private void drawPoints(Canvas canvas) {
        float chartWidth = getWidth() - 2 * padding;
        float chartHeight = getHeight() - 2 * padding;
        
        float maxValue = getMaxValue();
        float minValue = getMinValue();
        float valueRange = maxValue - minValue;
        
        if (valueRange == 0) valueRange = 1;
        
        float xStep = chartWidth / (dataPoints.size() - 1);
        
        for (int i = 0; i < dataPoints.size(); i++) {
            float value = dataPoints.get(i);
            float normalized = (value - minValue) / valueRange;
            float x = padding + i * xStep;
            float y = padding + chartHeight - (normalized * chartHeight);
            
            // Рисуем точку
            canvas.drawCircle(x, y, pointRadius, pointPaint);
            
            // Подпись значения
            textPaint.setTextSize(20);
            canvas.drawText(String.format("%.1f", value), x, y - 15, textPaint);
        }
    }
    
    private void drawTitle(Canvas canvas) {
        if (!title.isEmpty()) {
            textPaint.setTextSize(28);
            canvas.drawText(title, getWidth() / 2, padding - 20, textPaint);
        }
    }
    
    private float getMaxValue() {
        float max = 0;
        for (float value : dataPoints) {
            if (value > max) max = value;
        }
        return Math.max(max, 10.9f);
    }
    
    private float getMinValue() {
        float min = 11.0f;
        for (float value : dataPoints) {
            if (value < min) min = value;
        }
        return Math.min(min, 0);
    }
}
