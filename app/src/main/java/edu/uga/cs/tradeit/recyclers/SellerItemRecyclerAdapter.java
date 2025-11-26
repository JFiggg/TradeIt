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

        // Display owner name if available, otherwise show owner key
        String ownerDisplay = transaction.getRecipient();
        if (ownerDisplay == null || ownerDisplay.isEmpty()) {
            ownerDisplay = transaction.getRecipient();
        }
        holder.transactionOwnerTextView.setText("Owner: " + (ownerDisplay != null ? ownerDisplay : "Unknown"));

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

    private void declineRequest(Transaction transaction) {

        // Validate mandatory fields needed for operation
        if (transaction.getKey() == null) {
            Toast.makeText(context, "Failed to cancel: Transaction key missing.", Toast.LENGTH_SHORT).show();
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

                    // 3. Recreate (restore) the Item to the market
                    recreateItemFromTransaction(transaction);

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
     * Helper to RECREATE the Item object using data from the Transaction and push it back to the categories node.
     * This assumes the Item was DESTROYED when the request was made.
     * @param transaction The source transaction data.
     */
    private void recreateItemFromTransaction(Transaction transaction) {
        String categoryName = transaction.getCategoryName();
        String itemId = transaction.getItemId();
        String itemName = transaction.getItemName();
        Double amount = transaction.getAmount();
        String ownerKey = transaction.getSender(); // The recipient of the TX is the item owner (seller)

        if (categoryName == null || itemId == null || itemName == null || ownerKey == null) {
            Log.e(DEBUG_TAG, "Cannot recreate item: missing critical fields in transaction.");
            return;
        }

        // NOTE: DATA LOSS OCCURS HERE as only basic fields are restored ---
        boolean isFree = (amount == 0.0);
        Item restoredItem = new Item(itemName, amount, isFree, categoryName);
        restoredItem.setKey(itemId);
        restoredItem.setOwnerKey(ownerKey);
        restoredItem.setCreatedAt(System.currentTimeMillis());

        // Get reference to the item's original location
        DatabaseReference itemRef = FirebaseDatabase.getInstance()
                .getReference("categories")
                .child(categoryName)
                .child("items")
                .child(itemId);

        // Write the newly created Item object back to the database, making it available
        itemRef.setValue(restoredItem)
                .addOnSuccessListener(aVoid -> Log.d(DEBUG_TAG, "Item '" + itemName + "' recreated and restored."))
                .addOnFailureListener(e -> Log.e(DEBUG_TAG, "Failed to recreate item: " + e.getMessage()));
    }
}
