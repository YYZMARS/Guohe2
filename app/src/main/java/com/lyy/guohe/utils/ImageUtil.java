package com.lyy.guohe.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

public class ImageUtil {

    //将图像保存到本地
    public static void saveImage(Context context, ImageView imageView) {
        imageView.setDrawingCacheEnabled(true);//开启catch，开启之后才能获取ImageView中的bitmap
        Bitmap bitmap = imageView.getDrawingCache();//获取imageview中的图像
        MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "这是title", "这是description");
        imageView.setDrawingCacheEnabled(false);//关闭catch
    }

    //分享二维码
    public static void shareImg(Context context, ImageView imageView, String dlgTitle, String subject, String content) {

        imageView.setDrawingCacheEnabled(true);
        Bitmap bitmap = imageView.getDrawingCache();//获取imageview中的图像
        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "这是title", "这是description"));

        if (uri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (subject != null && !"".equals(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (content != null && !"".equals(content)) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }

        // 设置弹出框标题
        if (dlgTitle != null && !"".equals(dlgTitle)) { // 自定义标题
            context.startActivity(Intent.createChooser(intent, dlgTitle));
        } else { // 系统默认标题
            context.startActivity(intent);
        }
    }


}
