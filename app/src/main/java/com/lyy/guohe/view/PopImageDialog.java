package com.lyy.guohe.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.flyco.animation.ZoomEnter.ZoomInEnter;
import com.flyco.dialog.widget.base.BaseDialog;
import com.lyy.guohe.R;
import com.lyy.guohe.utils.DialogUtils;

public class PopImageDialog extends BaseDialog<PopImageDialog> {

    private Context context;

    private Bitmap bitmap;

    private ImageView iv_pop;

    public PopImageDialog(Context context, Bitmap bitmap) {
        super(context);
        this.context = context;
        this.bitmap = bitmap;
    }

    @Override
    public View onCreateView() {
        showAnim(new ZoomInEnter());
        View inflate = View.inflate(context, R.layout.dialog_pop_image, null);
        iv_pop = (ImageView) inflate.findViewById(R.id.iv_pop);
        iv_pop.setImageBitmap(bitmap);
        return inflate;
    }

    @Override
    public void setUiBeforShow() {
        iv_pop.setOnLongClickListener(view -> {
            DialogUtils.showOneDialog((Activity) context, iv_pop);
            return true;
        });
    }
}
