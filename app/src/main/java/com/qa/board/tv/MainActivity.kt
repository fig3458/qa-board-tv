package com.qa.board.tv

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.qa.board.tv.databinding.ActivityMainBinding
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainHandler = Handler(Looper.getMainLooper())
    private var menuVisible = false
    private var pageReady = false
    private var setupVisible = false

    private val hideSystemUiRunnable = Runnable { enterImmersiveMode() }

    /** Board pages are authored for full-HD desktop. */
    private val designWidth = 1920
    private val designHeight = 1080

    private val desktopUserAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enterImmersiveMode()
        setupWebView()
        setupRemoteMenu()
        setupUrlPanel()

        binding.root.post {
            applyTvScale()
            if (BoardPrefs.hasUrl(this)) {
                loadBoard(hard = false)
            } else {
                showUrlSetup(prefill = "")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enterImmersiveMode()
        binding.webView.onResume()
    }

    override fun onPause() {
        binding.webView.onPause()
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enterImmersiveMode()
            applyTvScale()
            fitBoardToScreen(binding.webView)
        }
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        binding.webView.apply {
            loadUrl("about:blank")
            stopLoading()
            clearHistory()
            removeAllViews()
            destroy()
        }
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_INFO,
            KeyEvent.KEYCODE_TV,
            KeyEvent.KEYCODE_GUIDE -> {
                if (setupVisible) return true
                toggleRemoteMenu()
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                if (menuVisible) {
                    hideRemoteMenu()
                    return true
                }
                if (setupVisible && BoardPrefs.hasUrl(this)) {
                    hideUrlSetup()
                    return true
                }
            }
            KeyEvent.KEYCODE_PROG_RED,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                if (!setupVisible) {
                    refreshBoard(hard = false)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webView = binding.webView
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            setSupportZoom(true)
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            mediaPlaybackRequiresUserGesture = false
            userAgentString = desktopUserAgent
            textZoom = 100
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            allowFileAccess = false
            allowContentAccess = true
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
        }

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean = false

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                pageReady = false
                showLoading(getString(R.string.loading))
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                applyTvScale()
                fitBoardToScreen(view)
                mainHandler.postDelayed({ fitBoardToScreen(view) }, 500)
                mainHandler.postDelayed({ fitBoardToScreen(view) }, 1500)
                mainHandler.postDelayed({ fitBoardToScreen(view) }, 3000)
                pageReady = true
                hideLoading()
                enterImmersiveMode()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    showLoading(getString(R.string.load_failed))
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.load_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun applyTvScale() {
        val webView = binding.webView
        val w = webView.width.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels
        val h = webView.height.takeIf { it > 0 } ?: resources.displayMetrics.heightPixels
        val scalePct = (min(w.toFloat() / designWidth, h.toFloat() / designHeight) * 100f)
            .toInt()
            .coerceIn(25, 400)
        Log.i(TAG, "TV scale=${scalePct}% view=${w}x${h}")
        webView.setInitialScale(scalePct)
    }

    private fun fitBoardToScreen(webView: WebView?) {
        if (webView == null) return

        val js = """
            (function() {
              try {
                var DESIGN_W = $designWidth;
                var DESIGN_H = $designHeight;

                var meta = document.querySelector('meta[name="viewport"]');
                if (!meta) {
                  meta = document.createElement('meta');
                  meta.name = 'viewport';
                  (document.head || document.documentElement).appendChild(meta);
                }
                meta.setAttribute(
                  'content',
                  'width=' + DESIGN_W + ', initial-scale=1.0, maximum-scale=5.0, user-scalable=yes'
                );

                var html = document.documentElement;
                var body = document.body;
                html.style.margin = '0';
                html.style.padding = '0';
                html.style.width = DESIGN_W + 'px';
                html.style.height = DESIGN_H + 'px';
                html.style.overflow = 'hidden';
                if (body) {
                  body.style.margin = '0';
                  body.style.padding = '0';
                  body.style.width = DESIGN_W + 'px';
                  body.style.height = DESIGN_H + 'px';
                  body.style.overflow = 'hidden';
                  body.style.transform = '';
                  body.style.zoom = '';
                }

                ['#app', '#root', '#__nuxt', '.app', 'main'].forEach(function(sel) {
                  var el = document.querySelector(sel);
                  if (!el) return;
                  el.style.width = DESIGN_W + 'px';
                  el.style.height = DESIGN_H + 'px';
                  el.style.maxWidth = 'none';
                  el.style.minWidth = DESIGN_W + 'px';
                  el.style.minHeight = DESIGN_H + 'px';
                  el.style.boxSizing = 'border-box';
                });
              } catch (e) {}
            })();
        """.trimIndent()

        webView.evaluateJavascript(js, null)
    }

    private fun setupRemoteMenu() {
        binding.btnRefresh.setOnClickListener {
            hideRemoteMenu()
            refreshBoard(hard = false)
        }
        binding.btnHardReload.setOnClickListener {
            hideRemoteMenu()
            refreshBoard(hard = true)
        }
        binding.btnSetUrl.setOnClickListener {
            hideRemoteMenu()
            showUrlSetup(prefill = BoardPrefs.getUrl(this))
        }
    }

    private fun setupUrlPanel() {
        binding.btnSaveUrl.setOnClickListener { saveUrlFromInput() }
        binding.urlInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveUrlFromInput()
                true
            } else {
                false
            }
        }
    }

    private fun saveUrlFromInput() {
        val normalized = BoardPrefs.normalize(binding.urlInput.text?.toString().orEmpty())
        if (normalized.isEmpty()) {
            Toast.makeText(this, R.string.setup_empty, Toast.LENGTH_SHORT).show()
            return
        }
        BoardPrefs.setUrl(this, normalized)
        Toast.makeText(this, R.string.url_saved, Toast.LENGTH_SHORT).show()
        hideUrlSetup()
        loadBoard(hard = true)
    }

    private fun showUrlSetup(prefill: String) {
        setupVisible = true
        hideRemoteMenu()
        binding.loadingOverlay.visibility = View.GONE
        binding.urlInput.setText(prefill)
        binding.urlSetupOverlay.visibility = View.VISIBLE
        binding.urlInput.requestFocus()
    }

    private fun hideUrlSetup() {
        setupVisible = false
        binding.urlSetupOverlay.visibility = View.GONE
        enterImmersiveMode()
    }

    private fun loadBoard(hard: Boolean) {
        val url = BoardPrefs.getUrl(this)
        if (url.isEmpty()) {
            showUrlSetup(prefill = "")
            return
        }
        showLoading(getString(R.string.loading))
        if (hard) {
            binding.webView.clearCache(true)
        }
        applyTvScale()
        binding.webView.loadUrl(url)
    }

    private fun refreshBoard(hard: Boolean) {
        if (!BoardPrefs.hasUrl(this)) {
            showUrlSetup(prefill = "")
            return
        }
        if (hard || !pageReady) {
            loadBoard(hard = true)
        } else {
            showLoading(getString(R.string.loading))
            applyTvScale()
            binding.webView.reload()
        }
    }

    private fun toggleRemoteMenu() {
        if (menuVisible) hideRemoteMenu() else showRemoteMenu()
    }

    private fun showRemoteMenu() {
        menuVisible = true
        binding.remoteMenu.visibility = View.VISIBLE
        binding.btnRefresh.requestFocus()
    }

    private fun hideRemoteMenu() {
        menuVisible = false
        binding.remoteMenu.visibility = View.GONE
        binding.webView.requestFocus()
        enterImmersiveMode()
    }

    private fun showLoading(message: String) {
        binding.statusText.text = message
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingOverlay.visibility = View.GONE
    }

    private fun enterImmersiveMode() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        mainHandler.removeCallbacks(hideSystemUiRunnable)
        mainHandler.postDelayed(hideSystemUiRunnable, 1500)
    }

    companion object {
        private const val TAG = "BoardShell"
    }
}
