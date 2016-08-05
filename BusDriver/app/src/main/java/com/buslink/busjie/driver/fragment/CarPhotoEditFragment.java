package com.buslink.busjie.driver.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;

import com.buslink.busjie.driver.R;
import com.buslink.busjie.driver.activity.HomeActivity;
import com.buslink.busjie.driver.activity.PickPhotoActivity;
import com.buslink.busjie.driver.constant.JsonName;
import com.buslink.busjie.driver.constant.Net;
import com.buslink.busjie.driver.db.UserHelper;
import com.buslink.busjie.driver.manager.RequestManager;
import com.buslink.busjie.driver.util.CameraUtil;
import com.buslink.busjie.driver.util.PhotoSelectOptions;
import com.buslink.busjie.driver.util.XString;
import com.buslink.busjie.driver.viewholder.SimpleHolder;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2015/11/23.
 */
public class CarPhotoEditFragment extends LevelTwoFragment {
    @Bind(R.id.rv)
    RecyclerView rv;

    List<JSONObject> list;
    RecyclerView.Adapter adapter;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rv.setLayoutManager(new LinearLayoutManager(mActivity));
        rv.setItemAnimator(new DefaultItemAnimator());
        list = new LinkedList<>();
        list.add(new JSONObject());
        list.add(new JSONObject());
        list.add(new JSONObject());
        list.add(new JSONObject());
        rv.setAdapter(adapter = new RecyclerView.Adapter<SimpleHolder>() {
            @Override
            public SimpleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                final SimpleHolder sh = new SimpleHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.i_photo, parent, false));
                sh.setTag(R.id.tv, R.id.iv, R.id.loading, R.id.iv_1);
                sh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doClick(sh);
                    }
                });
                sh.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        doLongCLick(sh);
                        return true;
                    }
                });
                return sh;
            }

            @Override
            public void onBindViewHolder(SimpleHolder holder, int position) {
                holder.setIndex(position);
                if (position == 0 &&
                        (!TextUtils.isEmpty(XString.getStr(list.get(position), "img")) || !TextUtils.isEmpty(XString.getStr(list.get(position), "photo")))) {
                    holder.getImageView(R.id.iv_1).setVisibility(View.VISIBLE);
                } else {
                    holder.getImageView(R.id.iv_1).setVisibility(View.GONE);
                }
                if (!TextUtils.isEmpty(XString.getStr(list.get(position), "img"))) {
                    holder.getImageView(R.id.iv).setImageBitmap(
                            CameraUtil.getCompressBitmap(
                                    XString.getStr(list.get(position), "img"), CameraUtil.ICON_SIZE, CameraUtil.ICON_SIZE));
                    if (XString.getBoolean(list.get(position), "loading")) {
                        RotateAnimation animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
                                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        animation.setRepeatCount(Animation.INFINITE);
                        animation.setDuration(2000);
                        holder.getImageView(R.id.loading).setVisibility(View.VISIBLE);
                        holder.getImageView(R.id.loading).startAnimation(animation);
                        holder.getTextView(R.id.tv).setText("图片正在上传");
                    } else {
                        holder.getImageView(R.id.loading).clearAnimation();
                        holder.getImageView(R.id.loading).setVisibility(View.GONE);
                        holder.getTextView(R.id.tv).setText(XString.getStr(list.get(position), "msg"));
                    }
                } else if (!TextUtils.isEmpty(XString.getStr(list.get(position), "photo"))) {
                    Picasso.with(mActivity)
                            .load(Net.IMGURL + XString.getStr(list.get(position), "photo"))
                            .into(holder.getImageView(R.id.iv));
                    holder.getTextView(R.id.tv).setText("点击更换图片");
                } else {
                    holder.getImageView(R.id.iv).setImageDrawable(new ColorDrawable(0));
                    holder.getImageView(R.id.loading).clearAnimation();
                    holder.getImageView(R.id.loading).setVisibility(View.GONE);
                    holder.getTextView(R.id.tv).setText("点击添加图片");
                }
            }

            @Override
            public int getItemCount() {
                return list.size();
            }

            void doClick(SimpleHolder vh) {
                Intent intent = new Intent(getActivity(), PickPhotoActivity.class);
                intent.putExtra("title", "获取图片");
                intent.putExtra("crop", true);
                intent.putExtra("option", PhotoSelectOptions.DEFALUT);
                intent.putExtra(PickPhotoActivity.FILE_NAME, String.format("car%d.jpg", vh.getIndex()));
                startActivityForResult(intent, vh.getIndex());
            }

            void doLongCLick(final SimpleHolder sh) {
                if (sh.getIndex() != 0 &&
                        (!TextUtils.isEmpty(XString.getStr(list.get(sh.getIndex()), "img")) || !TextUtils.isEmpty(XString.getStr(list.get(sh.getIndex()), "photo")))) {
                    new AlertDialog.Builder(mActivity)
                            .setTitle("提示")
                            .setMessage("设置图片为车辆头像")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setCarHead(sh.getIndex());
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                }
            }
        });
        getData();
    }

    @Override
    public String getTitle() {
        return "车辆图片";
    }

    @Override
    protected int getResLayout() {
        return 0;
    }

    @Override
    protected void initView() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.f_car_photo_edit, container, false);
        ButterKnife.bind(this, v);
        v.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            String result = data.getStringExtra(PickPhotoActivity.PHOTO_PATH);
            XString.put(list.get(requestCode), "img", result);
            XString.put(list.get(requestCode), "loading", true);
            if (!TextUtils.isEmpty(result)) {
                adapter.notifyItemChanged(requestCode);
                uploadImage(requestCode, result);
            }
        }
    }

    @OnClick(R.id.bt)
    void finsh() {
        mActivity.finish();
    }

    private void uploadImage(final int where, String img) {
        HttpUtils http = new HttpUtils();
        RequestParams params = new RequestParams();
        params.addBodyParameter("key", UserHelper.getInstance().getCid());
        params.addBodyParameter("isupdate", "1"); // 更新照片
        params.addBodyParameter("avatar", new File(img));
        if (where == 0) {
            params.addBodyParameter("imgtype", "3");
        } else {
            params.addBodyParameter("imgtype", String.valueOf(11 + where));
        }
        http.send(HttpRequest.HttpMethod.POST, Net.IMAGEUPFILEAND, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                JSONObject res = XString.getJSONObject(responseInfo.result);
                JSONObject data = XString.getJSONObject(res, JsonName.DATA);
                if (XString.getBoolean(res, JsonName.STATUS)) {
                    mActivity.app.toast("图片上传成功");
                    XString.put(list.get(where), "loading", false);
                    XString.put(list.get(where), "msg", "点击更换图片");
                } else {
                    mActivity.app.toast(XString.getStr(data, JsonName.MSG));
                    XString.put(list.get(where), "loading", true);
                    XString.put(list.get(where), "msg", "请重新选择图片");
                }
                adapter.notifyItemChanged(where);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                mActivity.app.toast("你的网络不给力");
                XString.put(list.get(where), "loading", true);
                XString.put(list.get(where), "msg", "请重新选择图片");
                adapter.notifyItemChanged(where);
            }
        });
    }


    private void getData() {
        HttpUtils http = new HttpUtils();
        RequestParams params = RequestManager.simplePostParams();
        params.addBodyParameter(JsonName.CARID, UserHelper.getInstance().getCid());
        http.send(HttpRequest.HttpMethod.POST, Net.CAR_IMAGES, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                JSONObject res = XString.getJSONObject(responseInfo.result);
                JSONObject data = XString.getJSONObject(res, JsonName.DATA);
                Log.d("img", responseInfo.result);
                if (XString.getBoolean(res, JsonName.STATUS)) {
                    XString.put(list.get(0), "photo", XString.getStr(data, JsonName.CAR_HEAD));
                    XString.put(list.get(1), "photo", XString.getStr(data, JsonName.CAR_IMG_TWO));
                    XString.put(list.get(2), "photo", XString.getStr(data, JsonName.CAR_IMG_THREE));
                    XString.put(list.get(3), "photo", XString.getStr(data, JsonName.CAR_IMG_FOUR));
                    adapter.notifyDataSetChanged();
                } else {
                    mActivity.app.toast(XString.getStr(data, JsonName.MSG));
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                mActivity.app.toast("你的网络不给力");
            }
        });
    }

    private void setCarHead(final int index) {
        HttpUtils http = new HttpUtils();
        RequestParams params = RequestManager.simplePostParams();
        params.addBodyParameter(JsonName.CARID, UserHelper.getInstance().getCid());
        params.addBodyParameter("imgtype", String.valueOf(11 + index));
        http.send(HttpRequest.HttpMethod.POST, Net.SET_CAR_HEAD_IMG, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                JSONObject res = XString.getJSONObject(responseInfo.result);
                JSONObject data = XString.getJSONObject(res, JsonName.DATA);
                if (XString.getBoolean(res, JsonName.STATUS)) {
                    JSONObject o = list.get(index);
                    list.set(index, list.get(0));
                    list.set(0, o);
                    mActivity.app.toast("设置成功");
                    adapter.notifyDataSetChanged();
                } else {
                    mActivity.app.toast(XString.getStr(data, JsonName.MSG));
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                mActivity.app.toast("你的网络不给力");
            }
        });
    }
}
