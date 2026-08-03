package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.BottomNavTab
import com.example.model.CreativeEngineViewModel
import com.example.ui.components.CreativeBottomNavBar
import com.example.ui.components.CreativeEngineHeader
import com.example.ui.theme.ArtisticOnPrimaryContainer
import com.example.ui.theme.ArtisticPrimary
import com.example.ui.theme.ArtisticPrimaryContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.views.AssetsCatalogView
import com.example.ui.views.CollisionLogsView
import com.example.ui.views.SceneCanvasView
import com.example.ui.views.ScriptInspectorView

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = false) {
        val viewModel: CreativeEngineViewModel = viewModel()
        CreativeEngineApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun CreativeEngineApp(
  viewModel: CreativeEngineViewModel,
  modifier: Modifier = Modifier
) {
  val currentMode by viewModel.currentMode.collectAsState()
  val selectedTab by viewModel.selectedTab.collectAsState()
  val gravityEnabled by viewModel.gravityEnabled.collectAsState()

  var showMenuDialog by remember { mutableStateOf(false) }
  var showSettingsDialog by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      CreativeEngineHeader(
        currentMode = currentMode,
        gravityEnabled = gravityEnabled,
        onModeSelected = { viewModel.setMode(it) },
        onToggleGravity = { viewModel.toggleGravity() },
        onMenuClick = { showMenuDialog = true },
        onSettingsClick = { showSettingsDialog = true }
      )
    },
    bottomBar = {
      CreativeBottomNavBar(
        selectedTab = selectedTab,
        onTabSelected = { viewModel.selectTab(it) }
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTab) {
        BottomNavTab.SCENE -> SceneCanvasView(viewModel = viewModel)
        BottomNavTab.SCRIPT -> ScriptInspectorView(viewModel = viewModel)
        BottomNavTab.ASSETS -> AssetsCatalogView(viewModel = viewModel)
        BottomNavTab.LOGS -> CollisionLogsView(viewModel = viewModel)
      }
    }
  }

  // Menu Info Dialog
  if (showMenuDialog) {
    AlertDialog(
      onDismissRequest = { showMenuDialog = false },
      title = {
        Text(
          text = "Creative Engine: Artistic Flair",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = ArtisticPrimary
        )
      },
      text = {
        Column {
          Text("• Artistic Flair Theme Active (#FEF7FF bg, #E8DEF8 / #EADDFF accents).")
          Text("• Phaser3 / 2D Physics: Rigid-body impulse collision resolution with restitution, sparks, and joystick control.")
          Text("• Three.js / 3D AR Light: Perspective 3D directional lighting with shadow casting.")
          Text("• Fabric.js / 2D Editor: Tap objects to select, drag to position, resize/rotate with corner handles.")
        }
      },
      confirmButton = {
        Button(
          onClick = { showMenuDialog = false },
          colors = ButtonDefaults.buttonColors(
            containerColor = ArtisticPrimaryContainer,
            contentColor = ArtisticOnPrimaryContainer
          )
        ) {
          Text("Close")
        }
      }
    )
  }

  // Settings Dialog
  if (showSettingsDialog) {
    AlertDialog(
      onDismissRequest = { showSettingsDialog = false },
      title = {
        Text(
          text = "Physics Engine Settings",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = ArtisticPrimary
        )
      },
      text = {
        Column {
          Text("Adjust live simulation controls:")
          Text("• Gravity: ${if (gravityEnabled) "9.8 m/s² (Active)" else "0G (Zero-G Space)"}")
          Text("• Collision Telemetry: Stream impulses to Logs Console.")
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.resetArena()
            showSettingsDialog = false
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = ArtisticPrimaryContainer,
            contentColor = ArtisticOnPrimaryContainer
          )
        ) {
          Text("Reset Arena")
        }
      },
      dismissButton = {
        Button(
          onClick = { showSettingsDialog = false }
        ) {
          Text("Done")
        }
      }
    )
  }
}

