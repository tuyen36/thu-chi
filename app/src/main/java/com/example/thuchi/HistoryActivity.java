package com.example.thuchi;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuchi.data.AppDb;
import com.example.thuchi.data.Category;
import com.example.thuchi.data.ThuChiDao;
import com.example.thuchi.data.Transaction;
import com.example.thuchi.data.Wallet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Lich su giao dich theo thang: bam sua, giu xoa. */
public class HistoryActivity extends AppCompatActivity {

    private TextView tvRange;
    private TextView tvSum;
    private RecyclerView rv;
    private long anchor;
    private final List<Row> rows = new ArrayList<>();

    static class Row {
        int kind; // 0 header ngay, 1 giao dich
        String header;
        Transaction t;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        tvRange = findViewById(R.id.tvRange);
        tvSum = findViewById(R.id.tvSum);
        rv = findViewById(R.id.rvTx);
        rv.setLayoutManager(new LinearLayoutManager(this));
        anchor = System.currentTimeMillis();
        findViewById(R.id.btnPrev).setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(anchor);
            c.add(Calendar.MONTH, -1);
            anchor = c.getTimeInMillis();
            reload();
        });
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(anchor);
            c.add(Calendar.MONTH, 1);
            anchor = c.getTimeInMillis();
            reload();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        ThuChiDao d = AppDb.get(this).dao();
        long[] m = Money.monthRange(anchor);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(anchor);
        tvRange.setText("Thang " + (c.get(Calendar.MONTH) + 1)
                + "/" + c.get(Calendar.YEAR));
        long in = d.sumByType(1, m[0], m[1]);
        long out = d.sumByType(0, m[0], m[1]);
        tvSum.setText("Thu " + Money.vnd(in) + "  •  Chi "
                + Money.vnd(out) + "  •  Con lai " + Money.vnd(in - out));

        List<Transaction> txs = d.txInRange(m[0], m[1]);
        rows.clear();
        SimpleDateFormat f = new SimpleDateFormat("EEEE dd/MM", new Locale("vi", "VN"));
        String lastDay = "";
        for (Transaction t : txs) {
            String day = f.format(new Date(t.date));
            if (!day.equals(lastDay)) {
                lastDay = day;
                Row h = new Row();
                h.kind = 0;
                h.header = day;
                rows.add(h);
            }
            Row r = new Row();
            r.kind = 1;
            r.t = t;
            rows.add(r);
        }
        rv.setAdapter(new TxAdapter());
    }

    class TxAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public int getItemViewType(int p) {
            return rows.get(p).kind;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup p, int v) {
            if (v == 0) {
                TextView t = new TextView(HistoryActivity.this);
                t.setTextColor(0xFF8B949E);
                t.setTextSize(12);
                t.setPadding(4, 14, 4, 4);
                return new H0(t);
            }
            LinearLayout row = new LinearLayout(HistoryActivity.this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(10, 12, 10, 12);
            row.setBackgroundColor(0xFF161B22);
            TextView name = new TextView(HistoryActivity.this);
            name.setTextColor(0xFFFFFFFF);
            name.setTextSize(14);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            TextView amt = new TextView(HistoryActivity.this);
            amt.setTextSize(14);
            amt.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            row.addView(name, lp);
            row.addView(amt);
            return new H1(row, name, amt);
        }

        class H0 extends RecyclerView.ViewHolder {
            H0(View v) { super(v); }
        }
        class H1 extends RecyclerView.ViewHolder {
            TextView name;
            TextView amt;
            H1(View v, TextView n, TextView a) {
                super(v);
                name = n;
                amt = a;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder h, int pos) {
            Row r = rows.get(pos);
            if (r.kind == 0) {
                ((TextView) ((H0) h).itemView).setText(r.header.toUpperCase(Locale.US));
                return;
            }
            H1 hh = (H1) h;
            ThuChiDao d = AppDb.get(HistoryActivity.this).dao();
            String cat = "?";
            String wal = "";
            try {
                Category cc = d.catById(r.t.categoryId);
                if (cc != null) cat = cc.name;
            } catch (Exception ignored) {}
            try {
                Wallet w = d.walletById(r.t.walletId);
                if (w != null) wal = " • " + w.name;
            } catch (Exception ignored) {}
            String note = r.t.note.isEmpty() ? "" : " — " + r.t.note;
            hh.name.setText(cat + wal + note);
            hh.amt.setText((r.t.type == 1 ? "+" : "-") + Money.vnd(r.t.amount));
            hh.amt.setTextColor(r.t.type == 1 ? 0xFF3FB950 : 0xFFF85149);
            hh.itemView.setOnClickListener(v -> {
                Intent i = new Intent(HistoryActivity.this, AddActivity.class);
                i.putExtra(AddActivity.EXTRA_TX, r.t.id);
                startActivity(i);
            });
            hh.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(HistoryActivity.this)
                        .setTitle("Xoa giao dich?")
                        .setMessage((r.t.type == 1 ? "+" : "-")
                                + Money.vnd(r.t.amount) + " — " + cat)
                        .setPositiveButton("Xoa", (di, w) -> {
                            AppDb.get(HistoryActivity.this).dao().deleteTx(r.t);
                            reload();
                            Toast.makeText(HistoryActivity.this,
                                    "Da xoa.", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Huy", null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }
    }
}
