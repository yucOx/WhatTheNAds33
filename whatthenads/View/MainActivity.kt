package com.yucox.whatthenads.View

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsetsController
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.monstertechno.adblocker.AdBlockerWebView
import com.yucox.whatthenads.ViewModel.SeriesViewModel
import com.yucox.whatthenads.Model.SeriesInfo
import com.yucox.whatthenads.R
import com.yucox.whatthenads.Util.SynchroneScreen.FULLSCREEN
import com.yucox.whatthenads.Util.SynchroneScreen.LANDSCAPE
import com.yucox.whatthenads.Util.SynchroneScreen.NAVBAR
import com.yucox.whatthenads.Util.SynchroneScreen.PORTRAIT
import com.yucox.whatthenads.Util.Websites.DIZIBOX
import com.yucox.whatthenads.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var cookieManager: CookieManager
    private lateinit var viewModel: SeriesViewModel
    private lateinit var mainScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val uiController = window.insetsController

        mainScope = CoroutineScope(Dispatchers.Main)
        viewModel = ViewModelProvider(this).get(SeriesViewModel::class.java)


        viewModel.executeFilterList(this)

        cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(binding.webView, true)

        sharedPreferences = getSharedPreferences("SavedActivity", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        viewModel.message.observe(this) {
            if (!it.isNullOrEmpty()) {
                Toast.makeText(
                    this, it, Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.bookMarkBtn.setOnClickListener {
            viewModel.checkSaveStatus()
        }


        viewModel.adList.observe(this) {
            if (!it.isNullOrEmpty()) {
                setChromeClient(uiController)
                val superWebViewClient = initWebClient()
                binding.webView.webViewClient = superWebViewClient

                val selectedSeries = intent.getStringExtra("url")
                if (selectedSeries == null) {
                    binding.webView.loadUrl(DIZIBOX)
                } else {
                    binding.webView.loadUrl(selectedSeries)
                }
            }
        }


        binding.webView.settings.javaScriptEnabled = true

        binding.searchSeriesBtn.setOnClickListener {
            val enterAnim = AnimationUtils.loadAnimation(
                this,
                com.google.android.material.R.anim.m3_side_sheet_enter_from_right
            )
            val exitAnim = AnimationUtils.loadAnimation(
                this,
                com.google.android.material.R.anim.m3_side_sheet_exit_to_right
            )

            if (binding.searchAreaFrame.visibility == View.VISIBLE) {
                binding.searchAreaFrame.startAnimation(exitAnim)
                binding.searchAreaFrame.visibility = View.GONE
            } else {
                binding.searchAreaFrame.startAnimation(enterAnim)
                binding.searchAreaFrame.visibility = View.VISIBLE
            }
        }

        binding.secondSearchBtn.setOnClickListener {
            val theThing = binding.searchSeriesEt.text?.toString()
            if (!theThing.isNullOrEmpty()) {
                val searchedOne = DIZIBOX + "?s=${theThing}"
                println(searchedOne)
                binding.webView.loadUrl(searchedOne)
                initWebClient()

            }
        }

        binding.rotateScreenBtn.setOnClickListener {
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                requestedOrientation = LANDSCAPE
            } else {
                requestedOrientation = PORTRAIT
            }
        }

    }

    private fun initWebClient(): WebViewClient {
        val superClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {

                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                hideAccountButtons(view)
                //hidePlayableAds(view)

                view?.evaluateJavascript("UdvbAds = function() {};") { result ->
                }


                view?.evaluateJavascript(
                    """
    var videoElement = document.querySelector("video");
    if (videoElement) {
        videoElement.style.display = "none";
    }
    """
                ) { result ->
                }
                mainScope.launch {
                    val episode = getEpisode(view)
                    val seriesName = getSeriesName(view)

                    val newSeries = SeriesInfo(
                        seriesName,
                        episode,
                        binding.webView.url,
                    )
                    viewModel.updateSeries(newSeries)
                }
            }


            override fun shouldInterceptRequest(
                view: WebView?, request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url.toString()

                viewModel.adList.value?.forEach { adHost ->
                    if (url.contains(adHost)) {
                        mainScope.launch {
                            closePreVideoAndShowFrames(view)
                            hideBannerPart(view)
                        }
                        println(url)
                        return WebResourceResponse("text/plain", "utf-8", null)
                    }
                }

                return super.shouldInterceptRequest(view, request)
            }


        }
        return superClient
    }


    fun closePreVideoAndShowFrames(view: WebView?) {
        view?.evaluateJavascript(
            """
    clearInterval(skipTimer3);
    localStorage.setItem("_prevideoMuted", 0);
    """.trimIndent(), null
        )
        view?.evaluateJavascript(
            """
    var elements = document.getElementsByClassName("prevideo");
    while (elements.length > 0) {
        elements[0].parentNode.removeChild(elements[0]);
    }
    """.trimIndent(), null
        )

        view?.evaluateJavascript(
            """
        document.getElementById("vido-before-videox").style.display = "none";
        document.getElementById("vido-before-video-content").style.display = "block";
          """.trimIndent(), null
        )
    }


    private fun hideBannerPart(view: WebView?) {
        view?.evaluateJavascript(
            "(function() { " + "var element = document.getElementById('epom-6b036410');" + "if (element) {" + "   element.style.display = 'none';" + "}" + "})()"
        ) {}
        view?.evaluateJavascript(
            "(function() { " + "var element = document.querySelector('#epom-542414c8');" + "if (element) {" + "   element.style.display = 'none';" + "}" + "})()"
        ) {}
        view?.evaluateJavascript(
            "(function() { " + "var element = document.querySelector('.adv.box-header-bottom.full-width.pull-left');" + "if (element) {" + "   element.style.display = 'none';" + "}" + "})()"
        ) {}
        view?.evaluateJavascript(
            "(function() { " + "var element = document.querySelector('.m-t-1.full-width.pull-left');" + "if (element) {" + "   element.style.display = 'none';" + "}" + "})()"
        ) {}

    }

    private fun hidePlayableAds(view: WebView?) {
        view?.evaluateJavascript(
            """
            var adIframes = document.querySelectorAll('iframe[src^="https://sobreatsesuyp.com"]');
            if (adIframes) {
                adIframes.forEach(function(iframe) {
                    iframe.style.display = 'none';
                });
            }
            
            setTimeout(function() {
                var adIframes = document.querySelectorAll('iframe[src^="https://sobreatsesuyp.com"]');
                if (adIframes) {
                    adIframes.forEach(function(iframe) {
                        iframe.style.display = 'none';
                    });
                }
            }, 3000);
            """, null
        )
    }

    private fun hideAccountButtons(view: WebView?) {
        view?.evaluateJavascript(
            """
        (function() {
            var loginButtons = document.getElementById("login-buttons");
            if (loginButtons) {
                loginButtons.style.display = "none";
            }
        })();
        """.trimIndent(), null
        )
    }

    private suspend fun getSeriesName(view: WebView?): String {
        return suspendCoroutine { ready ->
            view?.evaluateJavascript(
                "(function() { return document.querySelector('span[itemprop=\"name\"]').innerText; })();"
            ) { result ->
                val name = result?.removeSurrounding("\"").toString()
                ready.resume(name)
            }
        }
    }

    private suspend fun getEpisode(view: WebView?): String {
        return suspendCoroutine { ready ->
            view?.evaluateJavascript(
                """
            (function() { return document.querySelector('.tv-title-episode').innerText; })();
            
        """
            ) { it ->
                ready.resume(it)
            }
        }
    }

    private fun setChromeClient(uiController: WindowInsetsController?) {
        val webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(view, callback)
                binding.searchAreaFrame.visibility = View.GONE
                binding.webView.visibility = View.GONE
                binding.customView.visibility = View.VISIBLE
                binding.customView.addView(view)
                binding.bookMarkBtn.visibility = View.GONE
                binding.searchSeriesBtn.visibility = View.GONE
                binding.rotateScreenBtn.visibility = View.GONE
                uiController?.hide(FULLSCREEN + NAVBAR)
            }

            override fun onHideCustomView() {
                super.onHideCustomView()
                binding.webView.visibility = View.VISIBLE
                binding.customView.visibility = View.GONE
                binding.rotateScreenBtn.visibility = View.VISIBLE
                binding.searchSeriesBtn.visibility = View.VISIBLE
                binding.bookMarkBtn.visibility = View.VISIBLE
            }
        }
        binding.webView.webChromeClient = webChromeClient
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            editor.putString(
                "cookies", cookieManager.getCookie(binding.webView.url)
            ).commit()
            val intent = Intent(
                this, SelectActivity::class.java
            )
            startActivity(intent)
            finish()
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        editor.putString(
            "cookies", cookieManager.getCookie(binding.webView.url)
        ).commit()
    }
}