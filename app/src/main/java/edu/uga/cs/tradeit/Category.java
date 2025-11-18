package edu.uga.cs.tradeit;

public class Category
{
    private String key;

    private String name;

    private String ownerKey;

    private long createdAt;

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
}
