package edu.uga.cs.tradeit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.uga.cs.tradeit.dialogs.CategoryDialogFragment;
import edu.uga.cs.tradeit.dialogs.ItemDialogFragment;
import edu.uga.cs.tradeit.objects.Category;
import edu.uga.cs.tradeit.objects.Item;
import edu.uga.cs.tradeit.recyclers.CategoryPostRecyclerAdapter;

/**
 * Placeholder screen to view all categories
 */
public class PostFragment extends Fragment implements CategoryDialogFragment.AddCategoryDialogListener, ItemDialogFragment.ItemDialogListener {

    private static final String DEBUG_TAG = "ReviewCategoryFragment";

    private RecyclerView recyclerView;
    private CategoryPostRecyclerAdapter adapter;

    private Button addCategoryButton;
    private List<Category> categoryList;

    private FirebaseDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_category, container, false);

        recyclerView = view.findViewById(R.id.categoriesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        categoryList = new ArrayList<>();
        adapter = new CategoryPostRecyclerAdapter(categoryList, getContext(), this);
        recyclerView.setAdapter(adapter);

        addCategoryButton = view.findViewById(R.id.addCategoryButton);
        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CategoryDialogFragment dialog = new CategoryDialogFragment();
                dialog.show(getChildFragmentManager(), "AddCategoryDialog");
            }
        });

        database = FirebaseDatabase.getInstance();
        DatabaseReference categoriesRef = database.getReference("categories");

        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    if (category != null) {
                        category.setKey(snapshot.getKey());
                        categoryList.add(category);
                        Log.d(DEBUG_TAG, "Loaded categories: " + category.getName());
                    }
                }

                // Sort categories alphabetically by name
                Collections.sort(categoryList, new Comparator<Category>() {
                    @Override
                    public int compare(Category c1, Category c2) {
                        return c1.getName().compareToIgnoreCase(c2.getName());
                    }
                });

                adapter.notifyDataSetChanged();
                Log.d(DEBUG_TAG, "Category count: " + categoryList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(DEBUG_TAG, "Error: failed to load categories: " + databaseError.getMessage());
            }
        });

        return view;
    }

    @Override
    public void addCategory(Category category) {
        database = FirebaseDatabase.getInstance();
        DatabaseReference categoriesRef = database.getReference("categories");

        // Use the category name as the key to prevent duplicates
        String categoryName = category.getName();
        category.setKey(categoryName);
        DatabaseReference newCategoryRef = categoriesRef.child(categoryName);

        // Set owner and timestamp
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            category.setOwnerKey(currentUser.getUid());
        }
        category.setCreatedAt(System.currentTimeMillis());

        newCategoryRef.setValue(category)
                .addOnSuccessListener(aVoid -> {
                    Log.d(DEBUG_TAG, "Category added: " + category.getName());
                    Toast.makeText(getContext(), "Category added: " + category.getName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(DEBUG_TAG, "Error adding category: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to add category: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void updateCategory(Category category) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("categories");

        /*
        In Database categories stored as
        categories -> key -> categoryObject
         */
        String oldKey = category.getKey();
        String newName = category.getName();

        if (oldKey == null) {
            Toast.makeText(getContext(), "Error: category has no key", Toast.LENGTH_SHORT).show();
            return;
        }

        // If name changed, we need to delete old and create new (since name is the key)
        if (!oldKey.equals(newName)) {
            // First, get the old category data to preserve ownerKey
            ref.child(oldKey).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    Category oldCategory = snapshot.getValue(Category.class);
                    if (oldCategory != null) {
                        // Delete old category
                        ref.child(oldKey).removeValue();

                        // Create new category with new name as key
                        category.setKey(newName);
                        // Same owner
                        category.setOwnerKey(oldCategory.getOwnerKey());
                        // Created at should remain unchanged
                        category.setCreatedAt(oldCategory.getCreatedAt());

                        // Update the category map with the new category
                        ref.child(newName).setValue(category)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Category renamed", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to rename: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            });
        } else {
            // Name unchanged, just update timestamp
            ref.child(oldKey).child("createdAt").setValue(System.currentTimeMillis());
            Toast.makeText(getContext(), "Category updated", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void deleteCategory(Category category) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference categoriesRef = database.getReference("categories");

        if (category.getKey() == null) {
            Toast.makeText(getContext(), "Error: Category key missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        categoriesRef.child(category.getKey()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Category deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete category", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void addItem(Item item) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference categoriesRef = database.getReference("categories");

        String categoryName = item.getCategoryName();
        if (categoryName == null || categoryName.isEmpty()) {
            Toast.makeText(getContext(), "Error: No category selected on item!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference itemsRef = categoriesRef.child(categoryName).child("items");
        DatabaseReference newItemRef = itemsRef.push();

        item.setKey(newItemRef.getKey());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            item.setOwnerKey(currentUser.getUid());
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                item.setOwnerName(displayName);
            } else {
                String email = currentUser.getEmail();
                item.setOwnerName(email != null ? email : "Unknown");
            }
        }
        item.setCreatedAt(System.currentTimeMillis());

        newItemRef.setValue(item)
                .addOnSuccessListener(aVoid -> {
                    Log.d(DEBUG_TAG, "Item added: " + item.getName());
                    Toast.makeText(getContext(), "Item added: " + item.getName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(DEBUG_TAG, "Error adding item: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to add item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void updateItem(Item item) {
        if (item.getCategoryName() == null || item.getKey() == null) {
            Toast.makeText(getContext(), "Error: Item missing category or key", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference itemRef = database.getReference("categories")
                .child(item.getCategoryName())
                .child("items")
                .child(item.getKey());

        itemRef.setValue(item)
                .addOnSuccessListener(aVoid -> {
                    Log.d(DEBUG_TAG, "Item updated: " + item.getName());
                    Toast.makeText(getContext(), "Item updated: " + item.getName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(DEBUG_TAG, "Error updating item: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to update item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
