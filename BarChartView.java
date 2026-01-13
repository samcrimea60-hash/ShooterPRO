package com.shooterpro.app.ui.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class BarChartView extends View {
    
    private List<BarData> data = new ArrayList<>();
    private String title = "";
    private Paint barPaint, textPaint, gridPaint;
    private float padding = 50;
    private float barWidth = 40;
    
    public static class BarData {
        public String label;
        public float value;
        public int color;
        
        public BarData(String label, float value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }
    
    public BarChartView(Context context) {
        super(context);
        init();
    }
    
    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);
        
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#FF4A5568"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#FFE2E8F0"));
        textPaint.setTextSize(24);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }
    
    public void setData(List<BarData> data) {
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
            drawNoData(canvas);
            return;
        }
        
        drawGrid(canvas);
        drawBars(canvas);
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
        
        // Горизонтальные линии
        int lines = 6;
        float yStep = chartHeight / (lines - 1);
        
        for (int i = 0; i < lines; i++) {
            float y = padding + i * yStep;
            canvas.drawLine(padding, y, padding + chartWidth, y, gridPaint);
        }
    }
    
    private void drawBars(Canvas canvas) {
        float chartWidth = getWidth() - 2 * padding;
        float chartHeight = getHeight() - 2 * padding;
        
        float maxValue = getMaxValue();
        if (maxValue == 0) maxValue = 1;
        
        float barSpacing = (chartWidth - (data.size() * barWidth)) / (data.size() + 1);
        
        for (int i = 0; i < data.size(); i++) {
            BarData barData = data.get(i);
            
            float normalizedHeight = (barData.value / maxValue) * chartHeight;
            float x = padding + barSpacing + i * (barWidth + barSpacing);
            float y = padding + chartHeight - normalizedHeight;
            
            // Рисуем столбик
            barPaint.setColor(barData.color);
            RectF rect = new RectF(x, y, x + barWidth, padding + chartHeight);
            canvas.drawRect(rect, barPaint);
            
            // Подпись значения
            textPaint.setTextSize(20);
            canvas.drawText(String.format("%.1f", barData.value), 
                          x + barWidth / 2, y - 10, textPaint);
            
            // Подпись метки
            textPaint.setTextSize(18);
            canvas.drawText(barData.label, 
                          x + barWidth / 2, padding + chartHeight + 30, textPaint);
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
        for (BarData barData : data) {
            if (barData.value > max) max = barData.value;
        }
        return max;
    }
}
