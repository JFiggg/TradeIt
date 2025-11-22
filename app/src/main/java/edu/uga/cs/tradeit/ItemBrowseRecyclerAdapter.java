package edu.uga.cs.tradeit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemBrowseRecyclerAdapter extends RecyclerView.Adapter<ItemBrowseRecyclerAdapter.ItemHolder> {

    private List<Item> itemList;
    private Context context;

    public ItemBrowseRecyclerAdapter(List<Item> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        TextView itemNameTextView;
        TextView itemPriceTextView;
        TextView itemCategoryTextView;

        TextView itemCreatedAtTextView;

        TextView itemOwnerTextView;

        public ItemHolder(View view) {
            super(view);

            itemNameTextView = view.findViewById(R.id.itemNameTextView);
            itemPriceTextView = view.findViewById(R.id.itemPriceTextView);
            itemCategoryTextView = view.findViewById(R.id.itemCategoryTextView);
            itemCreatedAtTextView = view.findViewById(R.id.itemCreatedAtTextView);
            itemOwnerTextView = view.findViewById(R.id.itemOwnerTextView);

        }
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from( parent.getContext()).inflate( R.layout.item_browse, parent, false );
        return new ItemBrowseRecyclerAdapter.ItemHolder( view );
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        Item item = itemList.get(position);

        holder.itemNameTextView.setText(item.getName());


        if (item.isFree()) {
            holder.itemPriceTextView.setText("Free");
        } else {
            holder.itemPriceTextView.setText(String.valueOf(item.getPrice()));
        }

        holder.itemCategoryTextView.setText( item.getCategoryName());

        long createdAt = item.getCreatedAt();
        if (createdAt > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(createdAt));
            holder.itemCreatedAtTextView.setText("Created: " + formattedDate);
        } else {
            holder.itemCreatedAtTextView.setText("Created: Unknown");
        }

        // Todo -> Name instead of id
        holder.itemOwnerTextView.setText(item.getOwnerKey());


    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


}
