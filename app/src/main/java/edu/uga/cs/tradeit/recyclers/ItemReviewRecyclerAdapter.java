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
import edu.uga.cs.tradeit.dialogs.ItemDialogFragment;
import edu.uga.cs.tradeit.objects.Item;

public class ItemReviewRecyclerAdapter extends RecyclerView.Adapter<ItemReviewRecyclerAdapter.ItemHolder> {

    private static final String DEBUG_TAG = "ItemReviewAdapter";
    private final List<Item> itemList;
    private final Context context;
    private final Fragment parentFragment;

    public ItemReviewRecyclerAdapter(List<Item> itemList, Context context, Fragment parentFragment) {
        this.itemList = itemList;
        this.context = context;
        this.parentFragment = parentFragment;
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        TextView itemNameTextView;
        TextView itemPriceTextView;
        TextView itemCategoryTextView;

        TextView itemCreatedAtTextView;

        TextView itemOwnerTextView;

        Button editButton;
        Button deleteButton;

        public ItemHolder(View view) {
            super(view);

            itemNameTextView = view.findViewById(R.id.itemNameTextView);
            itemPriceTextView = view.findViewById(R.id.itemPriceTextView);
            itemCategoryTextView = view.findViewById(R.id.itemCategoryTextView);
            itemCreatedAtTextView = view.findViewById(R.id.itemCreatedAtTextView);
            itemOwnerTextView = view.findViewById(R.id.itemOwnerTextView);
            editButton = view.findViewById(R.id.buttonEdit);
            deleteButton = view.findViewById(R.id.buttonDelete);

        }
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ItemReviewRecyclerAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        Item item = itemList.get(position);

        holder.itemNameTextView.setText(item.getName());


        if (item.isFree()) {
            holder.itemPriceTextView.setText("Free");
        } else {
            holder.itemPriceTextView.setText("Price: $" + item.getPrice());
        }

        holder.itemCategoryTextView.setText("Category: " + item.getCategoryName());

        long createdAt = item.getCreatedAt();
        if (createdAt > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(createdAt));
            holder.itemCreatedAtTextView.setText("Created: " + formattedDate);
        } else {
            holder.itemCreatedAtTextView.setText("Created: Unknown");
        }

        // Display owner name if available, otherwise show owner key
        String ownerDisplay = item.getOwnerName();
        if (ownerDisplay == null || ownerDisplay.isEmpty()) {
            ownerDisplay = item.getOwnerKey();
        }
        holder.itemOwnerTextView.setText("Owner: " + (ownerDisplay != null ? ownerDisplay : "Unknown"));


        holder.editButton.setOnClickListener(v -> {
            // Show ItemDialogFragment in edit mode
            ItemDialogFragment dialog = new ItemDialogFragment();
            dialog.setItemToEdit(item);
            dialog.show(parentFragment.getChildFragmentManager(), "EditItemDialog");
        });

        holder.deleteButton.setOnClickListener(v -> {
            // Show confirmation dialog before deleting
            new AlertDialog.Builder(context)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete \"" + item.getName() + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteItem(item);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    private void deleteItem(Item item) {
        if (item.getCategoryName() == null || item.getKey() == null) {
            Toast.makeText(context, "Error: Item missing category or key", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference itemRef = FirebaseDatabase.getInstance()
                .getReference("categories")
                .child(item.getCategoryName())
                .child("items")
                .child(item.getKey());

        itemRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(DEBUG_TAG, "Item deleted: " + item.getName());
                    Toast.makeText(context, "Item deleted: " + item.getName(), Toast.LENGTH_SHORT).show();
                    // Remove from local list
                    itemList.remove(item);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(DEBUG_TAG, "Error deleting item: " + e.getMessage());
                    Toast.makeText(context, "Failed to delete item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
