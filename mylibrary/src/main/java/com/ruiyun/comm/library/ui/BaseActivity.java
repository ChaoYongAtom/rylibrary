package com.ruiyun.comm.library.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.ruiyun.comm.library.R;
import com.ruiyun.comm.library.api.entitys.BaseResult;
import com.ruiyun.comm.library.mvp.BaseView;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import org.wcy.android.retrofit.exception.ApiException;
import org.wcy.android.utils.RxActivityTool;
import org.wcy.android.utils.RxDataTool;
import org.wcy.android.utils.RxKeyboardTool;
import org.wcy.android.utils.StatusBarUtil;
import org.wcy.android.view.HeaderLayout;
import org.wcy.android.view.toast.ToastUtils;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 公共baseactivity
 * Created by wcy on 2018/1/18.
 */

public abstract class BaseActivity extends RxAppCompatActivity implements BaseView {
    Unbinder unbinder;
    private HeaderLayout headerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxActivityTool.setScreenVertical(this);//竖屏显示
        finishInputWindow();//隐藏输入法
        RxActivityTool.addActivity(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        init();
    }

    @Override
    public void setContentView(View view) {
        // TODO Auto-generated method stub
        super.setContentView(view);
        init();
    }

    public void setView(int layoutID, String title) {
        setContentView(layoutID);
        headerLayout = findViewById(R.id.headerlayout);
        if (headerLayout != null) {
            if (!RxDataTool.isNullString(title)) headerLayout.setTitleText(title);
            headerLayout.getNavigationView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishActivity();
                }
            });
        }
        init();
    }

    private void init() {
        setStatusBar();
        unbinder = ButterKnife.bind(this);
    }


    /**
     * 关闭当前activity
     */
    public void finishActivity() {
        finishInputWindow();
        RxActivityTool.finishActivity(this);
    }

    /**
     * 关闭软键盘
     **/
    public void finishInputWindow() {
        RxKeyboardTool.hideSoftInput(this);
    }
    public void startActivity(Class<?> c, boolean isclose) {
        startActivity(new Intent(this, c));
        if (isclose) {
            finishActivity();
        }
    }

    public void startActivity(Class<?> c, boolean isclose, Bundle bundle) {
        Intent intent = new Intent(this, c);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        if (isclose) {
            finishActivity();
        }
    }

    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.white));
    }


    @Override
    public void onNext(BaseResult result) {

    }

    @Override
    public void onError(ApiException e, String mothead) {
        toastError(e.getDisplayMessage());
    }

    private void showToast(Object obj, int type) {
        if (obj != null && !RxDataTool.isEmpty(obj)) {
            ToastUtils.show(this, obj.toString(), type);
        } else {
            ToastUtils.show(this, "数据异常", ToastUtils.ERROR_TYPE);
        }
    }

    public void toastError(Object obj) {
        showToast(obj, ToastUtils.ERROR_TYPE);
    }

    public void toastSuccess(Object obj) {
        showToast(obj, ToastUtils.SUCCESS_TYPE);
    }

    public void toast(Object obj) {
        showToast(obj, ToastUtils.NO);
    }

    @Override
    public Context getThisContext() {
        return this;
    }


    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        Configuration configuration = new Configuration();
        configuration.setToDefaults();
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return resources;
    }

    public HeaderLayout getHeaderLayout() {
        return headerLayout;
    }
}
