package com.shooterpro.app.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import com.shooterpro.app.utils.DisplayUtils;

public class ButtonFactory {

    public static Button createPrimaryButton(Context context, String text) {
        return createButton(context, text, Color.parseColor("#FF4FD1C7"), 16);
    }

    public static Button createSecondaryButton(Context context, String text) {
        return createButton(context, text, Color.parseColor("#FF9F7AEA"), 16);
    }

    public static Button createWarningButton(Context context, String text) {
        return createButton(context, text, Color.parseColor("#FFF56565"), 16);
    }

    public static Button createBackButton(Context context, String text) {
        return createButton(context, text, Color.parseColor("#FF4A5568"), 16);
    }

    public static Button createButton(Context context, String text, int color, float textSizeSp) {
        Button button = new Button(context);
        button.setText(text);
        button.setBackgroundColor(color);
        button.setTextColor(Color.WHITE);
        button.setTextSize(textSizeSp);
        button.setAllCaps(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            DisplayUtils.dpToPx(context, 52)
        );
        params.setMargins(0, 0, 0, DisplayUtils.dpToPx(context, 12));
        button.setLayoutParams(params);

        return button;
    }

    public static Button createSmallButton(Context context, String text, int color) {
        Button button = new Button(context);
        button.setText(text);
        button.setBackgroundColor(color);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setAllCaps(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            DisplayUtils.dpToPx(context, 45)
        );
        params.setMargins(0, 0, 0, DisplayUtils.dpToPx(context, 8));
        button.setLayoutParams(params);

        return button;
    }

    public static Button createNavButton(Context context, String text) {
        Button button = new Button(context);
        button.setText(text);
        button.setBackgroundColor(Color.parseColor("#FF2A3B5A"));
        button.setTextColor(Color.parseColor("#FFE2E8F0"));
        button.setTextSize(14);

        int size = DisplayUtils.dpToPx(context, 40);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        button.setLayoutParams(params);

        return button;
    }
}
