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
import android.widget.*;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    LinearLayout home;
    PackageManager pm;

    int dp(float value) {
        return (int)(value * getResources().getDisplayMetrics().density);
    }

    GradientDrawable rounded(int color, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(radius));
        return bg;
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        pm = getPackageManager();
        showHome();
    }

    void showHome() {
        home = new LinearLayout(this);
        home.setOrientation(LinearLayout.VERTICAL);
        home.setPadding(dp(20), dp(35), dp(20), dp(20));

        GradientDrawable wallpaper = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{
                Color.rgb(32, 75, 145),
                Color.rgb(112, 100, 192),
                Color.rgb(238, 143, 181)
            }
        );
        home.setBackground(wallpaper);

        TextView title = new TextView(this);
        title.setText("LiquidPad 26");
        title.setTextSize(30);
        title.setTextColor(Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        home.addView(title);

        TextView search = new TextView(this);
        search.setText("⌕  Search Apps");
        search.setTextSize(17);
        search.setTextColor(Color.WHITE);
        search.setPadding(dp(20), dp(14), dp(20), dp(14));
        search.setBackground(rounded(0x55FFFFFF, 24));

        LinearLayout.LayoutParams searchParams =
            new LinearLayout.LayoutParams(-1, -2);
        searchParams.setMargins(0, dp(20), 0, dp(20));
        home.addView(search, searchParams);

        search.setOnClickListener(v -> showApps());

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(5);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(grid);

        home.addView(scroll,
            new LinearLayout.LayoutParams(-1, 0, 1));

        List<ResolveInfo> apps = getApps();

        int count = Math.min(apps.size(), 20);

        for (int i = 0; i < count; i++) {
            addApp(grid, apps.get(i));
        }

        LinearLayout dock = new LinearLayout(this);
        dock.setGravity(Gravity.CENTER);
        dock.setPadding(dp(12), dp(12), dp(12), dp(12));
        dock.setBackground(rounded(0x77FFFFFF, 28));

        for (int i = 0; i < Math.min(5, apps.size()); i++) {
            addApp(dock, apps.get(i));
        }

        home.addView(dock);

        setContentView(home);
    }

    List<ResolveInfo> getApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps =
            pm.queryIntentActivities(intent, 0);

        return apps == null ? new ArrayList<>() : apps;
    }

    void addApp(android.view.ViewGroup parent, ResolveInfo info) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(5), dp(10), dp(5), dp(10));

        ImageView icon = new ImageView(this);
        Drawable image = info.loadIcon(pm);
        icon.setImageDrawable(image);

        item.addView(icon,
            new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView label = new TextView(this);
        label.setText(info.loadLabel(pm));
        label.setTextSize(11);
        label.setTextColor(Color.WHITE);
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(1);

        item.addView(label);

        parent.addView(item,
            new android.view.ViewGroup.LayoutParams(dp(80), dp(85)));

        item.setOnClickListener(v -> {
            Intent launch = pm.getLaunchIntentForPackage(
                info.activityInfo.packageName
            );

            if (launch != null) {
                startActivity(launch);
            }
        });
    }

    void showApps() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(15), dp(20), dp(15), dp(10));
        layout.setBackgroundColor(0xFF202A45);

        Button back = new Button(this);
        back.setText("← Home");
        back.setOnClickListener(v -> showHome());
        layout.addView(back);

        EditText search = new EditText(this);
        search.setHint("Search installed apps");
        search.setSingleLine(true);
        layout.addView(search);

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);

        layout.addView(scroll,
            new LinearLayout.LayoutParams(-1, 0, 1));

        List<ResolveInfo> apps = getApps();

        for (ResolveInfo app : apps) {
            TextView row = new TextView(this);
            row.setText(app.loadLabel(pm));
            row.setTextSize(18);
            row.setTextColor(Color.WHITE);
            row.setPadding(dp(15), dp(14), dp(15), dp(14));

            list.addView(row);

            row.setOnClickListener(v -> {
                Intent launch = pm.getLaunchIntentForPackage(
                    app.activityInfo.packageName
                );
                if (launch != null) startActivity(launch);
            });
        }

        search.addTextChangedListener(
            new android.text.TextWatcher() {
                public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {}

                public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                    for (int i = 0; i < list.getChildCount(); i++) {
                        TextView row = (TextView) list.getChildAt(i);
                        boolean match = row.getText().toString()
                            .toLowerCase()
                            .contains(s.toString().toLowerCase());
                        row.setVisibility(match ? View.VISIBLE : View.GONE);
                    }
                }

                public void afterTextChanged(
                    android.text.Editable e) {}
            }
        );

        setContentView(layout);
    }
}
