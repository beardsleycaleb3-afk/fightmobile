package com.example.model

import android.app.Application
import android.webkit.WebView
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ui.theme.ArtisticCyan
import com.example.ui.theme.ArtisticGreen
import com.example.ui.theme.ArtisticOrange
import com.example.ui.theme.ArtisticPrimaryContainer
import com.example.ui.theme.ArtisticYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class EngineMode(val title: String, val jsModeName: String, val description: String) {
  PHASER_PHYSICS("Phaser3 / 2D Physics", "phaser", "2D Rigid-Body Physics Engine with Impulse Collision Solver"),
  THREE_JS_3D("Three.js / 3D AR Light", "three", "3D Directional Lighting & Cast Shadow Simulation"),
  FABRIC_2D_EDITOR("Fabric.js / 2D Editor", "fabric", "2D Interactive Object Manipulation & Property Editor")
}

enum class BottomNavTab(val label: String) {
  SCENE("Scene"),
  SCRIPT("Script"),
  ASSETS("Assets"),
  LOGS("Logs")
}

data class CollisionEvent(
  val id: Long,
  val timestamp: String,
  val objA: String,
  val objB: String,
  val impulse: Float,
  val x: Int,
  val y: Int
)

data class SelectedObjectInfo(
  val name: String = "Wanderer Hero",
  val x: Float = 160.0f,
  val y: Float = 320.0f,
  val rotDeg: Int = 0,
  val velocity: Float = 0.0f
)

data class SpawnableAsset(
  val id: String,
  val name: String,
  val jsType: String,
  val restitution: Float,
  val mass: Float,
  val color: Color,
  val description: String,
  val animationName: String,
  val animationUse: String
)

val defaultSpawnableAssets = listOf(
  SpawnableAsset(
    id = "crate",
    name = "Wooden Crate",
    jsType = "crate",
    restitution = 0.45f,
    mass = 12.0f,
    color = ArtisticPrimaryContainer,
    description = "Standard rigid box with medium friction and wood restitution.",
    animationName = "Impact Wobble (Elastic Decay)",
    animationUse = "Use: Physical feedback for crates & obstacles when hit by collisions or shockwaves."
  ),
  SpawnableAsset(
    id = "sphere",
    name = "Steel Sphere",
    jsType = "sphere",
    restitution = 0.30f,
    mass = 25.0f,
    color = ArtisticCyan,
    description = "Heavy circular rigid body with high momentum and low bounce.",
    animationName = "Continuous Gyro Spin",
    animationUse = "Use: Showcases 360° specular highlights across all facets of 3D geometry."
  ),
  SpawnableAsset(
    id = "diamond",
    name = "Bouncy Gem",
    jsType = "diamond",
    restitution = 0.95f,
    mass = 8.0f,
    color = ArtisticYellow,
    description = "High-restitution rotated diamond that ricochets across the arena.",
    animationName = "Orbital Hover / Levitate",
    animationUse = "Use: Attracts player attention to floating gems and collectibles without touching ground."
  ),
  SpawnableAsset(
    id = "hero",
    name = "Wanderer Brawler",
    jsType = "hero",
    restitution = 0.20f,
    mass = 18.0f,
    color = ArtisticOrange,
    description = "Hero fighter character body controlled by the virtual joystick.",
    animationName = "Pulse Resonation (Breathing Scale)",
    animationUse = "Use: Visual heartbeat indicator for active hero selection and energy charge."
  ),
  SpawnableAsset(
    id = "platform",
    name = "Neon Ramp",
    jsType = "platform",
    restitution = 0.60f,
    mass = 0.0f,
    color = ArtisticGreen,
    description = "Static inclined obstacle platform with separating axis collision.",
    animationName = "Static Ambient Shading",
    animationUse = "Use: Stable structural platform foundation with directional Lambertian shadow casting."
  )
)

class CreativeEngineViewModel(application: Application) : AndroidViewModel(application) {
  private var webViewRef: WebView? = null
  private var nextCollisionId: Long = 1L
  private val repository = ArcadeHighScoreRepository(application)

