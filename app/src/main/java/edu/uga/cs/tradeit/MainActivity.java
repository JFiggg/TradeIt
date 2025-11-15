package edu.uga.cs.tradeit;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "MainActivity";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
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
            navDemoAuthScreen();
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

    // A simple demo screen to show their name & logout button
    private void navDemoAuthScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new AuthScreenFragment())
                .commit();
    }

    // Sign in screen nav button
    private void navSignInScreen() {
        // Show sign in fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new SignInFragment())
                .commit();
    }
}