package com.zhujiang.mywebbrowser;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by zhujiang on 2016/11/27.
 */

public class PrivewFragment extends Fragment implements ISaveImageView {

    private Button btnSave;
    private ImageView imageView;

    IWebCrawlerPresenter iWebCrawlerPresenter;

    private ProgressDialog progressDialog;

    public static PrivewFragment getInstance() {
        return new PrivewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        Log.d("zhujiang","onCreateView");
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView = (ImageView) view.findViewById(R.id.img);

        btnSave = (Button) view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });

        Log.d("zhujiang","onViewCreated");

        iWebCrawlerPresenter = WebCrawlerPresenter.getInstance();
        iWebCrawlerPresenter.attchSaveImageView(this);


        showImage(((MainActivity)getActivity()).getCatchedBitmap());
    }

    private Bitmap bitmap;

    public void showImage(Bitmap bitmap) {
        this.bitmap = bitmap;
        Log.d("zhujiang","showImage");
        if (bitmap == null)
            return;
        imageView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,bitmap.getHeight()));
        imageView.setImageBitmap(bitmap);
    }

    private void saveImage() {
        iWebCrawlerPresenter.saveImage();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("zhujiang","onActivityCreated");

        progressDialog = new ProgressDialog(getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        iWebCrawlerPresenter.deattchSaveImageView();

        if (!bitmap.isRecycled())
            bitmap.recycle();
    }

    @Override
    public void showSavingImage() {
        progressDialog.setMessage("保存图片中");
        progressDialog.show();
    }

    @Override
    public void showSaveImageSuccess(String path) {
        progressDialog.dismiss();
        Toast.makeText(getActivity(),"图片保存成功:"+path,Toast.LENGTH_SHORT).show();
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void showSaveImageFail() {
        progressDialog.dismiss();
    }
}
