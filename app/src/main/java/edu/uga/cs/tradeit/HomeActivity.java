package edu.uga.cs.tradeit;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import edu.uga.cs.tradeit.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private ViewPager2 mainViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mainViewPager = findViewById(R.id.mainViewPager);
        mainViewPager.setAdapter( new MainVPAdapter(this) );
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
    }


}