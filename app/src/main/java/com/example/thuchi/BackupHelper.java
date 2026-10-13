package com.example.thuchi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Backup offline 100%: copy file thuchi.db ra Download (SAF) + nhap lai.
 * Sang may khac: copy file .db sang may moi → Backup → Nhap.
 */
public final class BackupHelper {
    private BackupHelper() {}

    private static final int REQ_EXPORT = 9001;
    private static final int REQ_IMPORT = 9002;
    private static Activity host;

    public static void menu(Activity a) {
        host = a;
        new AlertDialog.Builder(a)
                .setTitle("Backup du lieu")
                .setItems(new String[]{
                        "Xuat file backup (.db)",
                        "Nhap file backup (.db)",
                        "Xuat Excel CSV (xem bang tay)"}, (di, w) -> {
                    if (w == 0) doExport();
                    else if (w == 1) doImport();
                    else doCsv();
                })
                .show();
    }

    // ----- xuat .db -----
    private static void doExport() {
        String name = "thuchi-backup-"
                + new SimpleDateFormat("yyyyMMdd-HHmm", Locale.US)
                        .format(new Date()) + ".db";
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/octet-stream");
        i.putExtra(Intent.EXTRA_TITLE, name);
        try {
            host.startActivityForResult(i, REQ_EXPORT);
        } catch (Exception e) {
            toast("Khong mo duoc chon file: " + e.getMessage());
        }
    }

    // ----- nhap .db -----
    private static void doImport() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        try {
            host.startActivityForResult(i, REQ_IMPORT);
        } catch (Exception e) {
            toast("Khong mo duoc chon file: " + e.getMessage());
        }
    }

    /** Goi tu MainActivity.onActivityResult. Tra true neu da xu ly. */
    public static boolean onResult(int req, int res, Intent data) {
        if (host == null) return false;
        if (res != Activity.RESULT_OK || data == null
                || data.getData() == null) return false;
        try {
            File db = host.getDatabasePath("thuchi.db");
            if (req == REQ_EXPORT) {
                copy(db, host.getContentResolver()
                        .openOutputStream(data.getData()));
                toast("Da xuat backup.");
                return true;
            }
            if (req == REQ_IMPORT) {
                // Dong db cu bang cach restart process don gian: chep de
                // len file, lan mo sau Room doc ban moi.
                InputStream in = host.getContentResolver()
                        .openInputStream(data.getData());
                copyStream(in, new FileOutputStream(db));
                toast("Da nhap backup. Mo lai app de thay.");
                return true;
            }
        } catch (Exception e) {
            toast("Loi: " + e.getMessage());
        }
        return false;
    }

    // ----- xuat CSV -----
    private static void doCsv() {
        String name = "thuchi-"
                + new SimpleDateFormat("yyyyMMdd-HHmm", Locale.US)
                        .format(new Date()) + ".csv";
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("text/csv");
        i.putExtra(Intent.EXTRA_TITLE, name);
        CsvHolder.name = name;
        try {
            host.startActivityForResult(i, 9003);
        } catch (Exception e) {
            toast("Khong mo duoc chon file: " + e.getMessage());
        }
    }

    public static boolean onCsvResult(int req, int res, Intent data) {
        if (req != 9003 || res != Activity.RESULT_OK
                || data == null || data.getData() == null) return false;
        try {
            Uri u = data.getData();
            OutputStream out = host.getContentResolver().openOutputStream(u);
            StringBuilder sb = new StringBuilder(
                    "ngay,loai,so tien,danh muc,vi,ghi chu\n");
            com.example.thuchi.data.ThuChiDao d =
                    com.example.thuchi.data.AppDb.get(host).dao();
            SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy",
                    Locale.US);
            for (com.example.thuchi.data.Transaction t
                    : d.txInRange(0, Long.MAX_VALUE)) {
                String cat = "";
                String wal = "";
                try {
                    cat = d.catById(t.categoryId).name;
                } catch (Exception ignored) {}
                try {
                    wal = d.walletById(t.walletId).name;
                } catch (Exception ignored) {}
                sb.append(f.format(new Date(t.date))).append(",")
                        .append(t.type == 1 ? "thu" : "chi").append(",")
                        .append(t.amount).append(",")
                        .append(esc(cat)).append(",")
                        .append(esc(wal)).append(",")
                        .append(esc(t.note)).append("\n");
            }
            out.write(sb.toString().getBytes("UTF-8"));
            out.close();
            toast("Da xuat CSV.");
            return true;
        } catch (Exception e) {
            toast("Loi: " + e.getMessage());
            return false;
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static void copy(File src, OutputStream out) throws Exception {
        FileInputStream in = new FileInputStream(src);
        copyStream(in, out);
    }

    private static void copyStream(InputStream in, OutputStream out)
            throws Exception {
        byte[] b = new byte[8192];
        int n;
        while ((n = in.read(b)) > 0) out.write(b, 0, n);
        in.close();
        out.close();
    }

    private static void toast(String s) {
        if (host != null) {
            Toast.makeText(host, s, Toast.LENGTH_SHORT).show();
        }
    }

    private static final class CsvHolder {
        static String name = "";
    }
}
