package com.lyy.guohe.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.lyy.guohe.R;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.utils.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "PlayFragment";

    private Context mContext;

    private String headPicUrl;

    private View view;
    private ImageView mIvPgHeader;
    private CardView mCard1;
    private CardView mCard2;
    private CardView mCard3;
    private CardView mCard4;
    private CardView mCard5;
    private ImageView mIvCard1;
    private ImageView mIvCard2;
    private ImageView mIvCard3;
    private ImageView mIvCard4;
    private ImageView mIvCard5;

    public PlayFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        headPicUrl = UrlConstant.HEAD_PIC;

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_play, container, false);

        initView(view);
        getPic();

        return view;
    }

//    //弹出图片功能对话框
//    private void showPicDialog() {
//        final String[] stringItems = {"分享", "下载到本地", "设为壁纸"};
//        final ActionSheetDialog dialog = new ActionSheetDialog(getActivity(), stringItems, null);
//        dialog.isTitleShow(false).show();
//
//        dialog.setOnOperItemClickL((parent, view, position, id) -> {
//            switch (position) {
//                case 0:
//                    mIvPgHeader.setDrawingCacheEnabled(true);
//                    Bitmap bitmap = mIvPgHeader.getDrawingCache();//获取imageview中的图像
//                    Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(Objects.requireNonNull(getActivity()).getContentResolver(), bitmap, "这是title", "这是description"));
//                    shareImg("果核 - 每日一图", "我的主题", "我的分享内容", uri);
//                    break;
//                case 1:
//                    saveImage(mIvPgHeader);
//                    Toasty.success(mContext, "图片保存成功", Toast.LENGTH_SHORT).show();
//                    break;
//                case 2:
//                    setWallpaper1(mIvPgHeader);
//                    Toasty.success(mContext, "壁纸设置成功", Toast.LENGTH_SHORT).show();
//                    break;
//            }
//            dialog.dismiss();
//        });
//    }
//
//    //分享ImageView中的图片
//    private void shareImg(String dlgTitle, String subject, String content,
//                          Uri uri) {
//        if (uri == null) {
//            return;
//        }
//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType("image/*");
//        intent.putExtra(Intent.EXTRA_STREAM, uri);
//        if (subject != null && !"".equals(subject)) {
//            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
//        }
//        if (content != null && !"".equals(content)) {
//            intent.putExtra(Intent.EXTRA_TEXT, content);
//        }
//
//        // 设置弹出框标题
//        if (dlgTitle != null && !"".equals(dlgTitle)) { // 自定义标题
//            startActivity(Intent.createChooser(intent, dlgTitle));
//        } else { // 系统默认标题
//            startActivity(intent);
//        }
//    }
//
//    //保存imageview中的图片
//    private void saveImage(ImageView imageView) {
//        imageView.setDrawingCacheEnabled(true);//开启catch，开启之后才能获取ImageView中的bitmap
//        Bitmap bitmap = imageView.getDrawingCache();//获取imageview中的图像
//        MediaStore.Images.Media.insertImage(Objects.requireNonNull(getActivity()).getContentResolver(), bitmap, "这是title", "这是description");
//        imageView.setDrawingCacheEnabled(false);//关闭catch
//    }
//
//    //设置壁纸
//    private void setWallpaper1(ImageView imageView) {
//        imageView.setDrawingCacheEnabled(true);//开启catch，开启之后才能获取ImageView中的bitmap
//        Bitmap bmp = imageView.getDrawingCache();//获取imageview中的图像
//        try {
//            getActivity().setWallpaper(bmp);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    //获取广告图
    private void getPic() {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> Glide.with(mContext).load(headPicUrl).into(mIvPgHeader));
    }

    //初始化各控件
    private void initView(View view) {
        mIvPgHeader = view.findViewById(R.id.iv_pg_header);
        mIvPgHeader.setOnClickListener(this);
        mCard1 = (CardView) view.findViewById(R.id.card1);
        mCard1.setOnClickListener(this);
        mCard2 = (CardView) view.findViewById(R.id.card2);
        mCard2.setOnClickListener(this);
        mCard3 = (CardView) view.findViewById(R.id.card3);
        mCard3.setOnClickListener(this);
        mCard4 = (CardView) view.findViewById(R.id.card4);
        mCard4.setOnClickListener(this);
        mCard5 = (CardView) view.findViewById(R.id.card5);
        mCard5.setOnClickListener(this);
        mIvCard1 = (ImageView) view.findViewById(R.id.iv_card1);
        mIvCard2 = (ImageView) view.findViewById(R.id.iv_card2);
        mIvCard3 = (ImageView) view.findViewById(R.id.iv_card3);
        mIvCard4 = (ImageView) view.findViewById(R.id.iv_card4);
        mIvCard5 = (ImageView) view.findViewById(R.id.iv_card5);
        Glide.with(mContext).load(R.drawable.card1).into(mIvCard1);
        Glide.with(mContext).load(R.drawable.card2).into(mIvCard2);
        Glide.with(mContext).load(R.drawable.card3).into(mIvCard3);
        Glide.with(mContext).load(R.drawable.card4).into(mIvCard4);
        Glide.with(mContext).load(R.drawable.card5).into(mIvCard5);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.card1:
                break;
            case R.id.card2:
                break;
            case R.id.card3:
                break;
            case R.id.card4:
                break;
            case R.id.card5:
                break;
            case R.id.iv_pg_header:
                break;
        }
    }
}
