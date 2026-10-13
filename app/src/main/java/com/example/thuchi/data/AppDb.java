package com.example.thuchi.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Transaction.class, Category.class, Wallet.class, Debt.class},
        version = 1, exportSchema = false)
public abstract class AppDb extends RoomDatabase {
    public abstract ThuChiDao dao();

    private static volatile AppDb INS;

    public static AppDb get(Context ctx) {
        if (INS == null) {
            synchronized (AppDb.class) {
                if (INS == null) {
                    INS = Room.databaseBuilder(ctx.getApplicationContext(),
                                    AppDb.class, "thuchi.db")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                    seed(INS.dao());
                }
            }
        }
        return INS;
    }

    /** Du lieu mau lan dau: vi + danh muc co ban. */
    private static void seed(ThuChiDao d) {
        try {
            if (!d.allWallets().isEmpty()) return;
        } catch (Exception e) {
            return;
        }
        d.insertWallet(new Wallet("Tien mat", 0, 0));
        d.insertWallet(new Wallet("Ngan hang", 0, 1));
        d.insertWallet(new Wallet("Vi dien tu", 0, 2));
        String[][] cats = {
                {"An uong", "0"}, {"Cafe", "0"}, {"Xang xe", "0"},
                {"Nha tro", "0"}, {"Dien nuoc", "0"}, {"Mua sam", "0"},
                {"Suc khoe", "0"}, {"Giai tri", "0"}, {"Hieu hi", "0"},
                {"Hoc tap", "0"}, {"Khac (chi)", "0"},
                {"Luong", "1"}, {"Thuong", "1"}, {"Khac (thu)", "1"},
        };
        int[] colors = {0xFFE65100, 0xFF6D4C41, 0xFF546E7A, 0xFF00838F,
                0xFFF9A825, 0xFFAD1457, 0xFF2E7D32, 0xFF5E35B1, 0xFFC62828,
                0xFF1565C0, 0xFF757575, 0xFF00695C, 0xFF2E7D32, 0xFF757575};
        for (int i = 0; i < cats.length; i++) {
            d.insertCat(new Category(cats[i][0],
                    Integer.parseInt(cats[i][1]), "", colors[i % colors.length], i));
        }
    }
}
