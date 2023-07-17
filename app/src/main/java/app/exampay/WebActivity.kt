package app.exampay

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import java.net.URISyntaxException


class WebActivity : AppCompatActivity() {
    var backPressedTime: Long = 0
    private var webView: WebView? = null
    private var progress: ProgressBar? = null
    private var dialog: SplashDialogFragment? = null
    private var isLoadingStarted = false
    private var isTimerFinished = false
    val RC_SIGN_IN = 10121

    val BHIM_UPI = "in.org.npci.upiapp"
    val GOOGLE_PAY = "com.google.android.apps.nbu.paisa.user"
    val PHONE_PE = "com.phonepe.app"
    val PAYTM = "net.one97.paytm"
    val upiApps = listOf<String>(PAYTM, GOOGLE_PAY, PHONE_PE, BHIM_UPI)

    // we will use these when user responds
    @JvmField
    var mGeolocationOrigin: String? = null

    @JvmField
    var mGeolocationCallback: GeolocationPermissions.Callback? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = resources.getColor(R.color.white, this.theme)
        } else{
            window.statusBarColor = resources.getColor(R.color.white)
        }

        dialog = SplashDialogFragment()
        dialog!!.show(supportFragmentManager, "SplashDialogFragment")
        webView = findViewById(R.id.webview)
        progress = findViewById(R.id.progress)
        progress!!.setVisibility(View.VISIBLE)
        setDelay()
        val userAgent = "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.61 Mobile Safari/537.36"
        webView!!.settings.userAgentString = userAgent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }


        webView!!.settings.allowContentAccess = true
        webView!!.settings.allowFileAccess = true
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.domStorageEnabled = true
        webView!!.settings.databaseEnabled = true
//        webView!!.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK)
        webView!!.settings.javaScriptCanOpenWindowsAutomatically = true
        webView!!.settings.allowFileAccess = true
        webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webView!!.webChromeClient = GeoWebChromeClient(this)

        webView!!.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progress!!.visibility= View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                    progress!!.visibility = View.GONE

            }


            override fun onPageCommitVisible(view: WebView?, url1: String?) {
                super.onPageCommitVisible(view, url1)

            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                if (errorCode == -2) {

                }
            }


            override fun shouldOverrideUrlLoading(view: WebView, url123: String?): Boolean {
                url123?.let {
                    Log.d("@@@@",url123.toString())
                    if (url123.startsWith("intent://m.youtube.com/")) {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url123))
                        try {
                            this@WebActivity.startActivity(webIntent)
                        } catch (ex: ActivityNotFoundException) {
                        }
                        return true
                    } else  if (url123.startsWith("https://api.whatsapp.com")) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url123))
                        startActivity(intent)
                        return true
                    } else if (url123.startsWith("https://www.facebook.com")) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url123));
                        startActivity(intent);
                        return true;
                    } else if (url123.startsWith("https://www.linkedin.com")) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url123))
                        startActivity(intent)
                        return true
                    } else if (url123.startsWith("truecallersdk:")) {
                        try {

                            val uri = Uri.parse(url123)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)

                            return true
                        } catch (e: URISyntaxException) {
                            e.printStackTrace()
                        }
                    } else if (url123.startsWith("tel:")) {
                        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url123)))
                        return true
                    } else if (url123.startsWith("http") || url123.startsWith("https")) {
                        return false
                    } else if (url123.startsWith("intent")) {
                        try {
                            val intent = Intent.parseUri(url123, Intent.URI_INTENT_SCHEME)
                            val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                            if (fallbackUrl != null) {
                                view.loadUrl(fallbackUrl);
                                return true
                            }
                        } catch (e: URISyntaxException) {
                            e.printStackTrace()
                        }
                    } else if (Uri.parse(url123).getScheme().equals("market")) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url123));
                            startActivity(intent);
                            return true;
                        } catch (e: Exception) {
                            // Google Play app is not installed, you may want to open the app store link
                            val uri: Uri = Uri.parse(url123)
                            view.loadUrl("http://play.google.com/store/apps/" + uri.getHost() + "?" + uri.getQuery());
                            return false;
                        }

                    }else{
                        try {


                            val uri = Uri.parse(url123)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                startActivity(intent)
                            }catch (e:Exception){
                                e.printStackTrace()
                            }

                            return true


                        } catch (e: URISyntaxException) {
                            e.printStackTrace()
                        }
                    }
                }
                return super.shouldOverrideUrlLoading(view, url123)
            }
        }

            webView!!.loadUrl("https://exampay.in/")

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
//            if (isLoadingStarted) {
                dialog!!.dismiss()
//            }
            isTimerFinished = true
        }, 2500)
    }



    override fun onBackPressed() {

        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.getOnBackPressedDispatcher().onBackPressed()
            finish()
        } else {
            Toast.makeText(this, "Press back again to leave the app.", Toast.LENGTH_LONG).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}
