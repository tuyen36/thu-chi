package com.example.thuchi;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuchi.data.AppDb;
import com.example.thuchi.data.Category;
import com.example.thuchi.data.ThuChiDao;
import com.example.thuchi.data.Transaction;
import com.example.thuchi.data.Wallet;

import java.util.ArrayList;
import java.util.List;

/** Man nhap thu/chi (< 3 giay). Ho tro sua + tra no nhanh. */
public class AddActivity extends AppCompatActivity {

    /** Mo sua giao dich co san. */
    public static final String EXTRA_TX = "tx_id";
    /** Mo tra no: tao giao dich gan debtId. kind 0 cho vay / 1 di vay. */
    public static final String EXTRA_DEBT = "debt_id";
    public static final String EXTRA_DEBT_KIND = "debt_kind";

    private int type = 0;
    private Button btnOut;
    private Button btnIn;
    private EditText etAmount;
    private TextView tvAmountFmt;
    private EditText etNote;
    private TextView tvTitle;
    private TextView tvDebtHint;
    private RecyclerView rvCat;
    private LinearLayout rowWallet;

    private List<Category> cats = new ArrayList<>();
    private List<Wallet> wallets = new ArrayList<>();
    private long selCat = -1;
    private long selWallet = -1;
    private long editTx = -1;
    private long debtId = 0;
    private int debtKind = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        tvTitle = findViewById(R.id.tvTitle);
        btnOut = findViewById(R.id.btnOut);
        btnIn = findViewById(R.id.btnIn);
        etAmount = findViewById(R.id.etAmount);
        tvAmountFmt = findViewById(R.id.tvAmountFmt);
        etNote = findViewById(R.id.etNote);
        tvDebtHint = findViewById(R.id.tvDebtHint);
        rvCat = findViewById(R.id.rvCat);
        rowWallet = findViewById(R.id.rowWallet);

        editTx = getIntent().getLongExtra(EXTRA_TX, -1);
        debtId = getIntent().getLongExtra(EXTRA_DEBT, 0);
        debtKind = getIntent().getIntExtra(EXTRA_DEBT_KIND, 0);

