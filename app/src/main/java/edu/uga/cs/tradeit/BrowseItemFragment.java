package edu.uga.cs.tradeit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class BrowseItemFragment extends Fragment {

    private static final String DEBUG_TAG = "BrowseItemFragment";
    private static final String ARG_CATEGORY_NAME = "category_name";

    private RecyclerView recyclerView;
    private ItemBrowseRecyclerAdapter adapter;
    private List<Item> itemList;
    private FirebaseDatabase database;

    // Key name of the category we selected browse on
    private String categoryName;

    public static BrowseItemFragment newInstance(String categoryName) {
        BrowseItemFragment fragment = new BrowseItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_item, container, false);

        recyclerView = view.findViewById(R.id.itemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        itemList = new ArrayList<>();
        adapter = new ItemBrowseRecyclerAdapter(itemList, getContext());
        recyclerView.setAdapter(adapter);

        database = FirebaseDatabase.getInstance();
        DatabaseReference itemsRef = database.getReference("categories")
                .child(categoryName)
                .child("items");

        itemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Item item = snapshot.getValue(Item.class);
                    if (item != null) {
                        item.setKey(snapshot.getKey());
                        itemList.add(item);
                        Log.d(DEBUG_TAG, "Loaded item: " + item.getName());
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
                Log.d(DEBUG_TAG, "Item count: " + itemList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(DEBUG_TAG, "Error loading items: " + databaseError.getMessage());
            }
        });

        return view;
    }
}
