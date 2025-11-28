package edu.uga.cs.tradeit;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.uga.cs.tradeit.objects.Transaction;
import edu.uga.cs.tradeit.recyclers.ItemHistoryRecyclerAdapter;
import edu.uga.cs.tradeit.recyclers.SellerItemRecyclerAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ItemHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ItemHistoryFragment extends Fragment {

    private static final String DEBUG_TAG = "ItemHistoryFragment";
    private RecyclerView recyclerView;
    private ItemHistoryRecyclerAdapter adapter;
    private List<Transaction> transactionList;
    private FirebaseDatabase database;
    private String currentUserId;


    // Counter to ensure we only update the UI after BOTH queries have completed
    private int queriesCompleted = 0;
    private final int totalQueries = 2;

    public static ItemHistoryFragment newInstance() {
        ItemHistoryFragment fragment = new ItemHistoryFragment();
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
        View view = inflater.inflate(R.layout.fragment_item_history, container, false);

        recyclerView = view.findViewById(R.id.itemHistoryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        transactionList = new ArrayList<>();
        adapter = new ItemHistoryRecyclerAdapter(transactionList, getContext(), this);
        recyclerView.setAdapter(adapter);

        // Get current user to filter items by owner
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(DEBUG_TAG, "No user logged in");
            return view;
        }
        currentUserId = currentUser.getUid();
        database = FirebaseDatabase.getInstance();

        transactionList.clear();
        queriesCompleted = 0;


        // 1. Load transactions where the user is the SENDER (Buyer)
        loadTransactions(database.getReference("transactions").orderByChild("sender").equalTo(currentUserId));

        // 2. Load transactions where the user is the RECIPIENT (Seller)
        loadTransactions(database.getReference("transactions").orderByChild("recipient").equalTo(currentUserId));

        return view;
    }

    /**
     * Executes a Firebase query and merges the results into the transactionList.
     * @param query The Firebase query filtered by either sender or recipient.
     */
    private void loadTransactions(Query query) { // CHANGED: DatabaseReference -> Query
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Use a temporary list to hold new items to avoid concurrent modification
                List<Transaction> newTransactions = new ArrayList<>();

                for (DataSnapshot txSnapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = txSnapshot.getValue(Transaction.class);

                    if (transaction != null) {
                        transaction.setKey(txSnapshot.getKey());

                        // We check for all non-pending statuses for history
                        String status = transaction.getStatus();
                        if ("accepted".equalsIgnoreCase(status) ||
                                "cancelled".equalsIgnoreCase(status) ||
                                "completed".equalsIgnoreCase(status)) {

                            // Check for duplicates before adding (since one user might be both sender and recipient for different items)
                            boolean isDuplicate = false;
                            for (Transaction existingTx : transactionList) {
                                if (existingTx.getKey().equals(transaction.getKey())) {
                                    isDuplicate = true;
                                    break;
                                }
                            }
                            if (!isDuplicate) {
                                newTransactions.add(transaction);
                                Log.d(DEBUG_TAG, "History Loaded: " + transaction.getItemName() + " (" + status + ")");
                            }
                        }
                    }
                }

                // Merge results after the loop finishes
                transactionList.addAll(newTransactions);

                // Check if all queries are complete
                checkAndFinishLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(DEBUG_TAG, "Error loading history query: " + databaseError.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading history data.", Toast.LENGTH_SHORT).show();
                }
                checkAndFinishLoading(); // Ensure we still try to finish if one query fails
            }
        });
    }

    /**
     * Increments the counter and updates the UI once all expected queries are finished.
     */
    private synchronized void checkAndFinishLoading() {
        queriesCompleted++;

        if (queriesCompleted == totalQueries) {
            // Sort the complete, merged list
            Collections.sort(transactionList, new Comparator<Transaction>() {
                @Override
                public int compare(Transaction t1, Transaction t2) {
                    // Sorts by newest first
                    return Long.compare(t2.getTimestamp(), t1.getTimestamp());
                }
            });

            adapter.notifyDataSetChanged();
            Log.d(DEBUG_TAG, "Total history transactions loaded: " + transactionList.size());
            // Optional: Show a friendly message if the list is empty
            if (transactionList.isEmpty()) {
                Toast.makeText(getContext(), "Your transaction history is currently empty.", Toast.LENGTH_LONG).show();
            }
        }
    }
}