        rvCat.setLayoutManager(new GridLayoutManager(this, 3));
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                long v = Money.parse(s.toString());
                tvAmountFmt.setText(v > 0 ? "= " + Money.vnd(v) : "");
            }
        });

        btnOut.setOnClickListener(v -> setType(0));
        btnIn.setOnClickListener(v -> setType(1));
        findViewById(R.id.btnSave).setOnClickListener(v -> save());

        reload();
        if (debtId > 0) {
            // Tra no: cho vay -> thu tien ve; di vay -> chi tien tra.
            setType(debtKind == 0 ? 1 : 0);
            tvDebtHint.setVisibility(View.VISIBLE);
            tvDebtHint.setText(debtKind == 0
                    ? "Dang thu hoi no (tien ve vi)"
                    : "Dang tra no (tien ra khoi vi)");
            tvTitle.setText("Tra no");
        }
        if (editTx > 0) {
            tvTitle.setText("Sua giao dich");
            new Thread(() -> {
                ThuChiDao d = AppDb.get(this).dao();
                for (com.example.thuchi.data.Transaction t
                        : d.txInRange(0, Long.MAX_VALUE)) {
                    if (t.id == editTx) {
                        runOnUiThread(() -> fill(t));
                        break;
                    }
                }
            }).start();
        }
    }

    private void fill(com.example.thuchi.data.Transaction t) {
        type = t.type;
        selCat = t.categoryId;
        selWallet = t.walletId;
        debtId = t.debtId;
        etAmount.setText(String.valueOf(t.amount));
        etNote.setText(t.note);
        refreshType();
        renderCats();
        renderWallets();
    }

    private void reload() {
        ThuChiDao d = AppDb.get(this).dao();
        wallets = d.allWallets();
        if (selWallet < 0 && !wallets.isEmpty()) selWallet = wallets.get(0).id;
        refreshType();
        renderCats();
        renderWallets();
    }

    private void setType(int t) {
        type = t;
        selCat = -1;
        refreshType();
        renderCats();
    }

    private void refreshType() {
        tvTitle.setText(editTx > 0 ? "Sua giao dich"
                : (debtId > 0 ? "Tra no" : (type == 0 ? "Nhap khoan chi" : "Nhap khoan thu")));
        btnOut.setAlpha(type == 0 ? 1f : 0.4f);
        btnIn.setAlpha(type == 1 ? 1f : 0.4f);
    }

    private void renderCats() {
        ThuChiDao d = AppDb.get(this).dao();
        List<Category> all = d.allCats();
        cats.clear();
        for (Category c : all) {
            if (c.type == type || c.type == 2) cats.add(c);
        }
        if (selCat < 0 && !cats.isEmpty()) selCat = cats.get(0).id;
        rvCat.setAdapter(new CatAdapter());
    }

    private void renderWallets() {
        rowWallet.removeAllViews();
        for (Wallet w : wallets) {
            Button b = new Button(this);
            b.setText(w.name);
            b.setTextSize(12);
            b.setAllCaps(false);
            if (w.id == selWallet) b.setBackgroundColor(0xFF1F6FEB);
            final long id = w.id;
            b.setOnClickListener(v -> {
                selWallet = id;
                renderWallets();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(0, 0, 8, 0);
            rowWallet.addView(b, lp);
        }
    }

    private void save() {
        long amount = Money.parse(etAmount.getText().toString());
        if (amount <= 0) {
            toast("Nhap so tien > 0.");
            return;
        }
        if (selCat < 0) {
            toast("Chon danh muc.");
            return;
        }
        if (selWallet < 0) {
            toast("Chon vi tien.");
            return;
        }
        String note = etNote.getText().toString().trim();
        ThuChiDao d = AppDb.get(this).dao();
        if (editTx > 0) {
            com.example.thuchi.data.Transaction t =
                    new com.example.thuchi.data.Transaction(
                            System.currentTimeMillis(), amount, type,
                            selCat, selWallet, note, debtId);
            t.id = editTx;
            d.updateTx(t);
            toast("Da luu.");
        } else {
            d.insertTx(new Transaction(System.currentTimeMillis(), amount,
                    type, selCat, selWallet, note, debtId));
            // Tu dong danh dau no xong khi tra du.
            if (debtId > 0) {
                try {
                    com.example.thuchi.data.Debt db = d.debtById(debtId);
                    if (db != null && d.debtPaid(debtId) >= db.total) {
                        db.status = 1;
                        d.updateDebt(db);
                    }
                } catch (Exception ignored) {}
            }
            toast("Da luu " + Money.vnd(amount) + ".");
        }
        finish();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void askAddCat() {
        final EditText et = new EditText(this);
        et.setHint("Ten danh muc moi");
        et.setTextColor(0xFFFFFFFF);
        new AlertDialog.Builder(this)
                .setTitle(type == 0 ? "Them danh muc chi" : "Them danh muc thu")
                .setView(et)
                .setPositiveButton("Them", (di, w) -> {
                    String name = et.getText().toString().trim();
                    if (name.isEmpty()) return;
                    ThuChiDao d = AppDb.get(this).dao();
                    long id = d.insertCat(new Category(name, type, "",
                            0xFF1F6FEB, 999));
                    selCat = id;
                    renderCats();
                })
                .setNegativeButton("Huy", null)
                .show();
    }

    class CatAdapter extends RecyclerView.Adapter<CatAdapter.H> {
        class H extends RecyclerView.ViewHolder {
            Button b;
            H(Button b) { super(b); this.b = b; }
        }

        @Override
        public H onCreateViewHolder(ViewGroup p, int v) {
            Button b = new Button(AddActivity.this);
            b.setTextSize(12);
            b.setAllCaps(false);
            b.setMinHeight(110);
            return new H(b);
        }

        @Override
        public void onBindViewHolder(H h, int pos) {
            if (pos < cats.size()) {
                Category c = cats.get(pos);
                h.b.setText(c.name);
                h.b.setBackgroundColor(c.id == selCat ? 0xFF1F6FEB : 0xFF21262D);
                h.b.setOnClickListener(v -> {
                    selCat = c.id;
                    notifyDataSetChanged();
                });
                h.b.setOnLongClickListener(v -> false);
            } else {
                h.b.setText("＋ Them");
                h.b.setBackgroundColor(0xFF161B22);
                h.b.setOnClickListener(v -> askAddCat());
            }
        }

        @Override
        public int getItemCount() {
            return cats.size() + 1;
        }
    }
}
