package com.ruiyun.comm.library.utils;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.apkfuns.logutils.LogUtils;
import com.ruiyun.comm.library.api.SubjectPostApi;
import com.ruiyun.comm.library.api.entitys.BaseResult;
import com.ruiyun.comm.library.common.JConstant;
import com.ruiyun.comm.library.mvp.BaseView;
import com.ruiyun.comm.library.widget.ProgressDialogView;

import org.wcy.android.retrofit.Api.BaseApi;
import org.wcy.android.retrofit.exception.ApiException;
import org.wcy.android.retrofit.exception.CodeException;
import org.wcy.android.retrofit.http.HttpManager;
import org.wcy.android.retrofit.listener.HttpOnNextListener;
import org.wcy.android.utils.AESOperator;
import org.wcy.android.utils.ExampleUtil;
import org.wcy.android.utils.PreferenceUtils;
import org.wcy.android.utils.RxActivityTool;
import org.wcy.android.utils.RxDataTool;
import org.wcy.android.view.dialog.RxDialogSure;

import java.math.BigDecimal;
import java.util.Set;


/**
 * 网络请求
 * Created by wcy on 2017/9/13.
 */

public class HttpUtil implements HttpOnNextListener {
    //    公用一个HttpManager
    private HttpManager manager;
    //    post请求接口信息
    protected SubjectPostApi postEntity;
    private Context application;
    ProgressDialogView progressDialogView;
    private BaseView httpOnListener;
    private static String heards = null;

    public HttpUtil(AppCompatActivity activity, BaseView onListener) {
        this.application = activity;
        this.httpOnListener = onListener;
        manager = new HttpManager(activity, this, getHeaders());
        postEntity = new SubjectPostApi();
        postEntity.setBaseUrl(JConstant.getHttpUrl());
        progressDialogView = new ProgressDialogView(activity);
        postEntity.setProgressDialog(progressDialogView);
    }

    /**
     * http请求头部信息并加密
     **/
    public String getHeaders() {
        if (RxDataTool.isNullString(heards)) {
            heards = PreferenceUtils.getValue(application, JConstant.heards, "");
            if (RxDataTool.isNullString(heards)) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("systemType", "1");
                    object.put("appVersion", RxActivityTool.getAppVersionCode(application));
                    object.put("mobileCode", ExampleUtil.getImei(application));
                    object.put("version", JConstant.getVersionName());
                    object.put("registrationID", JConstant.getRegistrationID());
                    if (JConstant.isEncrypt()) {
                        heards = AESOperator.encrypt(object.toJSONString());
                    } else {
                        heards = object.toJSONString();
                    }
                    PreferenceUtils.setValue(application, JConstant.heards, heards);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.i("heards====", heards);
        }
        return heards;
    }

    /**
     * @param method         请求指令
     * @param parameters     参数
     * @param cl             返回参数类型
     * @param isList         是否返回集合
     * @param isShowProgress 是否显示加载框
     * @param toast          加载框提示信息
     */
    public void send(String method, JSONObject parameters, Class<?> cl, boolean isList, boolean isShowProgress, String toast) {
        postEntity.setParameters(method, parameters);
        postEntity.setData(cl);
        postEntity.setList(isList);
        postEntity.setShowProgress(isShowProgress);
        postEntity.setMsg(toast);
        if (progressDialogView != null) {
            progressDialogView.setMsg(toast);
        }
        LogUtils.d("http------------->" + postEntity.getBaseUrl() + "/" + method + "/" + (parameters == null ? "" : parameters.toJSONString()));
        manager.doHttpDeal(postEntity);
    }

    @Override
    public void onNext(BaseApi api, String result) {
        try {
            LogUtils.d("result------------->", result);
            BaseResult baseResult = JSONObject.parseObject(result, BaseResult.class);
            if (baseResult == null) {
                baseResult = new BaseResult();
                baseResult.setResult(api.getData().newInstance());
                httpOnListener.onError(new ApiException(null, CodeException.JSON_ERROR, "数据解析错误"), api.getMethod());
            } else {
                if (baseResult.getCode() == 200) {
                    if (api.getData() != null) {
                        String dataJson = String.valueOf(baseResult.getResult());
                        try {
                            dataJson = AESOperator.decrypt(dataJson);
                        } catch (Exception e) {
                            dataJson = String.valueOf(baseResult.getResult());
                        } finally {
                            if (RxDataTool.isNullString(dataJson)) {
                                dataJson = String.valueOf(baseResult.getResult());
                            }
                            baseResult.setData(dataJson);
                        }
                        LogUtils.json(dataJson);
                        if (api.isList()) {
                            baseResult.setResult(JSONObject.parseArray(dataJson, api.getData()));
                        } else {
                            if (api.getData() == BigDecimal.class || api.getData() == String.class || api.getData() == Integer.class) {
                                dataJson = getData(dataJson);
                            }
                            if (api.getData() == BigDecimal.class) {
                                baseResult.setResult(new BigDecimal(dataJson));
                            } else if (api.getData() == String.class) {
                                baseResult.setResult(dataJson);
                            } else if (api.getData() == Integer.class) {
                                baseResult.setResult(Integer.parseInt(dataJson));
                            } else {
                                baseResult.setResult(JSONObject.parseObject(dataJson, api.getData()));
                            }
                        }

                    }
                    baseResult.setMethod(api.getMethod());
                    httpOnListener.onNext(baseResult);
                } else if (baseResult.getCode() == 101 || baseResult.getCode() == 102 || baseResult.getCode() == 103) {
                    final RxDialogSure rxDialogSure = new RxDialogSure(application);
                    rxDialogSure.setContent(baseResult.getMsg());
                    rxDialogSure.setSure("立即重新登录");
                    rxDialogSure.setCancelable(false);
                    rxDialogSure.setSureListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            rxDialogSure.dismiss();
                            if (JConstant.getLoinOutInterface() != null) {
                                JConstant.getLoinOutInterface().loginOut();
                            }
                        }
                    });
                    rxDialogSure.show();
                } else {
                    httpOnListener.onError(new ApiException(null, CodeException.ERROR, baseResult.getMsg()), api.getMethod());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            httpOnListener.onError(new ApiException(null, CodeException.ERROR, "数据处理异常，请稍后再试"), api.getMethod());
        }
    }

    private String getData(String json) {
        String dataValue = "";
        try {
            JSONObject dataObj = JSON.parseObject(json);
            if (dataObj.size() == 1) {
                Set<String> sIterator = dataObj.keySet();
                for (String str : sIterator) {
                    dataValue = dataObj.get(str).toString();
                }
            } else {
                dataValue = json;
            }
        } catch (Exception e) {
            dataValue = json;
        }
        return dataValue;
    }

    @Override
    public void onError(ApiException e, String mothead) {
        if (httpOnListener != null) {
            httpOnListener.onError(e, mothead);
        }
    }
}