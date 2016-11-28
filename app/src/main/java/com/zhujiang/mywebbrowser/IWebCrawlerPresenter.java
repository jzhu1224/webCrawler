package com.zhujiang.mywebbrowser;

import android.webkit.WebView;

/**
 * Created by zhujiang on 2016/11/27.
 */

public interface IWebCrawlerPresenter {

    void attchView(IWebCrawlerView iWebCrawlerView, WebView webView);

    void deattchView();

    /**
     * 开始抓取网页内容
     */
    void crawPage();

    /**
     * 加载网页
     */
    void loadUrl(String url);

    /**
     * 解析器
     * @return
     */
    ITextExtract getTextExtract();

    /**
     * 保存图片
     */
    void saveImage();

    void attchSaveImageView(ISaveImageView iSaveImageView);

    void deattchSaveImageView();
}
