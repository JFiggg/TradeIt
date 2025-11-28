package edu.uga.cs.tradeit.recyclers;

import static androidx.core.content.ContentProviderCompat.requireContext;

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
import edu.uga.cs.tradeit.dialogs.ItemDialogFragment;
import edu.uga.cs.tradeit.objects.Item;
import edu.uga.cs.tradeit.objects.Transaction;

public class SellerItemRecyclerAdapter extends RecyclerView.Adapter<SellerItemRecyclerAdapter.ItemHolder> {
    private static final String DEBUG_TAG = "SellerItemRecyclerAdapter";
    private List<Transaction> transactionList;
    private Context context;
    private Fragment parentFragment;

    public SellerItemRecyclerAdapter(List<Transaction> transactionList, Context context, Fragment parentFragment) {
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

        Button acceptButton;
        Button declineButton;

        public ItemHolder(View view) {
            super(view);

            transactionNameTextView = view.findViewById(R.id.itemNameTextView);
            transactionPriceTextView = view.findViewById(R.id.itemPriceTextView);
            transactionCategoryTextView = view.findViewById(R.id.itemCategoryTextView);
            transactionCreatedAtTextView = view.findViewById(R.id.itemCreatedAtTextView);
            transactionOwnerTextView = view.findViewById(R.id.itemOwnerTextView);
            transactionStatusTextView = view.findViewById(R.id.statusTextView);
            acceptButton = view.findViewById(R.id.acceptButton);
            declineButton = view.findViewById(R.id.declineButton);

        }
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from( parent.getContext()).inflate( R.layout.item_seller, parent, false );
        return new SellerItemRecyclerAdapter.ItemHolder( view );
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

        // Display buyer (requester) name - use display name with fallback to UID
        String buyerDisplay = transaction.getRecipientDisplayName();
        if (buyerDisplay == null || buyerDisplay.isEmpty()) {
            buyerDisplay = transaction.getRecipient();
        }
        holder.transactionOwnerTextView.setText("Requester: " + (buyerDisplay != null ? buyerDisplay : "Unknown"));

        holder.transactionStatusTextView.setText("Status: " + transaction.getStatus());


        holder.acceptButton.setOnClickListener(v -> {
            acceptItem(transaction);
        });

        holder.declineButton.setOnClickListener(v -> {
            // Show confirmation dialog before declining
            new AlertDialog.Builder(context)
                    .setTitle("Decline Item")
                    .setMessage("Are you sure you want to decline the transaction \"" + transaction.getItemName() + "\"?")
                    .setPositiveButton("Decline", (dialog, which) -> {
                        declineRequest(transaction);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    private void acceptItem(Transaction transaction) {

        // 1. Get the reference to the specific transaction node
        DatabaseReference transactionRef = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(transaction.getKey());

        // 2. Set the 'status' field to the string "accepted"
        transactionRef.child("status").setValue("accepted")
                .addOnSuccessListener(aVoid -> {
                    // Log success or show Toast for transaction update
                    Log.d("ACCEPT_TX", "Transaction " + transaction.getKey() + " accepted.");
                })
                .addOnFailureListener(e -> {
                    Log.e("ACCEPT_TX", "Failed to accept transaction: " + e.getMessage());
                    // Show failure Toast
                });
    }

    /**
     * Declines a transaction and restores the transaction to the market.
     * This method is used when the original Item was DESTROYED (removed from the
     * categories node) upon the initial buy request.
     * * @param transaction The Transaction object associated with the pending request.
     */
    private void declineRequest(Transaction transaction) {
        // 1. Validate mandatory fields needed for restoration
        String categoryName = transaction.getCategoryName();
        String transactionId = transaction.getItemId();
        String transactionName = transaction.getItemName();
        String ownerKey = transaction.getRecipient(); // Assuming the recipient of the TX is the transaction owner (seller)

        if (transaction.getKey() == null || categoryName == null || transactionId == null || transactionName == null || ownerKey == null) {
            Log.e("DECLINE_TX", "Error: Missing critical fields in transaction data. Cannot restore transaction.");
            Toast.makeText(context, "Failed to decline: Transaction data incomplete.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Get the reference to the transaction node
        DatabaseReference transactionRef = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(transaction.getKey());

        // 2. DELETE the transaction node entirely
        transactionRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(DEBUG_TAG, "Transaction " + transaction.getKey() + " cancelled/deleted successfully.");
                    restoreItem(transaction, ownerKey);
                    transactionList.remove(transaction);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Request cancelled. Item restored to market.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(DEBUG_TAG, "Failed to delete transaction: " + e.getMessage());
                    Toast.makeText(context, "Failed to cancel request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Helper to restore the complete Item object from transaction data back to the categories node.
     * @param transaction The source transaction data containing the full item.
     * @param ownerKey The UID of the transaction owner (seller) - not used anymore.
     */
    private void restoreItem(Transaction transaction, String ownerKey) {
        // Get the complete item from transaction
        Item restoredItem = transaction.getItem();

        if (restoredItem == null) {
            Log.e("DECLINE_TX", "Error: No item data stored in transaction. Cannot restore item.");
            Toast.makeText(context, "Error: Item data missing in transaction.", Toast.LENGTH_SHORT).show();
            return;
        }

        String categoryName = transaction.getCategoryName();
        String itemId = transaction.getItemId();

        // Get reference to the item's location in the category
        DatabaseReference itemRef = FirebaseDatabase.getInstance()
                .getReference("categories")
                .child(categoryName)
                .child("items")  // FIXED: was "transactions", now correctly "items"
                .child(itemId);

        // Write the complete Item object back to the database
        itemRef.setValue(restoredItem)
                .addOnSuccessListener(aVoid -> {
                    Log.d("DECLINE_TX", "Item '" + transaction.getItemName() + "' fully restored to category.");
                    Toast.makeText(context, "Request declined. Item is now available.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("DECLINE_TX", "Failed to restore item: " + e.getMessage());
                    Toast.makeText(context, "Error: Item restoration failed.", Toast.LENGTH_SHORT).show();
                });
    }
}
