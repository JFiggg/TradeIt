package edu.uga.cs.tradeit;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;

public class Category
{
    @Exclude  // Exclude from Firebase - key is derived from node key
    private String key;

    @Exclude  // Exclude from Firebase - name is used as the key
    private String name;

    private String ownerKey;

    private long createdAt;

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

    public Category(String name, String ownerKey, long createdAt) {
        this.key = null;
        this.name = name;
        this.ownerKey = ownerKey;
        this.createdAt = createdAt;
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

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public HashMap<String, Item> getItems() {
        return items;
    }

    public void setItems(HashMap<String, Item> items) {
        this.items = items;
    }
}
