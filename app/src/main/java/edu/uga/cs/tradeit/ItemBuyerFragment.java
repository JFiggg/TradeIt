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

import edu.uga.cs.tradeit.objects.Transaction;
import edu.uga.cs.tradeit.recyclers.ItemBuyerRecyclerAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ItemBuyerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ItemBuyerFragment extends Fragment {
    private static final String DEBUG_TAG = "ItemBuyerFragment";

    private RecyclerView recyclerView;
    private ItemBuyerRecyclerAdapter adapter;
    private List<Transaction> transactionList;
    private FirebaseDatabase database;

    public static ItemBuyerFragment newInstance() {
        ItemBuyerFragment fragment = new ItemBuyerFragment();
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
        View view = inflater.inflate(R.layout.fragment_item_buyer, container, false);

        recyclerView = view.findViewById(R.id.itemBuyerRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        transactionList = new ArrayList<>();
        adapter = new ItemBuyerRecyclerAdapter(transactionList, getContext(), this);
        recyclerView.setAdapter(adapter);

        // Get current user to filter items by owner
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(DEBUG_TAG, "No user logged in");
            return view;
        }
        String currentUserId = currentUser.getUid();

        database = FirebaseDatabase.getInstance();
        DatabaseReference transactionsRef = database.getReference("transactions");

        transactionsRef.orderByChild("recipient").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        transactionList.clear();

                        for (DataSnapshot txSnapshot : dataSnapshot.getChildren()) {
                            Transaction transaction = txSnapshot.getValue(Transaction.class);

                            if (transaction != null) {
                                transaction.setKey(txSnapshot.getKey());

                                // Manual filtering for 'pending' status
                                if ("pending".equalsIgnoreCase(transaction.getStatus())) {
                                    transactionList.add(transaction);
                                    Log.d(DEBUG_TAG, "Loaded pending request for item: " + transaction.getItemName());
                                }
                            }
                        }

                        // Sort transactions by timestamp from newest to oldest
                        Collections.sort(transactionList, new Comparator<Transaction>() {
                            @Override
                            public int compare(Transaction t1, Transaction t2) {
                                // Sorts by newest first
                                return Long.compare(t2.getTimestamp(), t1.getTimestamp());
                            }
                        });

                        adapter.notifyDataSetChanged();
                        Log.d(DEBUG_TAG, "Pending requests count: " + transactionList.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(DEBUG_TAG, "Error loading seller requests: " + databaseError.getMessage());
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Error loading requests.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return view;
    }
}