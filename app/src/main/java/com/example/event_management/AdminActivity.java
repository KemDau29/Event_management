package com.example.event_management;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.event_management.admin.AdminDashboardFragment;
import com.example.event_management.admin.AdminEventFragment;
import com.example.event_management.admin.AdminUserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.admin_nav_dashboard) {
                selectedFragment = new AdminDashboardFragment();
            } else if (itemId == R.id.admin_nav_events) {
                selectedFragment = new AdminEventFragment();
            } else if (itemId == R.id.admin_nav_users) {
                selectedFragment = new AdminUserFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.admin_fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_fragment_container, new AdminDashboardFragment())
                    .commit();
        }
    }
}
