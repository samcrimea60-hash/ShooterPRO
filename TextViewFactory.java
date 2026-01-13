package com.shooterpro.app.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.shooterpro.app.utils.DisplayUtils;

public class TextViewFactory {
    
    public static TextView createTitle(Context context, String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(Color.parseColor("#FF4FD1C7"));
        textView.setTextSize(20);
        textView.setGravity(Gravity.CENTER);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, DisplayUtils.dpToPx(context, 16));
        textView.setLayoutParams(params);
        
        return textView;
    }
    
    public static TextView createSubtitle(Context context, String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(Color.parseColor("#FFE2E8F0"));
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, DisplayUtils.dpToPx(context, 24));
        textView.setLayoutParams(params);
        
        return textView;
    }
    
    public static TextView createInfoText(Context context, String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(Color.parseColor("#FFA0AEC0"));
        textView.setTextSize(14);
        textView.setGravity(Gravity.CENTER);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, DisplayUtils.dpToPx(context, 16));
        textView.setLayoutParams(params);
        
        return textView;
    }
    
    public static TextView createLabel(Context context, String text, int color) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(color);
        textView.setTextSize(14);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.weight = 0.6f;
        textView.setLayoutParams(params);
        
        return textView;
    }
    
    public static TextView createValue(Context context, String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(Color.parseColor("#FFE2E8F0"));
        textView.setTextSize(14);
        textView.setGravity(Gravity.END);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.weight = 0.4f;
        textView.setLayoutParams(params);
        
        return textView;
    }
    
    public static TextView createStatRow(Context context, String label, String value) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, DisplayUtils.dpToPx(context, 8), 0, DisplayUtils.dpToPx(context, 8));
        
        TextView labelView = createLabel(context, label, Color.parseColor("#FFA0AEC0"));
        TextView valueView = createValue(context, value);
        
        row.addView(labelView);
        row.addView(valueView);
        
        TextView container = new TextView(context);
        // Здесь просто возвращаем контейнер, в реальности нужен другой подход
        return labelView;
    }
}
