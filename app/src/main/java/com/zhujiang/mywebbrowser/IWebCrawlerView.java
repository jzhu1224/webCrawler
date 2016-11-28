package com.zhujiang.mywebbrowser;

import android.graphics.Bitmap;

/**
 * Created by zhujiang on 2016/11/27.
 */

public interface IWebCrawlerView {

    /**
     * 显示生成的图片
     * @param bitmap
     */
    void showCrawedImage(Bitmap bitmap);

    /**
     * 现实当前是否可以抓取网页
     * @param enabled
     */
    void showCrawlerEnabled(boolean enabled);

    void showCrawlingWeb();

    void showCrawSuccess();

    void showCrawFail();

}
