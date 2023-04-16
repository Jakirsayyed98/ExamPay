package app.exampay

import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Build
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.annotation.RequiresApi

class MyWebChromeClient(  // reference to activity instance. May be unnecessary if your web chrome client is member class.
    private val myActivity: WebActivity
) : WebChromeClient() {
    override fun onGeolocationPermissionsShowPrompt(
        origin: String,
        callback: GeolocationPermissions.Callback
    ) {
        callback.invoke(origin, true, false)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        // make sure there is no existing message
        if (myActivity.uploadMessage != null) {
            myActivity.uploadMessage!!.onReceiveValue(null)
            myActivity.uploadMessage = null
        }
    //    myActivity.uploadMessage = filePathCallback
        val intent = fileChooserParams.createIntent()
        try {
            myActivity.startActivityForResult(intent, 100)
        } catch (e: ActivityNotFoundException) {
            myActivity.uploadMessage = null
            Toast.makeText(myActivity, "Cannot open file chooser", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
}