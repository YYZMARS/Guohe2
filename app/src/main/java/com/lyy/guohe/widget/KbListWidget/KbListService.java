package com.lyy.guohe.widget.KbListWidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class KbListService extends RemoteViewsService{
    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new KbListFactory(this.getApplicationContext(), intent);
    }
}
