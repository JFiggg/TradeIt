package edu.uga.cs.tradeit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.uga.cs.tradeit.objects.Category;
import edu.uga.cs.tradeit.recyclers.CategoryBrowseRecyclerAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BrowseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BrowseFragment extends Fragment {

    private static final String DEBUG_TAG = "ReviewCategoryFragment";

    private RecyclerView recyclerView;
    private CategoryBrowseRecyclerAdapter adapter;
    private List<Category> categoryList;
    private FirebaseDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_category, container, false);

        // Hide the Add Category button in browse view
        Button addCategoryButton = view.findViewById(R.id.addCategoryButton);
        if (addCategoryButton != null) {
            addCategoryButton.setVisibility(View.GONE);
        }

        recyclerView = view.findViewById(R.id.categoriesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        categoryList = new ArrayList<>();
        adapter = new CategoryBrowseRecyclerAdapter(categoryList, getContext(), this);
        recyclerView.setAdapter(adapter);

        database = FirebaseDatabase.getInstance();
        DatabaseReference categoriesRef = database.getReference("categories");

        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    if (category != null) {
                        // Set both key and name from snapshot key (they're the same now)
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
}