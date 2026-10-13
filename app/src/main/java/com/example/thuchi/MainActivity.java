package com.example.thuchi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.thuchi.data.AppDb;
import com.example.thuchi.data.ThuChiDao;
import com.example.thuchi.data.Wallet;

import java.util.Calendar;
import java.util.List;

/** Man chinh: tong quan thang + 4 nut. */
public class MainActivity extends AppCompatActivity {

    private TextView tvMonth;
    private TextView tvIn;
    private TextView tvOut;
    private TextView tvBal;
    private TextView tvWallets;
    private TextView tvTop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMonth = findViewById(R.id.tvMonth);
        tvIn = findViewById(R.id.tvIn);
        tvOut = findViewById(R.id.tvOut);
        tvBal = findViewById(R.id.tvBal);
        tvWallets = findViewById(R.id.tvWallets);
        tvTop = findViewById(R.id.tvTop);

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddActivity.class)));
        findViewById(R.id.btnHistory).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
        findViewById(R.id.btnStats).setOnClickListener(v ->
                startActivity(new Intent(this, StatsActivity.class)));
        findViewById(R.id.btnDebt).setOnClickListener(v ->
                startActivity(new Intent(this, DebtActivity.class)));
        findViewById(R.id.btnBackup).setOnClickListener(v -> BackupHelper.menu(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    protected void onActivityResult(int req, int res, android.content.Intent data) {
        super.onActivityResult(req, res, data);
        if (BackupHelper.onResult(req, res, data)) {
            refresh();
            return;
        }
        if (BackupHelper.onCsvResult(req, res, data)) return;
    }

    private void refresh() {
        ThuChiDao d = AppDb.get(this).dao();
        long now = System.currentTimeMillis();
        long[] m = Money.monthRange(now);
        Calendar c = Calendar.getInstance();
        tvMonth.setText("Thang " + (c.get(Calendar.MONTH) + 1)
                + "/" + c.get(Calendar.YEAR));

        long in = d.sumByType(1, m[0], m[1]);
        long out = d.sumByType(0, m[0], m[1]);
        tvIn.setText(Money.vnd(in));
        tvOut.setText(Money.vnd(out));
        tvBal.setText(Money.vnd(in - out));

        StringBuilder wb = new StringBuilder("Vi: ");
        List<Wallet> ws = d.allWallets();
        for (int i = 0; i < ws.size(); i++) {
            Wallet w = ws.get(i);
            long bal = w.opening + d.balanceWallet(w.id);
            if (i > 0) wb.append("  •  ");
            wb.append(w.name).append(" ").append(Money.vnd(bal));
        }
        tvWallets.setText(wb.toString());

        List<ThuChiDao.CatSum> top = d.spendByCat(m[0], m[1]);
        if (top.isEmpty()) {
            tvTop.setText("Chua co khoan chi nao thang nay.");
        } else {
            StringBuilder sb = new StringBuilder("Ton nhat: ");
            int n = Math.min(3, top.size());
            for (int i = 0; i < n; i++) {
                ThuChiDao.CatSum s = top.get(i);
                if (i > 0) sb.append("  •  ");
                String name = "?";
                try {
                    name = d.catById(s.cat).name;
                } catch (Exception ignored) {}
                sb.append(name).append(" ").append(Money.vnd(s.sum));
            }
            tvTop.setText(sb.toString());
        }
    }
}
