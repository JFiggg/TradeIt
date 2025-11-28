package edu.uga.cs.tradeit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.uga.cs.tradeit.auth.AuthActivity;
import edu.uga.cs.tradeit.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private ViewPager2 mainViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is logged in
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, redirect to AuthActivity
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mainViewPager = findViewById(R.id.mainViewPager);
        mainViewPager.setAdapter(new MainVPAdapter(this));
        mainViewPager.setCurrentItem(2, false);
        binding.bottomNavigationView.setSelectedItemId(R.id.profile);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.browse) {
                mainViewPager.setCurrentItem(0, true);
                return true;
            } else if (id == R.id.post) {
                mainViewPager.setCurrentItem(1, true);
                return true;
            } else if (id == R.id.profile) {
                mainViewPager.setCurrentItem(2, true);
                return true;
            }
            return false;
        });
        mainViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        binding.bottomNavigationView.setSelectedItemId(R.id.browse);
                        break;
                    case 1:
                        binding.bottomNavigationView.setSelectedItemId(R.id.post);
                        break;
                    case 2:
                        binding.bottomNavigationView.setSelectedItemId(R.id.profile);
                        break;
                }
            }
        });

        // I had to look this up because I couldn't figure out how to replace the fragments
        // On the navbar without creating a whole new activity
        // Feel free to change it

        // Listen to back stack changes to automatically manage overlay visibility
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
            if (backStackCount > 0) {
                // There are fragments in the back stack, show overlay
                binding.fragmentOverlayContainer.setVisibility(View.VISIBLE);
                binding.overlayToolbar.setVisibility(View.VISIBLE);

                // Restore title if fragment is BrowseItemFragment
                Fragment currentFragment = getSupportFragmentManager()
                        .findFragmentById(R.id.fragmentOverlayContainer);
                if (currentFragment instanceof BrowseItemFragment) {
                    // Get category name from fragment arguments
                    Bundle args = currentFragment.getArguments();
                    if (args != null) {
                        String categoryName = args.getString("category_name");
                        if (categoryName != null) {
                            binding.overlayTitle.setText(categoryName);
                        }
                    }
                }
            } else {
                // No fragments in back stack, hide overlay
                binding.fragmentOverlayContainer.setVisibility(View.GONE);
                binding.overlayToolbar.setVisibility(View.GONE);
            }
        });

        // Handle configuration changes (rotation) - restore overlay state
        if (savedInstanceState != null) {
            // FragmentManager will restore fragments automatically
            // Just need to make sure overlay visibility is correct
            getSupportFragmentManager().executePendingTransactions();
            int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
            if (backStackCount > 0) {
                binding.fragmentOverlayContainer.setVisibility(View.VISIBLE);
                binding.overlayToolbar.setVisibility(View.VISIBLE);

                // Restore title
                Fragment currentFragment = getSupportFragmentManager()
                        .findFragmentById(R.id.fragmentOverlayContainer);
                if (currentFragment instanceof BrowseItemFragment) {
                    Bundle args = currentFragment.getArguments();
                    if (args != null) {
                        String categoryName = args.getString("category_name");
                        if (categoryName != null) {
                            binding.overlayTitle.setText(categoryName);
                        }
                    }
                }
            }
        }

        // Set up global overlay back button
        binding.overlayBackButton.setOnClickListener(v -> {
            // Pop the back stack (listener will handle hiding overlay)
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            }
        });

        // Handle system back button for overlay
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check if overlay is visible
                if (binding.fragmentOverlayContainer.getVisibility() == View.VISIBLE) {
                    // Pop the back stack (listener will handle hiding overlay)
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStack();
                    }
                } else {
                    // Default back button behavior
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }


}