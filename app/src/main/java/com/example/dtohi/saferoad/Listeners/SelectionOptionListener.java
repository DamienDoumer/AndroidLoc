package com.example.dtohi.saferoad.Listeners;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.example.dtohi.saferoad.MapFragment;
import com.example.dtohi.saferoad.R;
import com.example.dtohi.saferoad.SafetyTipsFragment;

/**
 * Created by dtohi on 9/4/2017.
 */

public class SelectionOptionListener implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FragmentManager fragmentManager;

    public SelectionOptionListener(DrawerLayout drawer, FragmentManager fragmentManager){

        this.drawerLayout = drawer;
        this.fragmentManager = fragmentManager;

        //Set the map fragment as first fragment on load.
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, new MapFragment());
        ft.commit();
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;

        if (id == R.id.nav_map) {

            fragment = new MapFragment();
            // Handle the camera action

        } else if (id == R.id.safety_tips) {

            fragment = new SafetyTipsFragment();

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        //Switch the fragment
        if(fragment != null)
        {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
