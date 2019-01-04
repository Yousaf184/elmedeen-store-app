package com.example.yousafkhan.elmedeenappstore.recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.yousafkhan.elmedeenappstore.Interfaces.RecyclerViewItemClick;
import com.example.yousafkhan.elmedeenappstore.Models.App;
import com.example.yousafkhan.elmedeenappstore.R;

import java.util.List;

public class RcAdapter extends RecyclerView.Adapter<RcAdapter.RcViewHolder> {

    private List<App> appList;
    private Context context;

    private RecyclerViewItemClick recyclerViewItemClick;

    public RcAdapter(List<App> dataList, RecyclerViewItemClick rc) {
        this.appList = dataList;
        this.recyclerViewItemClick = rc;
    }

    @NonNull
    @Override
    public RcViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        View v = LayoutInflater.from(context)
                               .inflate(R.layout.recyclerview_item, viewGroup, false);

        return new RcViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RcViewHolder rcViewHolder, int i) {
        App app = appList.get(i);
        rcViewHolder.setData(app);

        // set transition name for shared element transition on RecyclerView item
        // should be unique for each row's ImageView
        ViewCompat.setTransitionName(rcViewHolder.appTitleImage, app.getAppPackageName());
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public class RcViewHolder extends RecyclerView.ViewHolder {

        private ImageView appTitleImage;
        private TextView appTitle;
        private TextView appAPKSize;

        public RcViewHolder(@NonNull View itemView) {
            super(itemView);
            appTitleImage = itemView.findViewById(R.id.app_title_image);
            appTitle = itemView.findViewById(R.id.app_name_text);
            appAPKSize = itemView.findViewById(R.id.app_size_text);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerViewItemClick.itemClicked(getAdapterPosition(), appTitleImage);
                }
            });
        }

        public void setData(App app) {
            Glide.with(context)
                    .load(app.getAppTitleImageURL())
                    .into(appTitleImage);
            appTitle.setText(app.getAppNameEng());
            appAPKSize.setText(app.getApk_size());
        }
    }
}
