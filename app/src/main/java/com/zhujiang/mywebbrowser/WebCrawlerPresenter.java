package com.zhujiang.mywebbrowser;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhujiang on 2016/11/27.
 */

public class WebCrawlerPresenter implements IWebCrawlerPresenter {

    private IWebCrawlerView webCrawlerView;
    private WebView webView;

    private ISaveImageView iSaveImageView;

    private Handler handler = new Handler();

    private String title;
    private String content = "默认摘要";

    private ITextExtract textExtract;

    private float screenWidth;

    private static WebCrawlerPresenter webCrawlerPresenter;

    private Bitmap crawlerBitmap;//最终生成的图片


    private WebCrawlerPresenter() {

    }

    public static WebCrawlerPresenter getInstance() {
        if (webCrawlerPresenter == null) {
            webCrawlerPresenter = new WebCrawlerPresenter();
        }
        return webCrawlerPresenter;
    }

    @Override
    public void attchView(IWebCrawlerView iWebCrawlerView, WebView webView) {
        this.webCrawlerView = iWebCrawlerView;
        this.webView = webView;

        webCrawlerView.showCrawlerEnabled(false);

        textExtract = getTextExtract();

        screenWidth = new Utils().getScreenWidth();

        initWebView();
    }

    private void initWebView() {

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        //设置缓存模式
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // 开启DOM storage API 功能
        settings.setDomStorageEnabled(true);
        // 开启database storage API功能
        settings.setDatabaseEnabled(true);
        settings.setAppCacheEnabled(true);

        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HtmlViewer");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                webCrawlerView.showCrawlerEnabled(false);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onLoadResource(WebView view, String url) {

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webCrawlerView.showCrawlerEnabled(true);
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                WebCrawlerPresenter.this.title = title;
            }
        });

    }

    @Override
    public void deattchView() {
        webCrawlerView = null;
        webView.destroy();
        webView = null;

        if (crawlerBitmap != null && !crawlerBitmap.isRecycled()) {
            crawlerBitmap.recycle();
        }
    }

    @Override
    public void crawPage() {
        webView.loadUrl("javascript:window.HtmlViewer.showHTML" +
                "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
    }

    @Override
    public void loadUrl(String url) {
         url = addPrex(url);
        if (isMatch(url)) {
            webView.loadUrl(url);
        } else {

        }
    }

    @Override
    public ITextExtract getTextExtract() {
        return new TextExtract();
    }

    @Override
    public void saveImage() {
        new SaveImageTask().execute();
    }

    @Override
    public void attchSaveImageView(ISaveImageView iSaveImageView) {
        this.iSaveImageView = iSaveImageView;
    }

    @Override
    public void deattchSaveImageView() {
        iSaveImageView = null;
    }

    class SaveImageTask extends AsyncTask<Void,Void,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            iSaveImageView.showSavingImage();
        }

        @Override
        protected String doInBackground(Void... params) {
            return saveImage(crawlerBitmap);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            iSaveImageView.showSaveImageSuccess(s);
        }
    }

    /**
     * 保存图片到SD卡
     * @param finalBitmap
     */
    private String saveImage(Bitmap finalBitmap) {

        String root = MyApplication.getInstance().getExternalFilesDir("saved_images").getAbsolutePath();
        String fname = "Image"+ System.currentTimeMillis() +".jpg";
        File file = new File (root, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            iSaveImageView.showSaveImageFail();
        }

        return fname;
    }

    private String addPrex(String url) {
        if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
            url = "http://"+url;
        }
        return url;
    }

    private boolean isMatch(String url) {
        String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern patt = Pattern. compile(regex );
        Matcher matcher = patt.matcher(url);
        return matcher.matches();
    }


    private class MyJavaScriptInterface {

        @JavascriptInterface
        public void showHTML(final String html) {

            handler.post(new Runnable() {
                @Override
                public void run() {
                    webCrawlerView.showCrawlingWeb();
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String content = textExtract.parse(html);
                        Log.d("zhujiang","content:"+content);
                        WebCrawlerPresenter.this.content = content;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Document doc = Jsoup.parse(html);
                                Elements elements = doc.getElementsByTag("img");
                                findSuitableImage(elements,0);
                            }
                        });


                }
            }).start();
        }
    }


    /**
     * 递归查找符合条件的图片
     * @param elements
     * @param index
     */
    private void findSuitableImage(final Elements elements, final int index) {

        if (index >= elements.size()) {
            //// TODO: 2016/11/27 没有找到合适图片的情况处理
            webCrawlerView.showCrawFail();
            return;
        }

        String src = elements.get(index).attr("src");

        if (src.startsWith("//")) { /**img中的url是以双斜杠“//”开头的，
         这种写法有特殊的用途，它会判断当前的页面协议是http 还是 https 来决定请求 url 的协议。
         防止IE下出现“This Page Contains Both Secure and Non-Secure Items”的报错。**/
            src = "http:"+src;
        }

        Picasso.with(MyApplication.getInstance()).load(src).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                //约定大于100*100为合适图片
                Log.d("zhujiang","width:"+width+" height:"+height);


                int idx = index +1;

                if (width * height < 100*100) {
                    findSuitableImage(elements,idx);
                } else {
                    try {
                        if (bitmap.isRecycled()) {
                            webCrawlerView.showCrawFail();
                            return;
                        }
                        new DrawTextToBitmapTask().execute(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                        webCrawlerView.showCrawFail();
                    }
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.e("zhujiang","onBitmapFailed");
                webCrawlerView.showCrawFail();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Log.e("zhujiang","onPrepareLoad");
            }
        });
    }

    private Bitmap drawTextToBitmap(Bitmap bitmap,
                                   String title, String content) {

        android.graphics.Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }

        bitmap = bitmap.copy(bitmapConfig, true);

        Bitmap bitmapTitle = textAsBitmap(bitmap.getWidth(),title,20, Color.rgb(61, 61, 61));

        Bitmap bitmapContent = textAsBitmap(bitmap.getWidth(),content,20,Color.rgb(61, 61, 61));

        Bitmap bitmap1 = Bitmap.createBitmap(bitmap.getWidth()
                , bitmap.getHeight()+bitmapTitle.getHeight()+bitmapContent.getHeight()
                , Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap1);

        //背景颜色

        canvas.drawColor(0xFFFFFFFF);

        canvas.drawBitmap(bitmapTitle,0,0,null);

        canvas.drawBitmap(bitmap,0,bitmapTitle.getHeight(),null);

        canvas.drawBitmap(bitmapContent,0,bitmapTitle.getHeight()+bitmap.getHeight(),null);

        bitmap.recycle();
        bitmapTitle.recycle();
        bitmapContent.recycle();

        return bitmap1;
    }

    private Bitmap textAsBitmap(int lineWidth,String text, float textSize, int textColor) {
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        //Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.LEFT);

        StaticLayout myStaticLayout = new StaticLayout(text, textPaint, lineWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        Bitmap image = Bitmap.createBitmap(lineWidth, myStaticLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        myStaticLayout.draw(canvas);
        return image;
    }

    /**
     * 合成图片
     */
    private class DrawTextToBitmapTask extends AsyncTask<Bitmap,Void,Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            title = webView.getTitle();
        }

        @Override
        protected Bitmap doInBackground(Bitmap... params) {

            Bitmap bitmap = params[0];

            int width = bitmap.getWidth();

            float scale;

            scale = screenWidth / (float)width;

            int newWidth = (int)screenWidth;
            int newHeigth = (int)(scale * bitmap.getHeight());

            return drawTextToBitmap(getResizedBitmap(bitmap,newWidth,newHeigth),title,content);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            webCrawlerView.showCrawSuccess();
            crawlerBitmap = bitmap;
            webCrawlerView.showCrawedImage(bitmap);
        }
    }

    /**
     * 缩放图片
     * @param bm
     * @param newWidth
     * @param newHeight
     * @return
     */
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);

        if (!bm.isRecycled()) {
            bm.recycle();
        }

        return resizedBitmap;
    }

}
