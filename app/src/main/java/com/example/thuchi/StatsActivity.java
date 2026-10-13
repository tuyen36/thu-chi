package com.example.thuchi;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.thuchi.data.AppDb;
import com.example.thuchi.data.ThuChiDao;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** Thong ke thang: tron danh muc + cot 6 thang. */
public class StatsActivity extends AppCompatActivity {

    private TextView tvRange;
    private TextView tvSum;
    private PieChart pie;
    private BarChart bar;
    private LinearLayout listTop;
    private long anchor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        tvRange = findViewById(R.id.tvRange);
        tvSum = findViewById(R.id.tvSum);
        pie = findViewById(R.id.chartPie);
        bar = findViewById(R.id.chartBar);
        listTop = findViewById(R.id.listTop);
        anchor = System.currentTimeMillis();
        Button prev = findViewById(R.id.btnPrev);
        Button next = findViewById(R.id.btnNext);
        prev.setOnClickListener(v -> move(-1));
        next.setOnClickListener(v -> move(1));
        styleCharts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    private void move(int d) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(anchor);
        c.add(Calendar.MONTH, d);
        anchor = c.getTimeInMillis();
        reload();
    }

    private void styleCharts() {
        pie.setUsePercentValues(false);
        pie.getDescription().setEnabled(false);
        pie.setDrawHoleEnabled(true);
        pie.setHoleColor(Color.TRANSPARENT);
        pie.setEntryLabelColor(Color.WHITE);
        pie.getLegend().setTextColor(Color.WHITE);

        bar.getDescription().setEnabled(false);
        bar.getLegend().setTextColor(Color.WHITE);
        bar.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        bar.getXAxis().setTextColor(Color.WHITE);
        bar.getAxisLeft().setTextColor(Color.WHITE);
        bar.getAxisRight().setEnabled(false);
    }

    private void reload() {
        ThuChiDao d = AppDb.get(this).dao();
        long[] m = Money.monthRange(anchor);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(anchor);
        int mo = c.get(Calendar.MONTH) + 1;
        int ye = c.get(Calendar.YEAR);
        tvRange.setText("Thang " + mo + "/" + ye);

        long in = d.sumByType(1, m[0], m[1]);
        long out = d.sumByType(0, m[0], m[1]);
        tvSum.setText("Thu " + Money.vnd(in) + "  •  Chi "
                + Money.vnd(out) + "  •  Con lai " + Money.vnd(in - out));

        // Tron.
        List<ThuChiDao.CatSum> top = d.spendByCat(m[0], m[1]);
        List<PieEntry> pe = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        listTop.removeAllViews();
        int n = Math.min(8, top.size());
        for (int i = 0; i < n; i++) {
            ThuChiDao.CatSum s = top.get(i);
            String name = "?";
            int col = 0xFF1F6FEB;
            try {
                com.example.thuchi.data.Category cc = d.catById(s.cat);
                if (cc != null) {
                    name = cc.name;
                    col = cc.color;
                }
            } catch (Exception ignored) {}
            pe.add(new PieEntry(s.sum, name));
            colors.add(col);
            TextView t = new TextView(this);
            t.setText((i + 1) + ". " + name + " — " + Money.vnd(s.sum));
            t.setTextColor(0xFFFFFFFF);
            t.setTextSize(13);
            t.setPadding(4, 6, 4, 6);
            listTop.addView(t, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        if (pe.isEmpty()) {
            pie.clear();
            pie.setNoDataText("Chua co chi tieu thang nay.");
        } else {
            PieDataSet ds = new PieDataSet(pe, "");
            ds.setColors(colors);
            ds.setValueTextColor(Color.WHITE);
            ds.setValueTextSize(11f);
            PieData pd = new PieData(ds);
            pd.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return Money.vnd((long) value);
                }
            });
            pie.setData(pd);
            pie.invalidate();
        }

        // Cot 6 thang.
        List<BarEntry> beIn = new ArrayList<>();
        List<BarEntry> beOut = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(anchor);
        cc.set(Calendar.DAY_OF_MONTH, 1);
        cc.add(Calendar.MONTH, -5);
        for (int i = 0; i < 6; i++) {
            long[] r = Money.monthRange(cc.getTimeInMillis());
            beIn.add(new BarEntry(i, d.sumByType(1, r[0], r[1])));
            beOut.add(new BarEntry(i, d.sumByType(0, r[0], r[1])));
            labels.add((cc.get(Calendar.MONTH) + 1) + "/" + cc.get(Calendar.YEAR));
            cc.add(Calendar.MONTH, 1);
        }
        BarDataSet sIn = new BarDataSet(beIn, "Thu");
        sIn.setColor(0xFF3FB950);
        sIn.setValueTextColor(Color.WHITE);
        BarDataSet sOut = new BarDataSet(beOut, "Chi");
        sOut.setColor(0xFFF85149);
        sOut.setValueTextColor(Color.WHITE);
        BarData bd = new BarData(sIn, sOut);
        bd.setBarWidth(0.35f);
        bar.setData(bd);
        bar.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        bar.getXAxis().setGranularity(1f);
        bar.groupBars(0f, 0.2f, 0.05f);
        bar.invalidate();
    }
}
