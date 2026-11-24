package com.example.nhom_15_dulichtphcm.ui.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.nhom_15_dulichtphcm.R;

/**
 * Pure Java helper creating styled selectable pill chips inside HorizontalScrollView LinearLayouts.
 */
public final class ChipViewFactory {

    private ChipViewFactory() {}

    public static TextView createChip(
            Context context,
            String label,
            boolean selected,
            int optionalLeftIconRes,
            Runnable onClick
    ) {
        return createStyledChip(
                context,
                label,
                selected,
                R.drawable.bg_chip_selected,
                optionalLeftIconRes,
                onClick
        );
    }

    public static TextView createTealChip(
            Context context,
            String label,
            boolean selected,
            Runnable onClick
    ) {
        return createStyledChip(
                context,
                label,
                selected,
                R.drawable.bg_chip_teal_selected,
                0,
                onClick
        );
    }

    public static TextView createSkyChip(
            Context context,
            String label,
            boolean selected,
            Runnable onClick
    ) {
        return createStyledChip(
                context,
                label,
                selected,
                R.drawable.bg_chip_sky_selected,
                0,
                onClick
        );
    }

    public static TextView createFxChip(
            Context context,
            String label,
            boolean selected,
            Runnable onClick
    ) {
        return createStyledChip(
                context,
                label,
                selected,
                R.drawable.bg_chip_fx_selected,
                0,
                onClick
        );
    }

    private static TextView createStyledChip(
            Context context,
            String label,
            boolean selected,
            int selectedDrawableRes,
            int optionalLeftIconRes,
            Runnable onClick
    ) {
        float d = context.getResources().getDisplayMetrics().density;
        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setTextSize(12.5f);
        tv.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        tv.setBackgroundResource(selected ? selectedDrawableRes : R.drawable.bg_chip_unselected);
        tv.setTextColor(selected ? Color.WHITE : ContextCompat.getColor(context, R.color.text_primary));

        int padH = (int) (13 * d);
        int padV = (int) (7.5f * d);
        tv.setPadding(padH, padV, padH, padV);

        if (optionalLeftIconRes != 0) {
            Drawable icon = ContextCompat.getDrawable(context, optionalLeftIconRes);
            if (icon != null) {
                int size = (int) (16 * d);
                icon.setBounds(0, 0, size, size);
                tv.setCompoundDrawables(icon, null, null, null);
                tv.setCompoundDrawablePadding((int) (6 * d));
            }
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMarginEnd((int) (7 * d));
        tv.setLayoutParams(lp);
        tv.setOnClickListener(v -> {
            if (onClick != null) onClick.run();
        });
        return tv;
    }
}
