package com.lyy.guohe.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.flyco.dialog.widget.base.BaseDialog;
import com.lyy.guohe.R;
import com.lyy.guohe.constant.UrlConstant;

public class EggDialog extends BaseDialog<EggDialog> {

    private Context context;
    private ImageView iv_egg;

    public EggDialog(Context context) {
        super(context);
        this.context = context;
    }

    //该方法用来出来数据初始化代码
    @Override
    public View onCreateView() {

        RequestOptions options = new RequestOptions()
                .centerCrop()
                //.placeholder(R.mipmap.ic_launcher_round)
                .priority(Priority.HIGH)
                //.skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        widthScale(0.85f);
        //填充弹窗布局
        View inflate = View.inflate(context, R.layout.dialog_egg, null);
        //用来放整个图片的控件
        iv_egg = (ImageView) inflate.findViewById(R.id.iv_egg);
        //用来加载网络图片，填充iv_ad控件，注意要添加网络权限，和Picasso的依赖和混淆
        Glide.with(context).load(UrlConstant.EGG).apply(options).into(iv_egg);
//        Glide.with(context).load(R.drawable.ic_menu_update).into(iv_back);

        return inflate;
    }

    //该方法用来处理逻辑代码
    @Override
    public void setUiBeforShow() {
        //点击弹窗相应位置，处理相关逻辑。
        iv_egg.setOnClickListener(v -> {
//            Toast.makeText(context, "哈哈", Toast.LENGTH_SHORT).show();
            //处理完逻辑关闭弹框的代码
            dismiss();
        });
    }
}
