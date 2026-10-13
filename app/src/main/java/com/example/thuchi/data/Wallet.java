package com.example.thuchi.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Vi tien: mat, ngan hang, vi dien tu. balance tu dong tru/cong. */
@Entity(tableName = "wallets")
public class Wallet {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    /** So du dau ky (khi tao vi). */
    public long opening;
    public int sort = 0;

    public Wallet(String name, long opening, int sort) {
        this.name = name;
        this.opening = opening;
        this.sort = sort;
    }
}
