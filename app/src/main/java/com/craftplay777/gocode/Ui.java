package com.craftplay777.gocode;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Ui {

    static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    static final int BG = 0xFF0F1115;
    static final int BAR = 0xFF161920;
    static final int CARD = 0xFF1A1D24;
    static final int CARD_OFF = 0xFF13151A;
    static final int TEXT = 0xFFE8E8E8;
    static final int SUB = 0xFF9AA0AA;
    static final int GREEN = 0xFF4EE05A;
    static final int ACCENT = 0xFF3B82F6;
    static final int TERM_BG = 0xFF000000;

    // Convierte dp a píxeles
    static int dp(Context c, int v) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics()));
    }

    static GradientDrawable box(Context c, int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    static LinearLayout col(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    static TextView text(Context c, CharSequence s, int sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    // Agrega una vista con margen arriba
    static void add(LinearLayout parent, View v, int topDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, WRAP);
        lp.topMargin = dp(parent.getContext(), topDp);
        parent.addView(v, lp);
    }

    static LinearLayout card(Context c, boolean enabled) {
        LinearLayout l = col(c);
        int p = dp(c, 14);
        l.setPadding(p, p, p, p);
        l.setBackground(box(c, enabled ? CARD : CARD_OFF, 12));
        return l;
    }

    static TextView button(Context c, String label, boolean primary) {
        TextView t = text(c, label, 15, primary ? 0xFFFFFFFF : TEXT, true);
        t.setGravity(Gravity.CENTER);
        int ph = dp(c, 14);
        int pv = dp(c, 12);
        t.setPadding(ph, pv, ph, pv);
        t.setBackground(box(c, primary ? ACCENT : CARD, 10));
        return t;
    }

    // Barra de progreso simple
    static View bar(Context c, int done, int total, int color) {
        LinearLayout track = new LinearLayout(c);
        track.setBackground(box(c, 0xFF2A2E38, 3));
        track.setWeightSum(Math.max(total, 1));
        View fill = new View(c);
        fill.setBackground(box(c, color, 3));
        track.addView(fill, new LinearLayout.LayoutParams(0, MATCH, done));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, dp(c, 6));
        lp.topMargin = dp(c, 10);
        track.setLayoutParams(lp);
        return track;
    }

    // Texto con `código` en línea resaltado
    static CharSequence rich(String s) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        int i = 0;
        while (i < s.length()) {
            int a = s.indexOf('`', i);
            if (a < 0) {
                sb.append(s.substring(i));
                break;
            }
            int b = s.indexOf('`', a + 1);
            if (b < 0) {
                sb.append(s.substring(i));
                break;
            }
            sb.append(s.substring(i, a));
            int start = sb.length();
            sb.append(s.substring(a + 1, b));
            int end = sb.length();
            sb.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new ForegroundColorSpan(0xFFFFC66D), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new BackgroundColorSpan(0xFF262A33), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            i = b + 1;
        }
        return sb;
    }

    // Terminal: las líneas que empiezan con "$ " se pintan como comandos
    static View terminal(Context c, String term, int sp) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        String[] lines = term.split("\n", -1);
        for (int k = 0; k < lines.length; k++) {
            String l = lines[k];
            int start = sb.length();
            sb.append(l);
            if (l.startsWith("$ ")) {
                sb.setSpan(new ForegroundColorSpan(GREEN), start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new ForegroundColorSpan(0xFFFFFFFF), start + 1, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (k < lines.length - 1) sb.append('\n');
        }

        LinearLayout box = col(c);
        box.setBackground(box(c, TERM_BG, 10));

        TextView head = text(c, "●  ●  ●   terminal", 11, 0xFF6B7280, false);
        head.setPadding(dp(c, 12), dp(c, 8), dp(c, 12), dp(c, 4));
        box.addView(head);

        TextView tv = new TextView(c);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setTextColor(0xFFBFC7D5);
        tv.setText(sb);
        tv.setHorizontallyScrolling(true);
        tv.setTextIsSelectable(true);
        tv.setPadding(dp(c, 12), dp(c, 4), dp(c, 12), dp(c, 12));

        HorizontalScrollView hs = new HorizontalScrollView(c);
        hs.setHorizontalScrollBarEnabled(false);
        hs.addView(tv);
        box.addView(hs, new LinearLayout.LayoutParams(MATCH, WRAP));
        return box;
    }
}
