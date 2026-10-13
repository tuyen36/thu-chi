package com.example.thuchi.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Khoan no/vay. kind = 0 cho vay (minh la chu no) / 1 di vay (minh la con no).
 * status = 0 con no / 1 xong. Tra gop bang giao dich thu/chi gan debtId.
 */
@Entity(tableName = "debts")
public class Debt {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String person;
    public long total;
    public int kind;
    public int status = 0;
    public long createdAt;
    public String note = "";

    public Debt(String person, long total, int kind, long createdAt, String note) {
        this.person = person;
        this.total = total;
        this.kind = kind;
        this.createdAt = createdAt;
        this.note = note == null ? "" : note;
    }
}
