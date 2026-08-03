package com.example.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.CreativeEngineViewModel
import com.example.ui.theme.ArtisticBorder
import com.example.ui.theme.ArtisticCanvasBg
import com.example.ui.theme.ArtisticOnPrimaryContainer
import com.example.ui.theme.ArtisticOnTertiaryContainer
import com.example.ui.theme.ArtisticPrimaryContainer
import com.example.ui.theme.ArtisticTertiaryContainer

class AndroidEngineBridge(private val viewModel: CreativeEngineViewModel) {
  @JavascriptInterface
  fun onCollision(objA: String, objB: String, impulse: Float, x: Int, y: Int) {
    viewModel.addCollisionEvent(objA, objB, impulse, x, y)
  }

  @JavascriptInterface
  fun onFpsUpdate(fps: Int, count: Int) {
    viewModel.updateTelemetry(fps, count)
  }

  @JavascriptInterface
  fun onObjectSelected(name: String, x: Float, y: Float, rot: Int, vel: Float) {
    viewModel.updateSelection(name, x, y, rot, vel)
  }

  @JavascriptInterface
  fun onShockwave(x: Int, y: Int) {
    // Optional telemetry trigger
  }

  @JavascriptInterface
  fun onLaunchFightMobile() {
    viewModel.launchFightMobileOnline()
  }
}

private fun ensureWebViewCacheDirs(context: Context) {
  try {
    val cacheDir = context.cacheDir
    val baseDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache")
    File(baseDir, "js").mkdirs()
    File(baseDir, "wasm").mkdirs()
  } catch (e: Exception) {
    // Ignore filesystem exceptions
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SceneCanvasView(
  viewModel: CreativeEngineViewModel,
  modifier: Modifier = Modifier
) {
  val fps by viewModel.fps.collectAsState()
  val objectCount by viewModel.objectCount.collectAsState()
  val isFightMobileOnline by viewModel.isFightMobileOnline.collectAsState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .background(ArtisticCanvasBg, shape = RoundedCornerShape(28.dp))
      .border(
        width = 4.dp,
        color = ArtisticBorder,
        shape = RoundedCornerShape(28.dp)
      )
      .clip(RoundedCornerShape(24.dp))
  ) {
    // High-performance WebView Canvas Engine
    AndroidView(
      factory = { ctx ->
        ensureWebViewCacheDirs(ctx)
        val attributedCtx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          ctx.createAttributionContext("WebView")
        } else {
          ctx
        }
        WebView(attributedCtx).apply {
          settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_DEFAULT
          }
          webChromeClient = WebChromeClient()
          webViewClient = WebViewClient()
          addJavascriptInterface(AndroidEngineBridge(viewModel), "AndroidBridge")
          setBackgroundColor(0x1C1B1F)

          viewModel.bindWebView(this)
        }
      },
      modifier = Modifier.fillMaxSize()
    )

    // Top-Left Toggle: FightMobile Online Arcade vs Local Engine
    Surface(
      color = if (isFightMobileOnline) Color(0xFF6750A4) else Color(0xCC2B2930),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(16.dp)
        .clickable { viewModel.toggleFightMobileOnline() }
        .border(
          width = 1.dp,
          color = if (isFightMobileOnline) Color(0xFFEADDFF) else ArtisticBorder.copy(alpha = 0.5f),
          shape = RoundedCornerShape(14.dp)
        )
        .testTag("action_toggle_fightmobile")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isFightMobileOnline) "🥊 FIGHTMOBILE ONLINE (LIVE)" else "🥊 FIGHTMOBILE ONLINE",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = if (isFightMobileOnline) Color(0xFFFFE37A) else Color(0xFFEADDFF)
        )
      }
    }

    // Top-Center Overlay: Telemetry FPS & Bodies indicator
    Row(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 16.dp)
        .background(
          color = Color(0xCC49454F),
          shape = RoundedCornerShape(16.dp)
        )
        .border(1.dp, ArtisticBorder.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        .padding(horizontal = 14.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "FPS: $fps",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF48DF88)
      )
      Text(
        text = "BODIES: $objectCount",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFD0BCFF)
      )
    }

    // Top-Right Corner Action Controls: Shockwave & Reset
    Column(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Shockwave Button
      Surface(
        color = ArtisticTertiaryContainer,
        shape = CircleShape,
        modifier = Modifier
          .size(44.dp)
          .clickable { viewModel.triggerShockwave() }
          .testTag("action_shockwave")
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.FlashOn,
            contentDescription = "Radial Physics Shockwave",
            tint = ArtisticOnTertiaryContainer
          )
        }
      }

      // Reset Arena Button
      Surface(
        color = ArtisticPrimaryContainer,
        shape = CircleShape,
        modifier = Modifier
          .size(44.dp)
          .clickable { viewModel.resetArena() }
          .testTag("action_reset")
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Reset Arena",
            tint = ArtisticOnPrimaryContainer
          )
        }
      }
    }
  }
}
