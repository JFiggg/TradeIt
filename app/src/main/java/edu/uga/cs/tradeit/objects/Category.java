package edu.uga.cs.tradeit.objects;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

public class Category {
    @Exclude  // Exclude from Firebase - key is derived from node key
    private String key;

    @Exclude  // Exclude from Firebase - name is used as the key
    private String name;

    private String ownerKey;

    private String ownerName;

    private long createdAt;

    @Exclude // Firebase
    private HashMap<String, Item> items;

    public Category() {
        this.key = null;
        this.name = null;
        this.ownerKey = null;
        this.createdAt = 0;
    }

    public Category(String name) {
        this.key = null;
        this.name = name;
        this.ownerKey = null;
        this.createdAt = 0;
    }

    public Category(String name, String ownerKey, String ownerName, long createdAt) {
        this.key = null;
        this.name = name;
        this.ownerKey = ownerKey;
        this.createdAt = createdAt;
        this.ownerName = ownerName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
        this.name = key;
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

    public HashMap<String, Item> getItems() {
        return items;
    }

    public int getItemCount() {
        if (this.getItems() != null) {
            return this.getItems().size();
        }

        return 0;
    }


    public void setItems(HashMap<String, Item> items) {
        this.items = items;
    }
}
