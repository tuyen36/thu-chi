package com.example.thuchi;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuchi.data.AppDb;
import com.example.thuchi.data.Debt;
import com.example.thuchi.data.ThuChiDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** No/vay V1.0: them khoan, tra 1 phan, xong/xoa. */
public class DebtActivity extends AppCompatActivity {

    private int kind = 0;
    private Button btnOut;
    private Button btnIn;
    private EditText etPerson;
    private EditText etAmount;
    private TextView tvSum;
    private RecyclerView rv;
    private final List<Debt> items = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt);
        btnOut = findViewById(R.id.btnTabOut);
        btnIn = findViewById(R.id.btnTabIn);
        etPerson = findViewById(R.id.etPerson);
        etAmount = findViewById(R.id.etAmount);
        tvSum = findViewById(R.id.tvSum);
        rv = findViewById(R.id.rvDebt);
        rv.setLayoutManager(new LinearLayoutManager(this));
        btnOut.setOnClickListener(v -> setKind(0));
        btnIn.setOnClickListener(v -> setKind(1));
        findViewById(R.id.btnAddDebt).setOnClickListener(v -> add());
        setKind(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    private void setKind(int k) {
        kind = k;
        btnOut.setAlpha(k == 0 ? 1f : 0.4f);
        btnIn.setAlpha(k == 1 ? 1f : 0.4f);
        reload();
    }

    private void add() {
        String person = etPerson.getText().toString().trim();
        long amount = Money.parse(etAmount.getText().toString());
        if (person.isEmpty()) {
            toast("Nhap ten nguoi.");
            return;
        }
        if (amount <= 0) {
            toast("Nhap so tien > 0.");
            return;
        }
        ThuChiDao d = AppDb.get(this).dao();
        d.insertDebt(new Debt(person, amount, kind,
                System.currentTimeMillis(), ""));
        etPerson.setText("");
        etAmount.setText("");
        toast("Da them khoan " + (kind == 0 ? "cho vay " : "di vay ")
                + Money.vnd(amount) + ".");
        reload();
    }

    private void reload() {
        ThuChiDao d = AppDb.get(this).dao();
        List<Debt> all = d.allDebts();
        items.clear();
        long rest = 0;
        for (Debt x : all) {
            if (x.kind != kind) continue;
            items.add(x);
            if (x.status == 0) {
                long paid = d.debtPaid(x.id);
                rest += Math.max(0, x.total - paid);
            }
        }
        tvSum.setText(kind == 0
                ? "Nguoi ta con no minh: " + Money.vnd(rest)
                : "Minh con no: " + Money.vnd(rest));
        rv.setAdapter(new DebtAdapter());
    }

    private void askPay(Debt x) {
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setHint("So tien tra lan nay");
        et.setTextColor(0xFFFFFFFF);
        new AlertDialog.Builder(this)
                .setTitle(x.kind == 0 ? "Thu hoi no" : "Tra no")
                .setMessage(x.person + " — con " + Money.vnd(
                        Math.max(0, x.total - AppDb.get(this).dao().debtPaid(x.id))))
                .setView(et)
                .setPositiveButton("Luu", (di, w) -> {
                    long amt = Money.parse(et.getText().toString());
                    if (amt <= 0) return;
                    Intent i = new Intent(this, AddActivity.class);
                    i.putExtra(AddActivity.EXTRA_DEBT, x.id);
                    i.putExtra(AddActivity.EXTRA_DEBT_KIND, x.kind);
                    startActivity(i);
                    toast("Nhap " + Money.vnd(amt)
                            + " o man tiep theo roi Luu.");
                })
                .setNegativeButton("Huy", null)
                .show();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    class DebtAdapter extends RecyclerView.Adapter<DebtAdapter.H> {
        class H extends RecyclerView.ViewHolder {
            TextView a;
            TextView b;
            H(LinearLayout v, TextView a, TextView b) {
                super(v);
                this.a = a;
                this.b = b;
            }
        }

        @Override
        public H onCreateViewHolder(ViewGroup p, int v) {
            LinearLayout row = new LinearLayout(DebtActivity.this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(12, 12, 12, 12);
            row.setBackgroundColor(0xFF161B22);
            TextView a = new TextView(DebtActivity.this);
            a.setTextColor(0xFFFFFFFF);
            a.setTextSize(15);
            a.setTextStyle(android.graphics.Typeface.BOLD);
            TextView b = new TextView(DebtActivity.this);
            b.setTextColor(0xFF8B949E);
            b.setTextSize(12);
            row.addView(a);
            row.addView(b);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 10);
            row.setLayoutParams(lp);
            return new H(row, a, b);
        }

        @Override
        public void onBindViewHolder(H h, int pos) {
            Debt x = items.get(pos);
            ThuChiDao d = AppDb.get(DebtActivity.this).dao();
            long paid = d.debtPaid(x.id);
            long rest = Math.max(0, x.total - paid);
            SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy",
                    Locale.US);
            h.a.setText(x.person + " — " + Money.vnd(x.total)
                    + (x.status == 1 ? "  ✓ XONG" : ""));
            h.b.setText("Da tra " + Money.vnd(paid) + "  •  Con "
                    + Money.vnd(rest) + "  •  " + f.format(new Date(x.createdAt)));
            h.itemView.setOnClickListener(v -> {
                if (x.status == 1) {
                    toast("Khoan nay xong roi.");
                    return;
                }
                askPay(x);
            });
            h.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(DebtActivity.this)
                        .setTitle(x.person + " — " + Money.vnd(x.total))
                        .setItems(new String[]{"Danh dau xong", "Xoa khoan"},
                                (di, w) -> {
                                    ThuChiDao dd = AppDb.get(
                                            DebtActivity.this).dao();
                                    if (w == 0) {
                                        x.status = 1;
                                        dd.updateDebt(x);
                                    } else {
                                        dd.deleteDebt(x);
                                    }
                                    reload();
                                })
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}
