package edu.uga.cs.tradeit.recyclers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.objects.Transaction;

public class ItemHistoryRecyclerAdapter extends RecyclerView.Adapter<ItemHistoryRecyclerAdapter.ItemHolder> {

    private static final String DEBUG_TAG = "HistoryItemRecyclerAdapter";
    private final List<Transaction> transactionList;
    private final Context context;
    private final Fragment parentFragment;

    public ItemHistoryRecyclerAdapter(List<Transaction> transactionList, Context context, Fragment parentFragment) {
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
        TextView transactionBuyerTextView;
        TextView transactionStatusTextView;

        public ItemHolder(View view) {
            super(view);

            transactionNameTextView = view.findViewById(R.id.itemNameTextView);
            transactionPriceTextView = view.findViewById(R.id.itemPriceTextView);
            transactionCategoryTextView = view.findViewById(R.id.itemCategoryTextView);
            transactionCreatedAtTextView = view.findViewById(R.id.itemCreatedAtTextView);
            transactionOwnerTextView = view.findViewById(R.id.itemOwnerTextView);
            transactionBuyerTextView = view.findViewById(R.id.itemBuyerTextView);
            transactionStatusTextView = view.findViewById(R.id.statusTextView);

        }
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.transactionNameTextView.setText(transaction.getItemName());


        if (transaction.getAmount() == 0.0) {
            holder.transactionPriceTextView.setText("Free");
        } else {
            holder.transactionPriceTextView.setText("Price: $" + transaction.getAmount());
        }

        holder.transactionCategoryTextView.setText("Category: " + transaction.getCategoryName());

        long createdAt = transaction.getTimestamp();
        if (createdAt > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(createdAt));
            holder.transactionCreatedAtTextView.setText("Created: " + formattedDate);
        } else {
            holder.transactionCreatedAtTextView.setText("Created: Unknown");
        }

        // Display seller name
        String sellerDisplay = transaction.getSenderDisplayName();
        if (sellerDisplay == null || sellerDisplay.isEmpty()) {
            sellerDisplay = transaction.getSender();
        }
        holder.transactionOwnerTextView.setText("Seller: " + (sellerDisplay != null ? sellerDisplay : "Unknown"));

        // Display buyer name
        String buyerDisplay = transaction.getRecipientDisplayName();
        if (buyerDisplay == null || buyerDisplay.isEmpty()) {
            buyerDisplay = transaction.getRecipient();
        }
        holder.transactionBuyerTextView.setText("Buyer: " + (buyerDisplay != null ? buyerDisplay : "Unknown"));

        holder.transactionStatusTextView.setText("Status: " + transaction.getStatus());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }
}
