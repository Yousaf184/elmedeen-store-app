package com.example.yousafkhan.elmedeenappstore.activities;

import android.app.ActivityOptions;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.yousafkhan.elmedeenappstore.Interfaces.RecyclerViewItemClick;
import com.example.yousafkhan.elmedeenappstore.Models.App;
import com.example.yousafkhan.elmedeenappstore.R;
import com.example.yousafkhan.elmedeenappstore.Utils.MyUtils;
import com.example.yousafkhan.elmedeenappstore.recyclerview.RcAdapter;
import com.example.yousafkhan.elmedeenappstore.view_models.HomeActivityVM;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements RecyclerViewItemClick {

    private ProgressBar progressBar;
    private TextView infoText;
    private Button retryButton;

    public static final String INTENT_EXTRA_KEY = "item_position";
    public static final String SHARED_IMAGE_TRANSITION_NAME = "sharedImageTransitionName";

    private HomeActivityVM viewmodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        progressBar = findViewById(R.id.progress_bar_home);
        infoText = findViewById(R.id.info_text);
        retryButton = findViewById(R.id.retry_button);
        viewmodel = ViewModelProviders.of(this).get(HomeActivityVM.class);

        // set toolbar as action bar
        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);

        if(MyUtils.isInternetConnected(this)) {
            setViewmodelAndObservers();
        } else {
            progressBar.setVisibility(View.GONE);
            CoordinatorLayout coordinatorLayout = findViewById(R.id.home_activity_parent_layout);
            Snackbar.make(coordinatorLayout, "Internet not connected", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void setViewmodelAndObservers() {

        viewmodel.getDataList().observe(this, new Observer<List<App>>() {
            @Override
            public void onChanged(@Nullable List<App> appList) {
                progressBar.setVisibility(View.GONE);

                if(appList.size() > 0) {
                    displayAppList(appList);
                } else {
                    infoText.setText("App store is currently empty");
                    infoText.setVisibility(View.VISIBLE);
                }
            }
        });

        viewmodel.getServer_error_message().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                progressBar.setVisibility(View.GONE);
                infoText.setText(s);
                infoText.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayAppList(List<App> dataList) {
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, calculateNoOfColumns());
        recyclerView.setLayoutManager(gridLayoutManager);

        RcAdapter adapter = new RcAdapter(dataList, this);
        recyclerView.setAdapter(adapter);
    }

    // called when any item in RecyclerView is clicked
    // called from RcAdapter.RcViewHolder class
    @Override
    public void itemClicked(int position, ImageView appTitleImage) {
        Intent openAppDetailActivity = new Intent(this, AppDetailsActivity.class);
        openAppDetailActivity.putExtra(INTENT_EXTRA_KEY, position);

        // for shared element transition via RecyclerView row on app title ImageView
        openAppDetailActivity.putExtra(
                SHARED_IMAGE_TRANSITION_NAME,
                ViewCompat.getTransitionName(appTitleImage)
        );

        // shared element transition
        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                this,
                appTitleImage,
                ViewCompat.getTransitionName(appTitleImage)
        );

        startActivity(openAppDetailActivity, activityOptions.toBundle());
    }

    public void tryLoadingDataAgain(View view) {
        progressBar.setVisibility(View.VISIBLE);
        infoText.setVisibility(View.INVISIBLE);
        retryButton.setVisibility(View.GONE);
        viewmodel.loadAllApps();
    }

    public int calculateNoOfColumns() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / 190); // 190 is the width of the grid item
    }
}
