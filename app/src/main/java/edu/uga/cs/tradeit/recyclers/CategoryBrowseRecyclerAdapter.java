package edu.uga.cs.tradeit.recyclers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.uga.cs.tradeit.BrowseItemFragment;
import edu.uga.cs.tradeit.R;
import edu.uga.cs.tradeit.objects.Category;

public class CategoryBrowseRecyclerAdapter extends RecyclerView.Adapter<CategoryBrowseRecyclerAdapter.CategoryHolder> {
    private final List<Category> categoryList;
    private final Context context;
    private final Fragment parentFragment;

    public CategoryBrowseRecyclerAdapter(List<Category> categoryList, Context context, Fragment parentFragment) {
        this.categoryList = categoryList;
        this.context = context;
        this.parentFragment = parentFragment;
    }

    class CategoryHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        TextView categoryOwnerTextView;
        TextView categoryCreatedAtTextView;
        Button browseButton;
        TextView categoryItemCountTextView;

        public CategoryHolder(View view) {
            super(view);

            categoryNameTextView = view.findViewById(R.id.categoryNameTextView);
            categoryOwnerTextView = view.findViewById(R.id.categoryOwnerTextView);
            categoryItemCountTextView = view.findViewById(R.id.categoryItemCount);
            categoryCreatedAtTextView = view.findViewById(R.id.categoryCreatedAtTextView);
            browseButton = view.findViewById(R.id.browseButton);
        }
    }

    @NonNull
    @Override
    public CategoryBrowseRecyclerAdapter.CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_browse, parent, false);
        return new CategoryBrowseRecyclerAdapter.CategoryHolder(view);
    }

    // This method fills in the values of the Views to show a Category
    @Override
    public void onBindViewHolder(@NonNull CategoryHolder holder, int position) {
        Category category = categoryList.get(position);

        String key = category.getKey();
        String name = category.getName();
        String ownerKey = category.getOwnerKey();
        long createdAt = category.getCreatedAt();

        holder.categoryItemCountTextView.setText("Item Count: " + category.getItemCount());

        holder.categoryNameTextView.setText(category.getName());

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

        // This will show an overlay fragment over the navbar fragments
        // I had to look this up, not sure if its best solution ATM
        holder.browseButton.setOnClickListener(v -> {
            // Navigate to BrowseItemFragment using activity's fragment manager
            BrowseItemFragment browseItemFragment = BrowseItemFragment.newInstance(category.getName());
            AppCompatActivity activity = (AppCompatActivity) holder.itemView.getContext();

            // Show the overlay container and toolbar
            View overlayContainer = activity.findViewById(R.id.fragmentOverlayContainer);
            View overlayToolbar = activity.findViewById(R.id.overlayToolbar);
            TextView overlayTitle = activity.findViewById(R.id.overlayTitle);

            if (overlayContainer != null) {
                overlayContainer.setVisibility(View.VISIBLE);
            }
            if (overlayToolbar != null) {
                overlayToolbar.setVisibility(View.VISIBLE);
            }
            if (overlayTitle != null) {
                overlayTitle.setText(category.getName());
            }

            // Add fragment to overlay container
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentOverlayContainer, browseItemFragment)
                    .addToBackStack(null)
                    .commit();
        });

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}
