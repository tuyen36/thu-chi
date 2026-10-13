package com.example.thuchi.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ThuChiDao {
    // ----- transaction -----
    @Insert
    long insertTx(Transaction t);
    @Update
    void updateTx(Transaction t);
    @Delete
    void deleteTx(Transaction t);

    @Query("SELECT * FROM transactions WHERE date >= :from AND date < :to ORDER BY date DESC, id DESC")
    List<Transaction> txInRange(long from, long to);

    @Query("SELECT * FROM transactions WHERE walletId = :w ORDER BY date DESC, id DESC LIMIT :limit")
    List<Transaction> txByWallet(long w, int limit);

    @Query("SELECT IFNULL(SUM(CASE WHEN type=1 THEN amount ELSE -amount END),0) FROM transactions")
    long balanceAll();

    @Query("SELECT IFNULL(SUM(CASE WHEN type=1 THEN amount ELSE -amount END),0) FROM transactions WHERE walletId = :w")
    long balanceWallet(long w);

    @Query("SELECT IFNULL(SUM(amount),0) FROM transactions WHERE type = :type AND date >= :from AND date < :to")
    long sumByType(int type, long from, long to);

    @Query("SELECT categoryId AS cat, SUM(amount) AS sum FROM transactions WHERE type = 0 AND date >= :from AND date < :to GROUP BY categoryId ORDER BY sum DESC")
    List<CatSum> spendByCat(long from, long to);

    @Query("SELECT IFNULL(SUM(CASE WHEN t.type=1 THEN t.amount ELSE -t.amount END),0) FROM transactions t WHERE t.debtId = :debt")
    long debtPaid(long debt);

    // ----- category -----
    @Insert
    long insertCat(Category c);
    @Update
    void updateCat(Category c);
    @Delete
    void deleteCat(Category c);

    @Query("SELECT * FROM categories ORDER BY sort, name")
    List<Category> allCats();

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    Category catById(long id);

    // ----- wallet -----
    @Insert
    long insertWallet(Wallet w);
    @Update
    void updateWallet(Wallet w);
    @Delete
    void deleteWallet(Wallet w);

    @Query("SELECT * FROM wallets ORDER BY sort, name")
    List<Wallet> allWallets();

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    Wallet walletById(long id);

    // ----- debt -----
    @Insert
    long insertDebt(Debt d);
    @Update
    void updateDebt(Debt d);
    @Delete
    void deleteDebt(Debt d);

    @Query("SELECT * FROM debts WHERE status = :status ORDER BY createdAt DESC")
    List<Debt> debtsByStatus(int status);

    @Query("SELECT * FROM debts ORDER BY status, createdAt DESC")
    List<Debt> allDebts();

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    Debt debtById(long id);

    /** Tong hop danh muc + so tien chi. */
    class CatSum {
        public long cat;
        public long sum;
    }
}
