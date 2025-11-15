package edu.uga.cs.tradeit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Simple placeholder screen to indicate the user is authenticated
 */
public class AuthScreenFragment extends Fragment {
    private FirebaseAuth mAuth;

    public AuthScreenFragment() {
    }

    public static AuthScreenFragment newInstance() {
        return new AuthScreenFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.auth_screen_test, container, false);
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

        if (currentUser != null) {
            // If they set a display name show it on the screen
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                nameTextView.setText("Hello, " + displayName);
            } else {
                // Fallback
                String email = currentUser.getEmail();
                if (email != null) {
                    nameTextView.setText("Hello, " + email);
                } else {
                    nameTextView.setText("Hello, User");
                }
            }
        }

        // Logout button click listener
        view.findViewById(R.id.lgoutButton).setOnClickListener(new OnClickSignOut());
    }

    private class OnClickSignOut implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mAuth.signOut();

            // Navigate back to sign in screen
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.container, new SignInFragment())
                    .commit();
        }
    }
}
