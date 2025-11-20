package edu.uga.cs.tradeit;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainVPAdapter extends FragmentStateAdapter {

    public MainVPAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new BrowseFragment();
            case 1:
                return new AddItemFragment();
            case 2:
                return new ProfileFragment();
            default:
                return new BrowseFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }


}
