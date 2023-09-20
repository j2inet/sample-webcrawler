package net.j2i.webcrawler

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import kotlinx.serialization.json.JsonObject
import net.j2i.webcrawler.data.UrlReading
import net.j2i.webcrawler.data.UrlReadingDataHelper
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.Random

class MainActivity : ComponentActivity() {

    var UrlList:ArrayList<String> = ArrayList<String>()
    val random = Random(0);
    lateinit var dataHelper:UrlReadingDataHelper
    val PICKER_EXPORT_READING = 100
    var linearLoadCount = 1;

    lateinit var mainWebView:WebView
    lateinit var mainWebViewClient:WebViewClient
    var mainHandler:Handler = Handler()
    var sessionTime = System.currentTimeMillis() / 1000
    var pageSessionID:Long = 1;
    val NAVIGATE_DELAY:Long = 8000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        mainWebView = findViewById(R.id.mainWebView)
        //mainHandler = Looper.getMainLooper() as Handler
        this.dataHelper = UrlReadingDataHelper(this)
        mainWebViewClient = MainWebViewClient(object:IContentExtractedHandler {
            override fun LinksExtracted(linkList: Array<String>) {
                UrlList.addAll(linkList)
            }

            override fun PageLoadComplete() {
                ++pageSessionID;
                mainHandler.postDelayed(object:Runnable {
                    override fun run() {
                        openRandomSite()
                    }
                },NAVIGATE_DELAY)
            }
            override fun ResourceRequested(url: String) {
                Log.d(MainWebViewClient.TAG, "Loading URL [${url}]");
                val reading = UrlReading(
                    sessionTime,
                    pageSessionID,
                    url,
                    System.currentTimeMillis() / 1000
                )
                dataHelper.insertReading(reading)
            }

        })
        mainWebView.webViewClient = mainWebViewClient

        if (Build.VERSION.SDK_INT >= 19) {
            mainWebView.getSettings().setJavaScriptEnabled(true);
            //mainWebView.getSettings().setBlockNetworkLoads (false);
            //mainWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            //mainWebView.
        }

        UrlList.add("https://msn.com")
        UrlList.add("https://yahoo.com");
        linearLoadCount = UrlList.count()
        openRandomSite()
    }

    fun exportButtonClicked(v: View) {
        exportData()
    }
    fun exportData() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "url_captures.csv")
        }
        this.startActivityForResult(intent, PICKER_EXPORT_READING)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            PICKER_EXPORT_READING -> {
                if (resultCode == RESULT_OK) {
                    if(data?.data == null) return
                    var uri: Uri = data?.data as Uri;

                    val os: OutputStream? = contentResolver.openOutputStream(uri)
                    if(os!=null) {
                        val pw = OutputStreamWriter(os)
                        try {
                            dataHelper.writeAllRecords(pw);
                        } finally {
                            pw.close()
                        }
                    }
                }
            }
        }
    }

    fun openRandomSite() {
        var index = 0;
        if(linearLoadCount>0) {
            --linearLoadCount
            var index = random.nextInt(UrlList.count())
        }
        val nextUrl = UrlList[index];
        UrlList.removeAt(index);
        mainWebView!!.loadUrl(nextUrl)
    }



}
