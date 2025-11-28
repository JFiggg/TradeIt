package edu.uga.cs.tradeit.objects;

import com.google.firebase.database.Exclude;

public class Transaction {
    @Exclude  // Exclude from Firebase - key is derived from node key
    private String key;
    private String itemId;
    private String itemName;
    private String categoryName;
    private String sender;

    private String senderDisplayName;

    private String recipient;
    private String recipientDisplayName;


    private String status;
    private double amount;
    private long timestamp;
    private Item item;  // Store complete item data for restoration

    public Transaction() {
        // needed for Firebase
    }

    public Transaction(String itemId, String itemName, String sender, String recipient, String status, double amount, long timestamp, String categoryName) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.sender = sender;
        this.recipient = recipient;
        this.status = status;
        this.amount = amount;
        this.timestamp = timestamp;
        this.categoryName = categoryName;
    }

    public Transaction(String itemId, String itemName, String sender, String recipient, String status, double amount, long timestamp, String categoryName, Item item, String senderDisplayName, String recipientDisplayName) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.sender = sender;
        this.recipient = recipient;
        this.status = status;
        this.amount = amount;
        this.timestamp = timestamp;
        this.categoryName = categoryName;
        this.item = item;
        this.senderDisplayName = senderDisplayName;
        this.recipientDisplayName = recipientDisplayName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    public void setSenderDisplayName(String senderDisplayName) {
        this.senderDisplayName = senderDisplayName;
    }

    public String getRecipientDisplayName() {
        return recipientDisplayName;
    }

    public void setRecipientDisplayName(String recipientDisplayName) {
        this.recipientDisplayName = recipientDisplayName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }


}
