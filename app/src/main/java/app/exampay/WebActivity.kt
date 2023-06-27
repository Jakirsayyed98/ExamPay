package app.exampay

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
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


class WebActivity : AppCompatActivity() {
    var backPressedTime: Long = 0
    private var webView: WebView? = null
    private var progress: ProgressBar? = null
    private var dialog: SplashDialogFragment? = null
    private var isLoadingStarted = false
    private var isTimerFinished = false
    val RC_SIGN_IN = 10121
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

        //
/*
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        val googleApiClient: GoogleApiClient =GoogleApiClient.Builder(this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
*/
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)


        //

        webView!!.settings.userAgentString = "ExamPay"
        webView!!.getSettings().javaScriptEnabled = true
        webView!!.getSettings().setGeolocationEnabled(true)
        webView!!.getSettings().domStorageEnabled = true
        webView!!.setWebChromeClient(MyWebChromeClient(this))
//        webView!!.addJavascriptInterface(JavaScriptInterface(), "AndroidInterface")

        webView!!.setWebViewClient(object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
               // isLoadingStarted = true
                if (isTimerFinished && dialog!!.isVisible) {
                    dialog!!.dismiss()
                    progress!!.setVisibility(View.VISIBLE)
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                progress!!.setVisibility(View.GONE)

                dialog!!.dismiss()
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                progress!!.setVisibility(View.VISIBLE)
//                if (url.startsWith("https://accounts.google.com")) {
//                    val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
//                    startActivityForResult(signInIntent, RC_SIGN_IN)
//                    return true
//                }
//                return super.shouldOverrideUrlLoading(view, url);
                view.loadUrl(url)
                return true
            }
        })

        setDelay()
        webView!!.loadUrl("https://exampay.in/")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == RC_SIGN_IN) {
//            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
//            handleSignInResult(result)
//        }
    }

//    class JavaScriptInterface {
//        @JavascriptInterface
//        fun onSignIn(email: String?, profilePictureUrl: String?) {
//            // Perform actions with the sign-in information, such as passing it to the website
//            // or interacting with other parts of your Android app
//        }
//    }
//    private fun handleSignInResult(result: GoogleSignInResult?) {
//        if (result!!.isSuccess) {
//            val account = result.signInAccount
//            val userEmail = account!!.email
//            val userProfilePictureUrl = account.photoUrl.toString()
//            val javascriptCode =
//                "javascript:window.AndroidInterface.onSignIn('$userEmail', '$userProfilePictureUrl');"
//            webView!!.loadUrl(javascriptCode)
//        } else {
//            // Sign-in failed
//            val status: Status = result.status
//            val statusCode: Int = status.getStatusCode()
//            Log.e("Google Sign-In", "Sign-in failed with error code: $statusCode")
//        }
//    }


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
            }
            isTimerFinished = true
        }, 1500)
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