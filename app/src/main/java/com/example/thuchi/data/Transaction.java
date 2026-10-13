package com.example.thuchi.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** 1 giao dich thu/chi. amount > 0, type = 0 chi / 1 thu. */
@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    public long id;
    /** millis tu epoch, mac dinh hom nay. */
    public long date;
    public long amount;
    /** 0 = chi, 1 = thu. */
    public int type;
    public long categoryId;
    public long walletId;
    public String note = "";
    /** Lien ket khoan no (0 = khong). */
    public long debtId = 0;

    public Transaction(long date, long amount, int type,
                       long categoryId, long walletId,
                       String note, long debtId) {
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.walletId = walletId;
        this.note = note == null ? "" : note;
        this.debtId = debtId;
    }
}
