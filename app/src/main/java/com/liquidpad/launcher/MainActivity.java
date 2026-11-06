package com.liquidpad.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.text.TextWatcher;
import android.text.Editable;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    PackageManager pm;
    FrameLayout root;
    int white = Color.WHITE;
    int glass = 0x66FFFFFF;

    int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density + 0.5f);
    }

    GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        pm = getPackageManager();
        getWindow().setStatusBarColor(0xFF426EBA);
        getWindow().setNavigationBarColor(0xFF426EBA);
        showHome();
    }

    List<ResolveInfo> apps() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> result = pm.queryIntentActivities(i, 0);
        Collections.sort(result, new Comparator<ResolveInfo>() {
            public int compare(ResolveInfo a, ResolveInfo b) {
                return String.CASE_INSENSITIVE_ORDER.compare(
                    a.loadLabel(pm).toString(),
                    b.loadLabel(pm).toString()
                );
            }
        });
        return result;
    }

    void launch(ResolveInfo info) {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        i.setClassName(
            info.activityInfo.packageName,
            info.activityInfo.name
        );
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open app",
                Toast.LENGTH_SHORT).show();
        }
    }

    TextView text(String value, int size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(white);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }

    void addIcon(ViewGroup parent, ResolveInfo app, boolean dock) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        box.setPadding(dp(3), dp(6), dp(3), dp(4));

        ImageView image = new ImageView(this);
        image.setImageDrawable(app.loadIcon(pm));
        int iconSize = dp(dock ? 54 : 60);

        box.addView(image, new LinearLayout.LayoutParams(
            iconSize, iconSize
        ));

        if (!dock) {
            TextView label = text(
                app.loadLabel(pm).toString(), 12, false
            );
            label.setGravity(Gravity.CENTER);
            label.setSingleLine(true);
            label.setEllipsize(
                android.text.TextUtils.TruncateAt.END
            );
            label.setShadowLayer(3, 0, 1, 0x66000000);

            LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(dp(94), dp(25));
            lp.topMargin = dp(5);
            box.addView(label, lp);
        }

        parent.addView(box, new ViewGroup.LayoutParams(
            dp(dock ? 76 : 105),
            dp(dock ? 72 : 108)
        ));

        box.setOnClickListener(v -> launch(app));
    }

    void showHome() {
        root = new FrameLayout(this);

        GradientDrawable wallpaper = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{
                0xFF164DAD,
                0xFF4287D8,
                0xFF80A6EA,
                0xFFB8A4E8
            }
        );
        root.setBackground(wallpaper);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(24), dp(20), dp(24), dp(14));

        root.addView(page, new FrameLayout.LayoutParams(
            -1, -1
        ));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout clockPanel = new LinearLayout(this);
        clockPanel.setOrientation(LinearLayout.VERTICAL);
        clockPanel.setPadding(
            dp(20), dp(14), dp(20), dp(14)
        );
        clockPanel.setBackground(shape(0x33FFFFFF, 28));

        Date now = new Date();
        String time = new SimpleDateFormat(
            "h:mm", Locale.getDefault()
        ).format(now);
        String date = new SimpleDateFormat(
            "EEEE, d MMMM", Locale.getDefault()
        ).format(now);

        TextView clock = text(time, 43, true);
        TextView day = text(date, 16, false);
        clockPanel.addView(clock);
        clockPanel.addView(day);

        top.addView(clockPanel);

        TextView search = text("⌕  Search", 17, false);
        search.setGravity(Gravity.CENTER);
        search.setPadding(
            dp(24), dp(17), dp(24), dp(17)
        );
        search.setBackground(shape(0x55FFFFFF, 30));

        LinearLayout.LayoutParams searchParams =
            new LinearLayout.LayoutParams(-2, -2);
        searchParams.leftMargin = dp(20);
        top.addView(search, searchParams);

        search.setOnClickListener(v -> showSearch());

        page.addView(top);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(false);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(
            getResources().getConfiguration()
                .orientation ==
                android.content.res.Configuration
                    .ORIENTATION_LANDSCAPE ? 8 : 5
        );
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        grid.setPadding(0, dp(24), 0, dp(15));

        List<ResolveInfo> list = apps();
        int count = Math.min(list.size(), 32);

        for (int n = 0; n < count; n++) {
            addIcon(grid, list.get(n), false);
        }

        scroll.addView(grid);
        page.addView(scroll,
            new LinearLayout.LayoutParams(-1, 0, 1));

        HorizontalScrollView dockScroll =
            new HorizontalScrollView(this);
        dockScroll.setHorizontalScrollBarEnabled(false);
        dockScroll.setFillViewport(false);

        LinearLayout dock = new LinearLayout(this);
        dock.setGravity(Gravity.CENTER);
        dock.setPadding(
            dp(14), dp(10), dp(14), dp(10)
        );
        dock.setBackground(shape(0x77FFFFFF, 30));

        for (int n = 0; n < Math.min(7, list.size()); n++) {
            addIcon(dock, list.get(n), true);
        }

        dockScroll.addView(dock);
        dockScroll.setBackground(shape(0x33FFFFFF, 32));

        LinearLayout.LayoutParams dockParams =
            new LinearLayout.LayoutParams(-2, -2);
        dockParams.gravity = Gravity.CENTER_HORIZONTAL;
        dockParams.bottomMargin = dp(6);

        page.addView(dockScroll, dockParams);

        setContentView(root);
    }

    void showSearch() {
        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setPadding(
            dp(26), dp(25), dp(26), dp(20)
        );

        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{0xFF315BA9, 0xFF9686C8}
        );
        screen.setBackground(bg);

        TextView back = text("‹  Home", 20, true);
        back.setPadding(0, 0, 0, dp(20));
        screen.addView(back);
        back.setOnClickListener(v -> showHome());

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(0xDDFFFFFF);
        input.setHint("Search apps");
        input.setPadding(
            dp(20), dp(15), dp(20), dp(15)
        );
        input.setBackground(shape(0x55FFFFFF, 25));
        screen.addView(input);

        ScrollView scroll = new ScrollView(this);
        LinearLayout results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        results.setPadding(0, dp(20), 0, 0);

        scroll.addView(results);
        screen.addView(scroll,
            new LinearLayout.LayoutParams(-1, 0, 1));

        List<ResolveInfo> list = apps();
        ArrayList<TextView> rows = new ArrayList<>();

        for (ResolveInfo app : list) {
            TextView row = text(
                app.loadLabel(pm).toString(), 18, false
            );
            row.setPadding(
                dp(16), dp(14), dp(16), dp(14)
            );

            LinearLayout.LayoutParams rp =
                new LinearLayout.LayoutParams(-1, -2);
            rp.bottomMargin = dp(7);

            row.setBackground(shape(0x33FFFFFF, 17));
            results.addView(row, rp);
            rows.add(row);

            row.setOnClickListener(v -> launch(app));
        }

        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(
                CharSequence s, int start,
                int count, int after) {}

            public void onTextChanged(
                CharSequence s, int start,
                int before, int count) {
                String query = s.toString()
                    .toLowerCase(Locale.ROOT);

                for (TextView row : rows) {
                    boolean match = row.getText()
                        .toString()
                        .toLowerCase(Locale.ROOT)
                        .contains(query);

                    row.setVisibility(
                        match ? View.VISIBLE : View.GONE
                    );
                }
            }

            public void afterTextChanged(Editable e) {}
        });

        setContentView(screen);
    }

    @Override
    public void onBackPressed() {
        showHome();
    }
}
