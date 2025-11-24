package edu.uga.cs.tradeit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CategoryPostRecyclerAdapter extends RecyclerView.Adapter<CategoryPostRecyclerAdapter.CategoryHolder> {


    private List<Category> categoryList;
    private Context context;
    private PostFragment parentFragment;


    public CategoryPostRecyclerAdapter(List<Category> categoryList, Context context, PostFragment parentFragment ) {
        this.categoryList = categoryList;
        this.context = context;
        this.parentFragment = parentFragment;
    }

    class CategoryHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        TextView categoryOwnerTextView;
        TextView categoryCreatedAtTextView;

        TextView categoryItemCountTextView;
        Button editButton;
        Button deleteButton;
        Button addItemButton;

        public CategoryHolder(View view) {
            super(view);

            categoryNameTextView = view.findViewById(R.id.categoryNameTextView);
            categoryOwnerTextView = view.findViewById(R.id.categoryOwnerTextView);
            categoryCreatedAtTextView = view.findViewById(R.id.categoryCreatedAtTextView);
            categoryItemCountTextView = view.findViewById(R.id.categoryItemCount);
            editButton = view.findViewById(R.id.editButton);
            deleteButton = view.findViewById(R.id.deleteButton);
            addItemButton = view.findViewById(R.id.addItemButton);
        }
    }
    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
        View view = LayoutInflater.from( parent.getContext()).inflate( R.layout.category_post, parent, false );
        return new CategoryHolder( view );
    }

    // This method fills in the values of the Views to show a Category
    @Override
    public void onBindViewHolder( CategoryHolder holder, int position ) {
        Category category = categoryList.get( position );

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (category.getItems() != null) {
            holder.categoryItemCountTextView.setText("Item Count: " + category.getItems().size());
        }

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

        if (!category.getOwnerKey().equals(currentUser.getUid())) {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        } else {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);

            boolean hasItems = category.getItems() != null && !category.getItems().isEmpty();

            if (hasItems) {
                // Category has items - disable and grey out both edit and delete buttons
                holder.editButton.setEnabled(false);
                holder.editButton.setAlpha(0.5f);
                holder.deleteButton.setEnabled(false);
                holder.deleteButton.setAlpha(0.5f);
            } else {
                // Category has no items - enable both buttons
                holder.editButton.setEnabled(true);
                holder.editButton.setAlpha(1.0f);
                holder.deleteButton.setEnabled(true);
                holder.deleteButton.setAlpha(1.0f);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.editButton.setOnClickListener(v -> {
            CategoryDialogFragment dialog = new CategoryDialogFragment();
            dialog.setCategoryToEdit(category);   // pass the existing category
            dialog.show(parentFragment.getChildFragmentManager(), "EditCategory");
        });

        holder.deleteButton.setOnClickListener(v -> {

            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Delete Category")
                    .setMessage("Are you sure you want to delete \"" + category.getName() + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        parentFragment.deleteCategory(category); // ← CALL FRAGMENT
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });


        holder.addItemButton.setOnClickListener(v -> {
            ItemDialogFragment dialog = new ItemDialogFragment();
            dialog.setCategory(category);
            dialog.show(parentFragment.getChildFragmentManager(), "AddItemDialog");
        });

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}
