package edu.uga.cs.tradeit.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.uga.cs.tradeit.HomeActivity;
import edu.uga.cs.tradeit.R;

public class AuthActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "MainActivity";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check the current user's auth state
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // User is signed in
            Log.d(DEBUG_TAG, "Authenticated! Welcome");
            navProfileScreen();
        } else {
            // User isn't signed in
            Log.d(DEBUG_TAG, "Not authenticated");
            navSignInScreen();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // Navigate to the profile within the app
    private void navProfileScreen() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    // Sign in screen nav button
    private void navSignInScreen() {
        // Show sign in fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new SignInFragment())
                .commit();
    }
}