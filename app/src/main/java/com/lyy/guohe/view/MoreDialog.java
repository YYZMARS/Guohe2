package com.lyy.guohe.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.flyco.animation.Attention.Flash;
import com.flyco.animation.Attention.Swing;
import com.flyco.animation.Attention.Tada;
import com.flyco.animation.BounceEnter.BounceBottomEnter;
import com.flyco.animation.ZoomEnter.ZoomInEnter;
import com.flyco.dialog.widget.base.BaseDialog;
import com.lyy.guohe.R;
import com.lyy.guohe.activity.BrowserActivity;
import com.lyy.guohe.activity.ClassRoomActivity;
import com.lyy.guohe.activity.GameActivity;
import com.lyy.guohe.activity.LibraryActivity;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.utils.NavigateUtil;

public class MoreDialog extends BaseDialog<MoreDialog> implements View.OnClickListener {

    private Context context;

    private LinearLayout navLibrary;
    private LinearLayout navClassroom;
    private LinearLayout navCet;
    private LinearLayout navGame;
    private LinearLayout navTel;
    private LinearLayout navPj;

    public MoreDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public View onCreateView() {
        widthScale(0.85f);
        showAnim(new ZoomInEnter());
        //填充弹窗布局
        View inflate = View.inflate(context, R.layout.dialog_more, null);
        navLibrary = inflate.findViewById(R.id.nav_library);
        navClassroom = inflate.findViewById(R.id.nav_classroom);
        navCet = inflate.findViewById(R.id.nav_cet);
        navGame = inflate.findViewById(R.id.nav_game);
        navTel = inflate.findViewById(R.id.nav_tel);
        navPj = inflate.findViewById(R.id.nav_pj);

        return inflate;
    }

    @Override
    public void setUiBeforShow() {
        navLibrary.setOnClickListener(this);
        navClassroom.setOnClickListener(this);
        navCet.setOnClickListener(this);
        navGame.setOnClickListener(this);
        navTel.setOnClickListener(this);
        navPj.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_library:
                NavigateUtil.navigateTo((Activity) context, LibraryActivity.class);
                dismiss();
                break;
            case R.id.nav_classroom:
                NavigateUtil.navigateTo((Activity) context, ClassRoomActivity.class);
                dismiss();
                break;
            case R.id.nav_cet:
                toCET();
                dismiss();
                break;
            case R.id.nav_game:
                showGameDialog();
                break;
            case R.id.nav_tel:
                toTel();
                dismiss();
                break;
            case R.id.nav_pj:
                Toast.makeText(context, "敬请期待", Toast.LENGTH_SHORT).show();
                dismiss();
                break;
        }
    }

    //跳转至校园热线页面
    private void toTel() {
        Intent intent = new Intent((Activity) context, BrowserActivity.class);
        intent.putExtra("url", UrlConstant.SCHOOL_TEL);
        intent.putExtra("title", "校园热线");
        intent.putExtra("isVpn", false);
        context.startActivity(intent);
    }

    //跳转至四六级查询部分
    private void toCET() {
        Intent intent = new Intent((Activity) context, BrowserActivity.class);
        intent.putExtra("url", UrlConstant.CET);
        intent.putExtra("title", "全国大学英语四六级考试成绩查询");
        intent.putExtra("isVpn", false);
        context.startActivity(intent);
    }

    //弹出选择小游戏对话框
    private void showGameDialog() {
        final String[] items = {"六角消除", "2048", "六角拼拼", "无尽之旅", "彩虹穿越", "西部枪手"};
        AlertDialog.Builder listDialog = new AlertDialog.Builder((Activity) context);
        listDialog.setTitle("请选择你要进入的游戏");
        listDialog.setItems(items, (dialog, which) -> {
            // which 下标从0开始
            // ...To-do
            Intent intent = new Intent(context, GameActivity.class);
            intent.putExtra("which", which);
            context.startActivity(intent);

        });
        listDialog.show();
    }
}
