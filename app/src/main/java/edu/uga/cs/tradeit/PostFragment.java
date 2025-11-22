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

/**
 * Placeholder screen to view all categories
 */
public class PostFragment extends Fragment implements CategoryDialogFragment.AddCategoryDialogListener {

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

        DatabaseReference newCategoryRef = categoriesRef.push();

        category.setKey(newCategoryRef.getKey());

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
                    Toast.makeText(getContext(), "Failed to add category", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void updateCategory(Category category) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("categories");

        if (category.getKey() == null) {
            Toast.makeText(getContext(), "Error: category has no key", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update only the name + timestamp
        ref.child(category.getKey()).child("name").setValue(category.getName());
        ref.child(category.getKey()).child("createdAt").setValue(System.currentTimeMillis());

        Toast.makeText(getContext(), "Category updated", Toast.LENGTH_SHORT).show();
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
}
