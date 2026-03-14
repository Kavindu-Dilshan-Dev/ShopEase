package com.kavindu.shopeaseapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.kavindu.shopeaseapp.R;
import com.kavindu.shopeaseapp.databinding.ActivityMainBinding;
import com.kavindu.shopeaseapp.fragments.HomeFragment;
import com.kavindu.shopeaseapp.fragments.OrdersFragment;
import com.kavindu.shopeaseapp.fragments.ProfileFragment;
import com.kavindu.shopeaseapp.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        loadFragment(new HomeFragment());

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return loadFragment(new HomeFragment());
            if (id == R.id.nav_search) return loadFragment(new SearchFragment());
            if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            }
            if (id == R.id.nav_orders) return loadFragment(new OrdersFragment());
            if (id == R.id.nav_profile) return loadFragment(new ProfileFragment());
            return false;
        });
    }

    private boolean loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        return true;
    }
}