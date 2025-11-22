package edu.uga.cs.tradeit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CategoryBrowseRecyclerAdapter extends RecyclerView.Adapter<CategoryBrowseRecyclerAdapter.CategoryHolder> {
    private List<Category> categoryList;
    private Context context;

    public CategoryBrowseRecyclerAdapter(List<Category> categoryList, Context context ) {
        this.categoryList = categoryList;
        this.context = context;
    }

    class CategoryHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        TextView categoryOwnerTextView;
        TextView categoryCreatedAtTextView;
        Button browseButton;

        public CategoryHolder(View view) {
            super(view);

            categoryNameTextView = view.findViewById(R.id.categoryNameTextView);
            categoryOwnerTextView = view.findViewById(R.id.categoryOwnerTextView);
            categoryCreatedAtTextView = view.findViewById(R.id.categoryCreatedAtTextView);
            browseButton = view.findViewById(R.id.browseButton);
        }
    }

    @NonNull
    @Override
    public CategoryBrowseRecyclerAdapter.CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
        View view = LayoutInflater.from( parent.getContext()).inflate( R.layout.category_browse, parent, false );
        return new CategoryBrowseRecyclerAdapter.CategoryHolder( view );
    }

    // This method fills in the values of the Views to show a Category
    @Override
    public void onBindViewHolder(@NonNull CategoryHolder holder, int position ) {
        Category category = categoryList.get( position );

        String key = category.getKey();
        String name = category.getName();
        String ownerKey = category.getOwnerKey();
        long createdAt = category.getCreatedAt();

        holder.categoryNameTextView.setText( category.getName());

        // Set owner text
        if (ownerKey != null && !ownerKey.isEmpty()) {
            holder.categoryOwnerTextView.setText("Owner: " + ownerKey);
        } else {
            holder.categoryOwnerTextView.setText("Owner: Unknown");
        }

        // Format and set created at timestamp
        if (createdAt > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(createdAt));
            holder.categoryCreatedAtTextView.setText("Created: " + formattedDate);
        } else {
            holder.categoryCreatedAtTextView.setText("Created: Unknown");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}
