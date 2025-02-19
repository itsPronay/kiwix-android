/*
 * Kiwix Android
 * Copyright (c) 2019 Kiwix <android.kiwix.org>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.kiwix.kiwixmobile.core.main

import android.content.Intent
import android.webkit.MimeTypeMap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import org.kiwix.kiwixmobile.core.CoreApp.Companion.instance
import org.kiwix.kiwixmobile.core.reader.ZimFileReader
import org.kiwix.kiwixmobile.core.reader.ZimReaderContainer
import org.kiwix.kiwixmobile.core.utils.TAG_KIWIX
import org.kiwix.kiwixmobile.core.utils.files.Log

open class CoreWebViewClient(
  protected val callback: WebViewCallback,
  protected val zimReaderContainer: ZimReaderContainer
) : WebViewClient() {
  private var urlWithAnchor: String? = null

  @Suppress("ReturnCount")
  override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
    var url = request.url.toString()
    callback.webViewUrlLoading()
    url = convertLegacyUrl(url)
    urlWithAnchor = if (url.contains("#")) url else null
    if (zimReaderContainer.isRedirect(url)) {
      if (handleUnsupportedFiles(url)) {
        return true
      }
      view.loadUrl(zimReaderContainer.getRedirect(url))
      return true
    }
    if (url.startsWith(ZimFileReader.CONTENT_PREFIX)) {
      return handleUnsupportedFiles(url)
    }
    if (url.startsWith("javascript:")) {
      // Allow javascript for HTML functions and code execution (EX: night mode)
      return true
    }
    if (url.startsWith(ZimFileReader.UI_URI.toString())) {
      Log.e("KiwixWebViewClient", "UI Url $url not supported.")
      // Document this code - what's a UI_URL?
      return true
    }

    // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    callback.openExternalUrl(intent)
    return true
  }

  private fun convertLegacyUrl(url: String): String {
    return LEGACY_CONTENT_PREFIXES
      .firstOrNull(url::startsWith)
      ?.let { url.replace(it, ZimFileReader.CONTENT_PREFIX) }
      ?: url
  }

  @Suppress("NestedBlockDepth")
  fun handleUnsupportedFiles(url: String): Boolean {
    val extension = MimeTypeMap.getFileExtensionFromUrl(url)
    if (DOCUMENT_TYPES.containsKey(extension)) {
      callback.showSaveOrOpenUnsupportedFilesDialog(url, DOCUMENT_TYPES[extension])
      return true
    }
    return false
  }

  override fun onReceivedError(
    view: WebView?,
    request: WebResourceRequest?,
    error: WebResourceError?
  ) {
    callback.webViewFailedLoading(request?.url.toString())
  }

  override fun onPageFinished(view: WebView, url: String) {
    val invalidUrl = url == ZimFileReader.CONTENT_PREFIX + "null"
    Log.d(TAG_KIWIX, "invalidUrl = $invalidUrl")
    if (invalidUrl) {
      return
    }
    jumpToAnchor(view, url)
    callback.webViewUrlFinishedLoading()
  }

  /*
   * If 2 urls are the same aside from the `#` component then calling load
   * does not trigger our loading code and the webview will go to the anchor
   * */
  private fun jumpToAnchor(view: WebView, loadedUrl: String) {
    urlWithAnchor?.let {
      if (it.startsWith(loadedUrl)) {
        view.loadUrl(it)
        urlWithAnchor = null
      }
    }
  }

  override fun shouldInterceptRequest(
    view: WebView,
    request: WebResourceRequest
  ): WebResourceResponse? {
    val url = convertLegacyUrl(request.url.toString())
    return if (url.startsWith(ZimFileReader.CONTENT_PREFIX)) {
      zimReaderContainer.load(url, request.requestHeaders)
    } else {
      // Return an empty WebResourceResponse for the external resource to prevent
      // it from being loaded. Passing null would trigger an attempt to load the resource.
      WebResourceResponse(
        "text/css",
        Charsets.UTF_8.name(),
        null
      )
    }
  }

  companion object {
    private val DOCUMENT_TYPES: HashMap<String?, String?> = object : HashMap<String?, String?>() {
      init {
        put("epub", "application/epub+zip")
        put("pdf", "application/pdf")
      }
    }
    private val LEGACY_CONTENT_PREFIXES = arrayOf(
      "zim://content/",
      "content://${instance.packageName}.zim.base/".toUri().toString()
    )
  }
}
