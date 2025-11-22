package edu.uga.cs.tradeit;

import com.google.firebase.database.Exclude;

public class Item {
    @Exclude  // Exclude from Firebase - key is derived from node key
    private String key;

    private String name;

    private String ownerKey;

    private String categoryName;

    private long createdAt;

    private Double price;

    private boolean isFree;

    public Item() {
    }

    public Item(String name, double price, boolean isFree, String categoryName) {
        this.name = name;
        this.price = price;
        this.isFree = isFree;
        this.categoryName = categoryName;

        if (isFree) {
            this.price = null;
        }
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerKey() {
        return ownerKey;
    }

    public void setOwnerKey(String ownerKey) {
        this.ownerKey = ownerKey;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
