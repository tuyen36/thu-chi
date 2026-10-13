package com.example.thuchi.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Danh muc thu/chi. type = 0 chi / 1 thu / 2 dung chung. */
@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public int type;
    /** Ten icon material (vd "fastfood"), hien emoji tuong ung. */
    public String icon = "";
    /** Mau ARGB. */
    public int color = 0xFF1F6FEB;
    public int sort = 0;

    public Category(String name, int type, String icon, int color, int sort) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
        this.sort = sort;
    }
}
