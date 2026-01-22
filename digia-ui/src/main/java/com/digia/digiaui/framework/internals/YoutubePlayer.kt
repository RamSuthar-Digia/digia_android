package com.digia.digiaui.framework.internals

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.net.URLDecoder

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InternalYoutubePlayer(
    videoUrl: String,
    isMuted: Boolean = false,
    loop: Boolean = false,
    autoPlay: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val videoId = remember(videoUrl) { extractVideoId(videoUrl) }

    val embedUrl = remember(videoId, isMuted, loop, autoPlay) {
        if (videoId.isBlank()) return@remember "about:blank"
        val autoplay = if (autoPlay) 1 else 0
        val mute = if (isMuted) 1 else 0
        val loopParam = if (loop) 1 else 0
        // playlist required for loop in embed
        "https://www.youtube.com/embed/$videoId?autoplay=$autoplay&mute=$mute&loop=$loopParam&playlist=$videoId&controls=0&fs=0&playsinline=1"
    }

    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
        }
    }

    LaunchedEffect(embedUrl) {
        webView.loadUrl(embedUrl)
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { webView },
        update = { it.loadUrl(embedUrl) }
    )
}

private fun extractVideoId(url: String): String {
    if (url.isBlank()) return ""
    if (!url.contains("http", ignoreCase = true) && !url.contains("www", ignoreCase = true)) {
        return url
    }

    return try {
        val decoded = URLDecoder.decode(url, "UTF-8")
        val uri = android.net.Uri.parse(decoded)
        uri.getQueryParameter("v") ?: (uri.lastPathSegment ?: "")
    } catch (_: Throwable) {
        ""
    }
}
