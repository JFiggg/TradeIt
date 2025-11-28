package edu.uga.cs.tradeit.recyclers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.objects.Item;
import edu.uga.cs.tradeit.objects.Transaction;

public class ItemBuyerRecyclerAdapter extends RecyclerView.Adapter<ItemBuyerRecyclerAdapter.ItemHolder> {
    private static final String DEBUG_TAG = "ItemBuyerRecyclerAdapter";
    private List<Transaction> transactionList;
    private Context context;
    private Fragment parentFragment;

    public ItemBuyerRecyclerAdapter(List<Transaction> transactionList, Context context, Fragment parentFragment) {
        this.transactionList = transactionList;
        this.context = context;
        this.parentFragment = parentFragment;
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        TextView transactionNameTextView;
        TextView transactionPriceTextView;
        TextView transactionCategoryTextView;

        TextView transactionCreatedAtTextView;

        TextView transactionOwnerTextView;
        TextView transactionStatusTextView;

        Button cancelButton;

        public ItemHolder(View view) {
            super(view);

            transactionNameTextView = view.findViewById(R.id.itemNameTextView);
            transactionPriceTextView = view.findViewById(R.id.itemPriceTextView);
            transactionCategoryTextView = view.findViewById(R.id.itemCategoryTextView);
            transactionCreatedAtTextView = view.findViewById(R.id.itemCreatedAtTextView);
            transactionOwnerTextView = view.findViewById(R.id.itemOwnerTextView);
            transactionStatusTextView = view.findViewById(R.id.statusTextView);
            cancelButton = view.findViewById(R.id.cancelButton);
        }
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from( parent.getContext()).inflate( R.layout.item_buyer, parent, false );
        return new ItemBuyerRecyclerAdapter.ItemHolder( view );
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.transactionNameTextView.setText(transaction.getItemName());


        if (transaction.getAmount() == 0.0) {
            holder.transactionPriceTextView.setText("Free");
        } else {
            holder.transactionPriceTextView.setText("Price: $" + String.valueOf(transaction.getAmount()));
        }

        holder.transactionCategoryTextView.setText( "Category: "+ transaction.getCategoryName());

        long createdAt = transaction.getTimestamp();
        if (createdAt > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(createdAt));
            holder.transactionCreatedAtTextView.setText("Created: " + formattedDate);
        } else {
            holder.transactionCreatedAtTextView.setText("Created: Unknown");
        }

        // Display seller (owner) name - use display name with fallback to UID
        String ownerDisplay = transaction.getSenderDisplayName();
        if (ownerDisplay == null || ownerDisplay.isEmpty()) {
            ownerDisplay = transaction.getSender();
        }
        holder.transactionOwnerTextView.setText("Seller: " + (ownerDisplay != null ? ownerDisplay : "Unknown"));

        holder.transactionStatusTextView.setText("Status: " + transaction.getStatus());

        holder.cancelButton.setOnClickListener(v -> {
            // Show confirmation dialog before declining
            new AlertDialog.Builder(context)
                    .setTitle("Decline Item")
                    .setMessage("Are you sure you want to decline the transaction \"" + transaction.getItemName() + "\"?")
                    .setPositiveButton("Decline", (dialog, which) -> {
                        cancelRequest(transaction);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    private void cancelRequest(Transaction transaction) {

        // 1. Validate mandatory fields needed for updating the item
        final String categoryName = transaction.getCategoryName();
        final String itemId = transaction.getItemId();

        if (transaction.getKey() == null || categoryName == null || itemId == null) {
            Log.e(DEBUG_TAG, "Error: Missing critical keys in transaction data. Cannot cancel.");
            Toast.makeText(context, "Failed to cancel: Transaction data incomplete.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Part 1: Update Transaction Status ---
        DatabaseReference transactionRef = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(transaction.getKey());

        transactionRef.child("status").setValue("cancelled")
                .addOnSuccessListener(aVoid -> {
                    Log.d(DEBUG_TAG, "Transaction " + transaction.getKey() + " cancelled successfully.");

                    // --- Part 2: Restore Item from stored data ---
                    Item restoredItem = transaction.getItem();
                    if (restoredItem == null) {
                        Log.e(DEBUG_TAG, "Error: No item data stored in transaction. Cannot restore item.");
                        Toast.makeText(context, "Request cancelled, but item data missing.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    DatabaseReference itemRef = FirebaseDatabase.getInstance()
                            .getReference("categories")
                            .child(categoryName)
                            .child("items")
                            .child(itemId);

                    // Restore the complete item with all original data
                    itemRef.setValue(restoredItem)
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(DEBUG_TAG, "Item " + itemId + " fully restored to category.");
                                Toast.makeText(context, "Request cancelled. Item is now back on the market.", Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(DEBUG_TAG, "Error restoring item: " + e.getMessage());
                                Toast.makeText(context, "Request cancelled, but failed to restore item.", Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(DEBUG_TAG, "Failed to update transaction status to cancelled: " + e.getMessage());
                    Toast.makeText(context, "Failed to cancel request.", Toast.LENGTH_SHORT).show();
                });
    }
}
