package com.lyy.guohe.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.lyy.guohe.R;
import com.lyy.guohe.model.News;
import com.lyy.guohe.model.Sport;

import java.util.List;

public class NewsAdapter extends ArrayAdapter<News> {

    private int resourceId;
    private Context mContext;

    public NewsAdapter(Context context, int resource, List<News> objects) {
        super(context, resource, objects);
        resourceId = resource;
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        News news = getItem(position);
        @SuppressLint("ViewHolder") View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        ImageView iv_newsImg = view.findViewById(R.id.iv_newsImg);
        TextView iv_newsTitle = view.findViewById(R.id.iv_newsTitle);
        assert news != null;
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
        Glide.with(mContext).load(news.getImgUrl()).apply(options).into(iv_newsImg);
        iv_newsTitle.setText(news.getNewsTitle());
        return view;
    }
}
