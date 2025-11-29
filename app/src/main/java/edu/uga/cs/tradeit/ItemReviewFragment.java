package edu.uga.cs.tradeit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import edu.uga.cs.tradeit.dialogs.ItemDialogFragment;
import edu.uga.cs.tradeit.objects.Item;
import edu.uga.cs.tradeit.recyclers.ItemReviewRecyclerAdapter;

public class ItemReviewFragment extends Fragment implements ItemDialogFragment.ItemDialogListener {

    private static final String DEBUG_TAG = "ItemReviewFragment";

    private RecyclerView recyclerView;
    private ItemReviewRecyclerAdapter adapter;
    private List<Item> itemList;
    private FirebaseDatabase database;

    private View emptyView;

    public static ItemReviewFragment newInstance() {
        ItemReviewFragment fragment = new ItemReviewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_item, container, false);

        recyclerView = view.findViewById(R.id.reviewItemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyView = view.findViewById(R.id.emptyListView);
        itemList = new ArrayList<>();
        adapter = new ItemReviewRecyclerAdapter(itemList, getContext(), this);
        recyclerView.setAdapter(adapter);

        // Get current user to filter items by owner
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(DEBUG_TAG, "No user logged in");
            return view;
        }
        String currentUserId = currentUser.getUid();

        database = FirebaseDatabase.getInstance();
        DatabaseReference categoriesRef = database.getReference("categories");

        // Listen to all categories and filter items by current user
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();

                // Loop through all categories
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    // Get the items node for this category
                    DataSnapshot itemsSnapshot = categorySnapshot.child("items");

                    // Loop through all items in this category
                    for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                        Item item = itemSnapshot.getValue(Item.class);
                        if (item != null) {
                            item.setKey(itemSnapshot.getKey());

                            // Only add items owned by the current user
                            if (item.getOwnerKey() != null && item.getOwnerKey().equals(currentUserId)) {
                                itemList.add(item);
                                Log.d(DEBUG_TAG, "Loaded user's item: " + item.getName());
                            }
                        }
                    }
                }

                // Sort items from newest to oldest
                Collections.sort(itemList, new Comparator<Item>() {
                    @Override
                    public int compare(Item i1, Item i2) {
                        return Long.compare(i2.getCreatedAt(), i1.getCreatedAt());
                    }
                });

                adapter.notifyDataSetChanged();
                Log.d(DEBUG_TAG, "User's item count: " + itemList.size());

                if (itemList.isEmpty()) {
                    android.widget.TextView emptyTextView = emptyView.findViewById(R.id.emptyListTextView);
                    emptyTextView.setText("You have not listed any items");

                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(DEBUG_TAG, "Error loading items: " + databaseError.getMessage());
            }
        });

        return view;
    }

    @Override
    public void addItem(Item item) {
        // This fragment is for reviewing items, not adding
        // Items should be added from PostFragment
        Toast.makeText(getContext(), "Cannot add items from this view", Toast.LENGTH_SHORT).show();
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
                    // List will auto-update via ValueEventListener
                })
                .addOnFailureListener(e -> {
                    Log.e(DEBUG_TAG, "Error updating item: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to update item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
