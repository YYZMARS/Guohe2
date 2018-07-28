package com.lyy.guohe.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.Toast;

import com.flyco.animation.BounceEnter.BounceBottomEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.flyco.dialog.widget.MaterialDialog;
import com.lyy.guohe.activity.BrowserActivity;
import com.lyy.guohe.activity.GameActivity;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.view.EggDialog;
import com.lyy.guohe.view.MoreDialog;
import com.lyy.guohe.view.PopImageDialog;

import es.dmoral.toasty.Toasty;

//将果核里面的对话框都移植到这里
public class DialogUtils {

    //弹出支持捐赠对话框
    public static void showDonateDialog(Activity activity) {
        final MaterialDialog dialog = new MaterialDialog(activity);
        dialog.content(
                "假如此App为您带来了便利与舒适，假如您愿意支持我们。我们希望能得到小小的赞赏，这是一种莫大的肯定与鼓励。\n" +
                        "金钱是保持自由的一种工具，我们诚挚祈望未来与你同在。^ ^")//
                .btnText("关闭", "支持")//
                .show();

        //left btn click listener
        //right btn click listener
        dialog.setOnBtnClickL(
                dialog::dismiss,
                () -> {
                    if (RomUtils.checkApkExist(activity, "com.eg.android.AlipayGphone")) {
                        donate(activity);
                    } else {
                        Toasty.warning(activity, "本机未安装支付宝", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
        );
    }

    //跳转到支付宝付款界面
    private static void donate(Context context) {
        String intentFullUrl = "intent://platformapi/startapp?saId=10000007&" +
                "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2FFKX07846GRVQI6HABMOJ72%3F_s" +
                "%3Dweb-other&_t=1472443966571#Intent;" +
                "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
        try {
            Intent intent = Intent.parseUri(intentFullUrl, Intent.URI_INTENT_SCHEME);
            context.startActivity(intent);
        } catch (Exception e) {
            System.out.println("本机不支持使用支付宝");
        }
    }

    //弹出显示菜单的对话框
    public static void showEggDialog(Activity activity) {
        EggDialog eggDialog = new EggDialog(activity);
        eggDialog.setTitle("哇，你竟然发现了彩蛋");
        eggDialog.onCreateView();
        eggDialog.setUiBeforShow();
        //点击空白区域不能退出
        eggDialog.setCanceledOnTouchOutside(true);
        //按返回键不能退出
        eggDialog.setCancelable(true);
        eggDialog.show();
    }

    //显示课程信息的对话框
    public static void showCourseDialog(Activity activity, String courseMesg) {
        String[] courseInfo = courseMesg.split("@");
        String courseNum = "";
        String courseClassroom = "";
        String courseName = "";
        String courseTeacher = "";
        if (courseInfo.length == 1) {
            courseNum = courseInfo[0];
        }
        if (courseInfo.length == 2) {
            courseNum = courseInfo[0];
            courseName = courseInfo[1];
        }
        if (courseInfo.length == 3 || courseInfo.length == 4) {
            courseNum = courseInfo[0];
            courseName = courseInfo[1];
            courseTeacher = courseInfo[2];
        }
        if (courseInfo.length == 5) {
            courseNum = courseInfo[0];
            courseName = courseInfo[1];
            courseTeacher = courseInfo[2];
            courseClassroom = courseInfo[4];
        }

        final MaterialDialog dialog = new MaterialDialog(activity);

        dialog.isTitleShow(false)//
                .btnNum(1)
                .content("课程信息为：\n" + "课程号：\t" + courseNum + "\n课程名：\t" + courseName + "\n课程教师：\t" + courseTeacher + "\n教室：\t" + courseClassroom)
                .btnText("确定")//
                .showAnim(new BounceBottomEnter())
                .show();

        //left btn click listener
        dialog.setOnBtnClickL(
                (OnBtnClickL) dialog::dismiss
        );

    }

    //显示首页更多对话框
    public static void showMoreDialog(Activity activity) {
        MoreDialog moreDialog = new MoreDialog(activity);
        moreDialog.onCreateView();
        moreDialog.setUiBeforShow();
        moreDialog.setCanceledOnTouchOutside(true);
        moreDialog.setCancelable(true);
        moreDialog.show();
    }

    //选择进入哪一个校园系统
    public static void showSystemDialog(Activity activity) {
        final String[] items = {"教务系统", "奥兰系统", "实验系统", "一站式办事大厅"};
        AlertDialog.Builder listDialog = new AlertDialog.Builder(activity);
        listDialog.setTitle("选择要进入的系统");
        listDialog.setItems(items, (dialog, which) -> {
            // which 下标从0开始
            Intent intent = new Intent(activity, BrowserActivity.class);
            switch (which) {
                case 0:
                    intent.putExtra("url", UrlConstant.JIAOWU_URL);
                    intent.putExtra("title", "强智教务");
                    activity.startActivity(intent);
                    break;
                case 1:
                    intent.putExtra("url", UrlConstant.AOLAN_URL);
                    intent.putExtra("title", "奥兰系统");
                    activity.startActivity(intent);
                    break;
                case 2:
                    intent.putExtra("url", UrlConstant.LAB_URL);
                    intent.putExtra("title", "实验系统");
                    activity.startActivity(intent);
                    break;
                case 3:
                    intent.putExtra("url", UrlConstant.FUWU_URL);
                    intent.putExtra("title", "一站式办事大厅");
                    activity.startActivity(intent);
                    break;
            }
        });
        listDialog.show();
    }

    //显示校车对话框
    public static void showBusDialog(Activity activity, String mess) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(activity);
        normalDialog.setMessage("即将到来的车次是：\n" + mess);
        normalDialog.setPositiveButton("显示全部车次",
                (dialog, which) -> {
                    //...To-do
                    Intent intent = new Intent(activity, BrowserActivity.class);
                    intent.putExtra("title", "校车时刻表");
                    intent.putExtra("url", UrlConstant.SCHOOL_BUS);
                    intent.putExtra("isVpn", false);
                    activity.startActivity(intent);
                });
        normalDialog.setNegativeButton("关闭",
                (dialog, which) -> {
                    //...To-do
                    dialog.dismiss();
                });
        // 显示
        normalDialog.show();
    }

    //弹出选择小游戏对话框
    public static void showGameDialog(Activity activity) {
        final String[] items = {"六角消除", "2048", "六角拼拼", "无尽之旅", "彩虹穿越", "西部枪手"};
        AlertDialog.Builder listDialog = new AlertDialog.Builder(activity);
        listDialog.setTitle("请选择你要进入的游戏");
        listDialog.setItems(items, (dialog, which) -> {
            Intent intent = new Intent(activity, GameActivity.class);
            intent.putExtra("which", which);
            activity.startActivity(intent);

        });
        listDialog.show();
    }

    //显示One模块的对话框
    public static void showOneDialog(Activity activity, ImageView ivOneImg) {
        if (activity != null) {
            final String[] stringItems = {"分享", "下载到本地"};
            final ActionSheetDialog dialog = new ActionSheetDialog(activity, stringItems, null);
            dialog.isTitleShow(false).show();

            dialog.setOnOperItemClickL((parent, view1, position, id) -> {
                switch (position) {
                    case 0:
                        ImageUtil.shareImg(activity, ivOneImg, "果核", "我的主题", "我的分享内容");
                        break;
                    case 1:
                        ImageUtil.saveImage(activity, ivOneImg);
                        Toasty.success(activity, "图片保存成功", Toast.LENGTH_SHORT).show();
                        break;
                }
                dialog.dismiss();
            });
        }
    }

    //点击弹出Imageview的大图
    public static void showPopImageDialog(Activity activity, ImageView imageView) {
        imageView.setDrawingCacheEnabled(true);
        PopImageDialog popImageDialog = new PopImageDialog(activity, imageView.getDrawingCache());
        popImageDialog.onCreateView();
        popImageDialog.setUiBeforShow();
        popImageDialog.setCanceledOnTouchOutside(true);
        popImageDialog.setCancelable(true);
        popImageDialog.show();
    }


}
