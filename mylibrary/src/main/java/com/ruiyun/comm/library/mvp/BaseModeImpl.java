package com.ruiyun.comm.library.mvp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.ruiyun.comm.library.api.entitys.BaseResult;
import com.ruiyun.comm.library.utils.HttpUtil;

import org.wcy.android.retrofit.exception.ApiException;
import org.wcy.android.utils.RxKeyboardTool;

/**
 * Created by wcy on 2018/1/19.
 */

public class BaseModeImpl implements BaseView {
    private BaseView onListener;
    private AppCompatActivity appCompatActivity;

    public void attachPresenter(BaseView view, AppCompatActivity activity) {
        onListener = view;
        appCompatActivity = activity;
    }


    public void sendPost(String method, JSONObject parameters, Class<?> cl) {
        sendPost(method, parameters, cl, false, true, null);
    }

    public void sendPost(String method, JSONObject parameters, Class<?> cl, boolean isList) {
        sendPost(method, parameters, cl, isList, true, null);
    }

    public void sendPost(String method, JSONObject parameters, Class<?> cl, boolean isList, boolean isShowProgress) {
        sendPost(method, parameters, cl, isList, isShowProgress, null);
    }

    public void sendPost(String method, JSONObject parameters, Class<?> cl, boolean isList, boolean isShowProgress, String toast) {
        HttpUtil httpUtil = new HttpUtil(appCompatActivity, this);
        RxKeyboardTool.hideSoftInput(appCompatActivity);
        httpUtil.send(method, parameters, cl, isList, isShowProgress, toast);
    }

    @Override
    public void onNext(BaseResult result) {
        onListener.onNext(result);
    }

    @Override
    public void onError(ApiException e, String mothead) {
        onListener.onError(e, mothead);
    }

    @Override
    public Context getThisContext() {
        return appCompatActivity.getApplicationContext();
    }
}
