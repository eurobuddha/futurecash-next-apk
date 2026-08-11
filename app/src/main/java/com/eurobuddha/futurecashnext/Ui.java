package com.eurobuddha.futurecashnext;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Small view builders, so the screens read as content rather than as XML plumbing.
 *
 * <p>The colour constants are the app's risk vocabulary — green means the money is fine, orange
 * means the guardian is acting, red means something needs the user. They are used consistently and
 * nowhere decoratively.
 */
public final class Ui {

    public static final int BG       = Color.parseColor("#0A0A0F");
    public static final int CARD     = Color.parseColor("#14141C");
    public static final int ACCENT   = Color.parseColor("#F7931A");
    public static final int TEXT     = Color.parseColor("#E8E8F0");
    public static final int DIM      = Color.parseColor("#8A8A9A");
    public static final int OK       = Color.parseColor("#3DD68C");
    public static final int WARN     = Color.parseColor("#FFB020");
    public static final int DANGER   = Color.parseColor("#FF5C5C");

    private Ui() {}

    public static int dp(Context c, int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                c.getResources().getDisplayMetrics());
    }

    public static TextView text(Context c, String s, int color, int sizeSp, boolean bold) {
        final TextView t = new TextView(c);
        t.setText(s);
        t.setTextColor(color);
        t.setTextSize(sizeSp);
        if (bold) t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        return t;
    }

    public static LinearLayout column(Context c) {
        final LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public static LinearLayout row(Context c) {
        final LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    /** A card. `accent` tints the left edge so the risk state is readable at a glance. */
    public static LinearLayout card(Context c, int accent) {
        final LinearLayout l = column(c);
        final GradientDrawable bg = new GradientDrawable();
        bg.setColor(CARD);
        bg.setCornerRadius(dp(c, 12));
        if (accent != 0) bg.setStroke(dp(c, 1), accent);
        l.setBackground(bg);
        l.setPadding(dp(c, 14), dp(c, 12), dp(c, 14), dp(c, 12));
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(c, 10);
        l.setLayoutParams(lp);
        return l;
    }

    public static Button button(Context c, String label, int color, View.OnClickListener onClick) {
        final Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.BLACK);
        final GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(c, 10));
        b.setBackground(bg);
        b.setOnClickListener(onClick);
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(c, 8);
        lp.rightMargin = dp(c, 8);
        b.setLayoutParams(lp);
        return b;
    }

    public static Button ghost(Context c, String label, View.OnClickListener onClick) {
        final Button b = button(c, label, Color.TRANSPARENT, onClick);
        b.setTextColor(TEXT);
        final GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.TRANSPARENT);
        bg.setCornerRadius(dp(c, 10));
        bg.setStroke(dp(c, 1), DIM);
        b.setBackground(bg);
        return b;
    }

    public static EditText field(Context c, String hint, boolean numeric) {
        final EditText e = new EditText(c);
        e.setHint(hint);
        e.setHintTextColor(DIM);
        e.setTextColor(TEXT);
        e.setTextSize(14);
        e.setSingleLine(true);
        if (numeric) e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        final GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1C1C26"));
        bg.setCornerRadius(dp(c, 10));
        e.setBackground(bg);
        e.setPadding(dp(c, 12), dp(c, 10), dp(c, 12), dp(c, 10));
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(c, 8);
        e.setLayoutParams(lp);
        return e;
    }

    /** Shorten an address for display, keeping both ends so it stays recognisable. */
    public static String shortAddr(String s) {
        final String v = s == null ? "" : s;
        return v.length() > 18 ? v.substring(0, 10) + "…" + v.substring(v.length() - 6) : v;
    }

    public static String amount(String a) {
        try {
            final double d = Double.parseDouble(a);
            if (d == Math.floor(d) && !Double.isInfinite(d)) return String.valueOf((long) d);
            final String s = String.format(java.util.Locale.US, "%.4f", d);
            return s.replaceAll("0+$", "").replaceAll("\\.$", "");
        } catch (Exception e) { return a; }
    }

    /** "in ~3 h (block 12345)" — blocks are ~50s. */
    public static String maturesIn(long block, long tip) {
        final long blocks = Math.max(0, block - tip);
        final long mins = Math.round(blocks * 50 / 60.0);
        final String when = mins < 90 ? mins + " min"
                : mins < 1440 ? Math.round(mins / 60.0) + " h"
                : Math.round(mins / 1440.0) + " days";
        return "in ~" + when + " (block " + block + ")";
    }

    public static String timeAgo(long ms) {
        final long d = System.currentTimeMillis() - ms;
        if (d < 60000) return "just now";
        final long m = d / 60000;
        if (m < 60) return m + "m ago";
        return (m / 60) + "h ago";
    }
}
