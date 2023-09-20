package net.j2i.webcrawler

import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class MainWebViewClient(val dataHandler:IContentExtractedHandler) : WebViewClient() {

    companion object {
        val TAG = "MainWebViewClient"

    }


    override fun onLoadResource(view: WebView?, url: String?) {
        if(url!=null) {
            dataHandler.ResourceRequested(url);
        }
        super.onLoadResource(view, url)
    }
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        view!!.evaluateJavascript("(function extractLinks(){var list = Array.from(document.getElementsByTagName('a')); for(var i=0;i<list.length;++i) { list[i] = list[i].href};; return list;})()",
            object:ValueCallback<String> {
                override fun onReceiveValue(value: String) {
                    if(value != null && value != "null")
                    {
                        val gson = GsonBuilder().create()
                        val theList = gson.fromJson<ArrayList<String>>(value, object :
                            TypeToken<ArrayList<String>>(){}.type)
                        if(theList != null) {
                            dataHandler.LinksExtracted(theList.toTypedArray());
                        }

                        Log.d(TAG,value);
                    }
                    dataHandler.PageLoadComplete()
                }

            }
            )
    }
    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        Log.e(TAG, errorResponse.toString())
    }
    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        Log.e(TAG, error.toString())
    }
}