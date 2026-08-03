package com.example.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.model.CollisionEvent
import com.example.model.CreativeEngineViewModel
import com.example.ui.theme.ArtisticBg
import com.example.ui.theme.ArtisticBorder
import com.example.ui.theme.ArtisticCanvasBg
import com.example.ui.theme.ArtisticOnBg
import com.example.ui.theme.ArtisticOnTertiaryContainer
import com.example.ui.theme.ArtisticPrimary
import com.example.ui.theme.ArtisticSecondaryContainer
import com.example.ui.theme.ArtisticTertiaryContainer

@Composable
fun CollisionLogsView(
  viewModel: CreativeEngineViewModel,
  modifier: Modifier = Modifier
) {
  val logs by viewModel.collisionLogs.collectAsState()
  val fps by viewModel.fps.collectAsState()
  val count by viewModel.objectCount.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(ArtisticBg)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Card: Real-time Telemetry Summary
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(ArtisticSecondaryContainer)
        .border(1.dp, ArtisticBorder, RoundedCornerShape(20.dp))
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "PHYSICS COLLISION TELEMETRY",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = ArtisticPrimary
        )
        Text(
          text = "Logged Impacts: ${logs.size} | Bodies: $count | FPS: $fps",
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          color = ArtisticOnBg
        )
      }

      Button(
        onClick = { viewModel.clearLogs() },
        colors = ButtonDefaults.buttonColors(
          containerColor = ArtisticTertiaryContainer,
          contentColor = ArtisticOnTertiaryContainer
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.testTag("clear_logs_button")
      ) {
        Text(
          text = "Clear",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // Terminal Style Log Console (#1C1B1F bg, monospace text)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1.0f)
        .clip(RoundedCornerShape(20.dp))
        .background(ArtisticCanvasBg)
        .border(2.dp, ArtisticBorder, RoundedCornerShape(20.dp))
        .padding(14.dp)
    ) {
      if (logs.isEmpty()) {
        Column(
          modifier = Modifier.fillMaxSize(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = "NO COLLISION IMPACTS RECORDED YET",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFD0BCFF)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Switch to Scene tab and move objects or trigger a shockwave to record collisions.",
            fontSize = 11.sp,
            color = Color(0xFFCAC4D0),
            fontFamily = FontFamily.Monospace
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(logs, key = { it.id }) { log ->
            CollisionLogItem(event = log)
          }
        }
      }
    }
  }
}

@Composable
fun CollisionLogItem(
  event: CollisionEvent,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF2A2830))
      .border(1.dp, Color(0xFF49454F), RoundedCornerShape(12.dp))
      .padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "[COLLISION] @ ${event.timestamp}",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF7AD9FF)
      )
      Text(
        text = "IMPULSE: ${String.format("%.1f", event.impulse)} N",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFFFE37A)
      )
    }

    Text(
      text = "${event.objA}  <->  ${event.objB}",
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      color = Color(0xFFE8DEF8)
    )

    Text(
      text = "Impact Coord: (X: ${event.x}, Y: ${event.y}) | Energy Restitution Active",
      fontSize = 10.sp,
      fontFamily = FontFamily.Monospace,
      color = Color(0xFFCAC4D0)
    )
  }
}
