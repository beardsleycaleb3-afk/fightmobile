package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EngineMode
import com.example.ui.theme.ArtisticBg
import com.example.ui.theme.ArtisticOnBg
import com.example.ui.theme.ArtisticOnPrimaryContainer
import com.example.ui.theme.ArtisticOnSecondaryContainer
import com.example.ui.theme.ArtisticOnTertiaryContainer
import com.example.ui.theme.ArtisticPrimaryContainer
import com.example.ui.theme.ArtisticSecondaryContainer
import com.example.ui.theme.ArtisticTertiaryContainer

@Composable
fun CreativeEngineHeader(
  currentMode: EngineMode,
  gravityEnabled: Boolean,
  onModeSelected: (EngineMode) -> Unit,
  onToggleGravity: () -> Unit,
  onMenuClick: () -> Unit = {},
  onSettingsClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(ArtisticBg)
  ) {
    // Header: M3 Small Top App Bar (bg-[#FEF7FF])
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onMenuClick,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .testTag("menu_button")
        ) {
          Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Open Menu",
            tint = ArtisticOnBg
          )
        }
        Text(
          text = "Creative Engine",
          fontSize = 20.sp,
          fontWeight = FontWeight.Medium,
          color = ArtisticOnBg,
          letterSpacing = (-0.5).sp
        )
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onSettingsClick,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .testTag("settings_button")
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = ArtisticOnBg
          )
        }

        // Play/Pause Gravity Button (#EADDFF bg, #21005D text)
        Surface(
          color = ArtisticTertiaryContainer,
          shape = CircleShape,
          modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onToggleGravity)
            .testTag("toggle_gravity_button")
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (gravityEnabled) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (gravityEnabled) "Pause Gravity" else "Resume Gravity",
              tint = ArtisticOnTertiaryContainer
            )
          }
        }
      }
    }

    // Sub-navigation: Engine Mode Toggle (bg-[#E8DEF8] active vs #F3EDF7 inactive)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      EngineMode.values().forEach { mode ->
        val isActive = (mode == currentMode)
        val bgColor = if (isActive) ArtisticPrimaryContainer else ArtisticSecondaryContainer
        val textColor = if (isActive) ArtisticOnPrimaryContainer else ArtisticOnSecondaryContainer
        val icon: ImageVector = when (mode) {
          EngineMode.PHASER_PHYSICS -> Icons.Default.SportsEsports
          EngineMode.THREE_JS_3D -> Icons.Default.ViewInAr
          EngineMode.FABRIC_2D_EDITOR -> Icons.Default.Code
        }

        Surface(
          color = bgColor,
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onModeSelected(mode) }
            .testTag("mode_${mode.jsModeName}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = icon,
              contentDescription = mode.title,
              tint = textColor,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = mode.title,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = textColor
            )
          }
        }
      }
    }
  }
}
