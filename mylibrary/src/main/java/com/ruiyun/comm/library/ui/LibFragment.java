package com.ruiyun.comm.library.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ruiyun.comm.library.R;
import com.ruiyun.comm.library.api.entitys.BaseResult;
import com.ruiyun.comm.library.listerers.BackHandledInterface;
import com.ruiyun.comm.library.mvp.BaseView;

import org.wcy.android.retrofit.exception.ApiException;
import org.wcy.android.utils.RxActivityTool;
import org.wcy.android.view.HeaderLayout;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by wcy on 2018/1/18.
 */

public abstract class LibFragment extends Fragment implements BaseView {
    protected BackHandledInterface mBackHandledInterface;
    private BaseActivity activity;
    protected HeaderLayout headerLayout;
    private Unbinder unbinder;
    protected View rootView;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.finishInputWindow();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (BaseActivity) getActivity();
        if (!(getActivity() instanceof BackHandledInterface)) {
            // throw new ClassCastException("Hosting Activity must implement BackHandledInterface");
        } else {
            this.mBackHandledInterface = (BackHandledInterface) getActivity();
        }
    }

    public void finishFramager() {
        if (activity.getSupportFragmentManager().getBackStackEntryCount() == 1) {
            RxActivityTool.finishActivity(activity);
        } else {
            try {
                activity.getSupportFragmentManager().popBackStack();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        activity.finishInputWindow();
    }

    public void finishActivity() {
        RxActivityTool.finishActivity(getActivity());
    }

    public void startActivity(Class<?> c, boolean isclose, Bundle bundle) {
        activity.startActivity(c, isclose, bundle);
    }

    public BaseActivity getThisActivity() {
        return activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mBackHandledInterface != null) {
            //告诉FragmentActivity，当前Fragment在栈顶
            mBackHandledInterface.setSelectedFragment(this);
        }

    }

    public void toast(Object obj) {
        activity.toast(obj);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (headerLayout != null) {
            headerLayout.getNavigationView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishFramager();
                }
            });
        }
        initView();
    }

    public View setView(LayoutInflater inflater, int layoutId) {
        return setView(inflater, layoutId, null);
    }

    public View setView(LayoutInflater inflater, int layoutId, String title) {
        return setView(inflater, null, layoutId, title);
    }

    public View setView(LayoutInflater inflater, ViewGroup container, int layoutId, String title) {
        if (rootView == null) {
            rootView = inflater.inflate(layoutId, container, false);
            headerLayout = rootView.findViewById(R.id.headerlayout);
            unbinder = ButterKnife.bind(this, rootView);
            initTitle(title);
        }

        return rootView;
    }

    protected abstract void initTitle(String title);

    protected abstract void initView();


    /**
     * 所有继承BackHandledFragment的子类都将在这个方法中实现物理Back键按下后的逻辑
     * FragmentActivity捕捉到物理返回键点击事件后会首先询问Fragment是否消费该事件
     * 如果没有Fragment消息时FragmentActivity自己才会消费该事件
     */
    public abstract boolean onBackPressed();

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) unbinder.unbind();
    }

    @Override
    public void onNext(BaseResult result) {
        toastSuccess(result.getMsg());
    }

    @Override
    public void onError(ApiException e, String mothead) {
        toastError(e.getDisplayMessage());

    }

    public void toastError(Object obj) {
        activity.toastError(obj);
    }

    public void toastSuccess(Object obj) {
        activity.toastSuccess(obj);
    }

    @Override
    public Context getThisContext() {
        return getThisActivity().getThisContext();
    }

    public HeaderLayout getHeaderLayout() {
        return headerLayout;
    }

}
