package com.lyy.guohe.service;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.lyy.guohe.widget.KbListWidget.KbListFactory;

public class KbListService extends RemoteViewsService{
    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new KbListFactory(this.getApplicationContext(), intent);
    }
}
