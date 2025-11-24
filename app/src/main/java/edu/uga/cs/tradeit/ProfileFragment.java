package edu.uga.cs.tradeit;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.uga.cs.tradeit.auth.AuthActivity;
import edu.uga.cs.tradeit.auth.SignInFragment;

/**
 * Simple placeholder screen to indicate the user is authenticated
 */
public class ProfileFragment extends Fragment {
    private FirebaseAuth mAuth;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Text view to show user's name
        TextView nameTextView = view.findViewById(R.id.nameTextView);
        TextView emailTextView = view.findViewById(R.id.emailTextView);
        TextView currentNameTextView = view.findViewById(R.id.currentNameTextView);
        Button reviewItemsButton = view.findViewById(R.id.reviewItemsButton);

        if (currentUser != null) {
            // If they set a display name show it on the screen
            String displayName = currentUser.getDisplayName();
            String emailDisplay = currentUser.getEmail();
            emailTextView.setText(emailDisplay);
            if (displayName != null && !displayName.isEmpty()) {
                nameTextView.setText("Hello, " + displayName);
                currentNameTextView.setText(displayName);
            } else {
                // Fallback
                String email = currentUser.getEmail();
                if (email != null) {
                    nameTextView.setText("Hello, " + email);
                    currentNameTextView.setText(email);
                } else {
                    nameTextView.setText("Hello, User");
                    currentNameTextView.setText("User");
                }
            }
        }

        // Go to new fragment to show the user's specific items
        reviewItemsButton.setOnClickListener(v -> {
            ItemReviewFragment fragment = ItemReviewFragment.newInstance();
            AppCompatActivity activity = (AppCompatActivity) getActivity();

            if (activity == null) {
                return;
            }

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
                overlayTitle.setText("My Items");
            }

            // Add fragment to overlay container
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentOverlayContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Logout button click listener
        view.findViewById(R.id.lgoutButton).setOnClickListener(new OnClickSignOut());
    }

    private class OnClickSignOut implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mAuth.signOut();

            // Navigate back to sign in screen
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            startActivity(intent);
        }
    }
}
