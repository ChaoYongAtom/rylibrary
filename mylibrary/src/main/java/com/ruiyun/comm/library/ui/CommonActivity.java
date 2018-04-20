package com.ruiyun.comm.library.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.ruiyun.comm.library.R;
import com.ruiyun.comm.library.listerers.BackHandledInterface;


/**
 * CommonActivity
 * 通用activity 展示fragment
 * 2017-06-05
 * wcy
 * 重庆锐云科技有限公司
 * 友商云V1.0
 */
public class CommonActivity extends BaseActivity implements BackHandledInterface {
    private LibFragment mBackHandedFragment;
    public static final String EXTRA_FRAGMENT = "fragmentName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);
        initView();
    }

    protected void initView() {
        Intent intent = getIntent();
        try {
            String fragmentClazz = intent
                    .getStringExtra(EXTRA_FRAGMENT);
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction();
            Fragment fragment = (Fragment) Class.forName(fragmentClazz)
                    .newInstance();
            fragment.setArguments(intent.getExtras());
            transaction.add(R.id.common_frame, fragment, fragmentClazz);
            transaction.addToBackStack(fragmentClazz);
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mBackHandedFragment != null)
            mBackHandedFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setSelectedFragment(Fragment selectedFragment) {
        this.mBackHandedFragment = (LibFragment) selectedFragment;
    }

    @Override
    public void onBackPressed() {
        if (mBackHandedFragment != null) {
            mBackHandedFragment.onBackPressed();
        } else {
            finishActivity();
        }
    }

    public void backBtnClick(View v) {
        onBackPressed();
    }
}
