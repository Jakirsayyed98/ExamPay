package app.exampay

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class WebActivity : AppCompatActivity() {
    var uploadMessage: ValueCallback<Array<Uri>?>? = null
    private var webView: WebView? = null
    private var progress: ProgressBar? = null
    private var dialog: SplashDialogFragment? = null
    private var isLoadingStarted = false
    private var isTimerFinished = false
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = resources.getColor(R.color.white, this.theme)
        } else
            window.statusBarColor = resources.getColor(R.color.white)
        dialog = SplashDialogFragment()
        dialog!!.show(supportFragmentManager, "SplashDialogFragment")
        webView = findViewById(R.id.webview)
        progress = findViewById(R.id.progress)
        progress!!.setVisibility(View.VISIBLE)

        webView!!.settings.userAgentString = "ExamPay"
        webView!!.getSettings().javaScriptEnabled = true
        webView!!.getSettings().setGeolocationEnabled(true)
        webView!!.getSettings().domStorageEnabled = true
        webView!!.setWebChromeClient(MyWebChromeClient(this))
        webView!!.setWebViewClient(object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
               // isLoadingStarted = true
                if (isTimerFinished && dialog!!.isVisible) {
                    dialog!!.dismiss()
                    progress!!.setVisibility(View.VISIBLE)
                    location()
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                progress!!.setVisibility(View.GONE)
                dialog!!.dismiss()
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                progress!!.setVisibility(View.VISIBLE)
                view.loadUrl(url)
                return true
            }
        })

        setDelay()
        webView!!.loadUrl("https://exampay.in/")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_FILE) {
            if (uploadMessage == null) return
            uploadMessage!!.onReceiveValue(FileChooserParams.parseResult(resultCode, data))
            uploadMessage = null
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView!!.canGoBack()) {
            webView!!.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun setDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isLoadingStarted) {
                dialog!!.dismiss()
                location()
            }
            isTimerFinished = true
        }, 1500)
    }

    private fun location() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {}
                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    companion object {
        const val REQUEST_SELECT_FILE = 100
    }
}