package com.zhujiang.mywebbrowser;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements IWebCrawlerView{

    private EditText editUrl;
    private WebView webView;
    private Button btnCatch,btnGo;
    private ImageView img;


    private IWebCrawlerPresenter webCrawlerPresenter;

    private PrivewFragment fragment;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(MainActivity.this);

        initViews();
        webCrawlerPresenter = WebCrawlerPresenter.getInstance();
        webCrawlerPresenter.attchView(this,webView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webCrawlerPresenter.deattchView();
    }

    private void initViews() {

        editUrl = (EditText) findViewById(R.id.edt_url);
        webView = (WebView) findViewById(R.id.webview);
        btnCatch = (Button) findViewById(R.id.btn_catch);
        img = (ImageView) findViewById(R.id.img);
        btnGo = (Button) findViewById(R.id.btn_go);


        btnCatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webCrawlerPresenter.crawPage();
            }
        });

        editUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_GO){
                    InputMethodManager imm = (InputMethodManager) v
                            .getContext().getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(
                                v.getApplicationWindowToken(), 0);
                    }
                    webCrawlerPresenter.loadUrl(editUrl.getText().toString());
                    return true;
                }
                return false;
            }
        });

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webCrawlerPresenter.loadUrl(editUrl.getText().toString());
            }
        });
    }

    Bitmap catchedBitmap;

    public Bitmap getCatchedBitmap() {
        return catchedBitmap;
    }

    @Override
    public void showCrawedImage(Bitmap bitmap) {

        catchedBitmap = bitmap;

        if (getSupportFragmentManager().findFragmentByTag("PrivewFragment") == null) {
            fragment = PrivewFragment.getInstance();
            getSupportFragmentManager().beginTransaction().add(android.R.id.content,fragment,"PrivewFragment").commit();
        }

        //fragment.showImage(bitmap);
    }

    @Override
    public void showCrawlerEnabled(boolean enabled) {
        btnCatch.setEnabled(enabled);
    }

    @Override
    public void showCrawlingWeb() {
        progressDialog.setMessage("抓取内容中");
        progressDialog.show();
    }

    @Override
    public void showCrawSuccess() {
        Toast.makeText(MainActivity.this,"抓取成功",Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }

    @Override
    public void showCrawFail() {
        progressDialog.dismiss();
        Toast.makeText(MainActivity.this,"抓取失败",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {

        if (!fragment.isRemoving()) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            return;
        }

        if (webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }
}
