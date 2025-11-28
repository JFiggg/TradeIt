package edu.uga.cs.tradeit.objects;

import com.google.firebase.database.Exclude;

public class Item {
    @Exclude  // Exclude from Firebase - key is derived from node key
    private String key;

    private String name;

    private String ownerKey;

    private String ownerName;

    private String categoryName;

    private long createdAt;

    private Double price;

    private boolean isFree;

    public Item() {
    }

    public Item(String name, Double price, boolean isFree, String categoryName) {
        this.name = name;
        this.price = price;
        this.isFree = isFree;
        this.categoryName = categoryName;

        if (price != null && price <= 0) {
            isFree = true;
        }


        if (isFree) {
            this.price = null;
        }

        if (this.price == null) {
            this.price = 0.0;
        }
    }

    public Double getPrice() {
        if (this.price == null) {
            this.price = 0.0;
        }
        return price;
    }

    public void setPrice(Double price) {
        if (price == null) {
            this.price = 0.0;
        } else {
            this.price = price;
        }
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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
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