  val arcadeStats = repository.arcadeStatsFlow.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ArcadeStats()
  )

  fun recordArcadeScore(score: Int, bosses: Int, collectables: Int, rank: String, lore: String) {
    viewModelScope.launch {
      repository.recordArcadeScore(score, bosses, collectables, rank, lore)
    }
  }

  fun resetArcadeScores() {
    viewModelScope.launch {
      repository.resetScores()
    }
  }

  private val _currentMode = MutableStateFlow(EngineMode.PHASER_PHYSICS)
  val currentMode: StateFlow<EngineMode> = _currentMode.asStateFlow()

  private val _selectedTab = MutableStateFlow(BottomNavTab.SCENE)
  val selectedTab: StateFlow<BottomNavTab> = _selectedTab.asStateFlow()

  private val _collisionLogs = MutableStateFlow<List<CollisionEvent>>(emptyList())
  val collisionLogs: StateFlow<List<CollisionEvent>> = _collisionLogs.asStateFlow()

  private val _selectedObject = MutableStateFlow(SelectedObjectInfo())
  val selectedObject: StateFlow<SelectedObjectInfo> = _selectedObject.asStateFlow()

  private val _fps = MutableStateFlow(60)
  val fps: StateFlow<Int> = _fps.asStateFlow()

  private val _objectCount = MutableStateFlow(8)
  val objectCount: StateFlow<Int> = _objectCount.asStateFlow()

  private val _gravityEnabled = MutableStateFlow(true)
  val gravityEnabled: StateFlow<Boolean> = _gravityEnabled.asStateFlow()

  // Live Physics Engine Script Parameters
  private val _gravityStrength = MutableStateFlow(9.8f)
  val gravityStrength: StateFlow<Float> = _gravityStrength.asStateFlow()

  private val _restitutionMult = MutableStateFlow(1.0f)
  val restitutionMult: StateFlow<Float> = _restitutionMult.asStateFlow()

  private val _airResistance = MutableStateFlow(0.99f)
  val airResistance: StateFlow<Float> = _airResistance.asStateFlow()

  private val _isFightMobileOnline = MutableStateFlow(false)
  val isFightMobileOnline: StateFlow<Boolean> = _isFightMobileOnline.asStateFlow()

  fun toggleFightMobileOnline() {
    val newState = !_isFightMobileOnline.value
    _isFightMobileOnline.value = newState
    webViewRef?.post {
      if (newState) {
        webViewRef?.loadUrl("https://beardsleycaleb3-afk.github.io/fightmobile/")
      } else {
        webViewRef?.loadUrl("file:///android_asset/index.html")
      }
    }
  }

  fun launchFightMobileOnline() {
    if (!_isFightMobileOnline.value) {
      _isFightMobileOnline.value = true
      webViewRef?.post {
        webViewRef?.loadUrl("https://beardsleycaleb3-afk.github.io/fightmobile/")
      }
    }
  }

  fun returnToLocalEngine() {
    if (_isFightMobileOnline.value) {
      _isFightMobileOnline.value = false
      webViewRef?.post {
        webViewRef?.loadUrl("file:///android_asset/index.html")
      }
    }
  }

  fun bindWebView(webView: WebView) {
    webViewRef = webView
    if (_isFightMobileOnline.value) {
      webViewRef?.loadUrl("https://beardsleycaleb3-afk.github.io/fightmobile/")
    } else {
      webViewRef?.loadUrl("file:///android_asset/index.html")
    }
  }

  fun setMode(mode: EngineMode) {
    _currentMode.value = mode
    webViewRef?.post {
      webViewRef?.evaluateJavascript("if(window.setEngineMode) window.setEngineMode('${mode.jsModeName}')", null)
    }
  }

  fun selectTab(tab: BottomNavTab) {
    _selectedTab.value = tab
  }

  fun toggleGravity() {
    _gravityEnabled.value = !_gravityEnabled.value
    webViewRef?.post {
      webViewRef?.evaluateJavascript("if(window.toggleGravityMode) window.toggleGravityMode()", null)
    }
  }

  fun spawnObject(jsType: String) {
    webViewRef?.post {
      webViewRef?.evaluateJavascript("if(window.spawnPhysicsObject) window.spawnPhysicsObject('$jsType')", null)
    }
  }

  fun triggerShockwave() {
    webViewRef?.post {
      webViewRef?.evaluateJavascript("if(window.triggerRadialShockwave) window.triggerRadialShockwave()", null)
    }
  }

  fun resetArena() {
    webViewRef?.post {
      webViewRef?.evaluateJavascript("if(window.resetArenaScene) window.resetArenaScene()", null)
    }
    _collisionLogs.value = emptyList()
  }

  fun addCollisionEvent(objA: String, objB: String, impulse: Float, x: Int, y: Int) {
    val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
    val event = CollisionEvent(
      id = nextCollisionId++,
      timestamp = timestamp,
      objA = objA,
      objB = objB,
      impulse = impulse,
      x = x,
      y = y
    )
    // Keep latest 80 logs
    val updated = (listOf(event) + _collisionLogs.value).take(80)
    _collisionLogs.value = updated
  }

  fun updateTelemetry(currentFps: Int, totalObjects: Int) {
    _fps.value = currentFps
    _objectCount.value = totalObjects
  }

  fun updateSelection(name: String, x: Float, y: Float, rot: Int, vel: Float) {
    _selectedObject.value = SelectedObjectInfo(name, x, y, rot, vel)
  }

  fun clearLogs() {
    _collisionLogs.value = emptyList()
  }

  fun updateGravityStrength(value: Float) {
    _gravityStrength.value = value
  }

  fun updateRestitutionMult(value: Float) {
    _restitutionMult.value = value
  }

  fun updateAirResistance(value: Float) {
    _airResistance.value = value
  }
}
