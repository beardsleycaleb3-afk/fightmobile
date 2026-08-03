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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.model.CreativeEngineViewModel
import com.example.ui.theme.ArtisticBg
import com.example.ui.theme.ArtisticBorder
import com.example.ui.theme.ArtisticCanvasBg
import com.example.ui.theme.ArtisticCyan
import com.example.ui.theme.ArtisticOnBg
import com.example.ui.theme.ArtisticOnPrimaryContainer
import com.example.ui.theme.ArtisticOnTertiaryContainer
import com.example.ui.theme.ArtisticPrimary
import com.example.ui.theme.ArtisticPrimaryContainer
import com.example.ui.theme.ArtisticSecondaryContainer
import com.example.ui.theme.ArtisticTertiaryContainer

@Composable
fun ScriptInspectorView(
  viewModel: CreativeEngineViewModel,
  modifier: Modifier = Modifier
) {
  val gravityStrength by viewModel.gravityStrength.collectAsState()
  val restitutionMult by viewModel.restitutionMult.collectAsState()
  val airResistance by viewModel.airResistance.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(ArtisticBg)
      .padding(16.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Card: Engine Physics Configuration
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(ArtisticSecondaryContainer)
        .border(1.dp, ArtisticBorder, RoundedCornerShape(20.dp))
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = "LIVE PHYSICS ENGINE CONSTANTS",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = ArtisticPrimary
      )

      // Gravity Strength Slider
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Gravity Acceleration (G):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ArtisticOnBg
          )
          Text(
            text = "${String.format("%.1f", gravityStrength)} m/s²",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = ArtisticPrimary
          )
        }
        Slider(
          value = gravityStrength,
          onValueChange = { viewModel.updateGravityStrength(it) },
          valueRange = 0.0f..15.0f,
          colors = SliderDefaults.colors(
            thumbColor = ArtisticPrimary,
            activeTrackColor = ArtisticPrimary
          ),
          modifier = Modifier.testTag("slider_gravity")
        )
      }

      // Restitution Multiplier Slider
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Global Restitution (Bounciness):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ArtisticOnBg
          )
          Text(
            text = "${String.format("%.2f", restitutionMult)}x",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = ArtisticPrimary
          )
        }
        Slider(
          value = restitutionMult,
          onValueChange = { viewModel.updateRestitutionMult(it) },
          valueRange = 0.2f..1.0f,
          colors = SliderDefaults.colors(
            thumbColor = ArtisticPrimary,
            activeTrackColor = ArtisticPrimary
          ),
          modifier = Modifier.testTag("slider_restitution")
        )
      }

      // Air Resistance Slider
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Air Resistance Damping:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ArtisticOnBg
          )
          Text(
            text = String.format("%.3f", airResistance),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = ArtisticPrimary
          )
        }
        Slider(
          value = airResistance,
          onValueChange = { viewModel.updateAirResistance(it) },
          valueRange = 0.95f..1.00f,
          colors = SliderDefaults.colors(
            thumbColor = ArtisticPrimary,
            activeTrackColor = ArtisticPrimary
          ),
          modifier = Modifier.testTag("slider_air")
        )
      }
    }

    // Interactive Code Display: Matter.js / Fabric.js / Three.js script
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(ArtisticCanvasBg)
        .border(2.dp, ArtisticBorder, RoundedCornerShape(20.dp))
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "CreativeEngine.js — Collision Resolver Rule",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = Color(0xFFD0BCFF)
        )
        Text(
          text = "VERLET / SAT",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF48DF88),
          modifier = Modifier
            .background(Color(0x3348DF88), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
        )
      }

      val scriptCode = """
// 2D Impulse Collision Response Equation
function applyCollisionImpulse(b1, b2, normal, pen) {
  const totalInvMass = b1.invMass + b2.invMass;
  if (totalInvMass <= 0) return;
  
  // Separate overlapping bodies
  b1.pos.sub(normal.clone().mult(pen * b1.invMass));
  b2.pos.add(normal.clone().mult(pen * b2.invMass));
  
  // Calculate relative velocity along normal
  const rVel = b2.vel.clone().sub(b1.vel);
  const velAlongNormal = rVel.dot(normal);
  if (velAlongNormal > 0) return;
  
  // Coefficient of Restitution
  const e = Math.min(b1.restitution, b2.restitution) * ${String.format("%.2f", restitutionMult)};
  const j = -(1 + e) * velAlongNormal / totalInvMass;
  
  // Apply impulse force vector
  const impulse = normal.clone().mult(j);
  b1.vel.sub(impulse.clone().mult(b1.invMass));
  b2.vel.add(impulse.clone().mult(b2.invMass));
  
  // Emit telemetry event to Android Logs Console
  if (j > 0.8) {
    AndroidBridge.onCollision(b1.name, b2.name, j);
  }
}
      """.trimIndent()

      Text(
        text = scriptCode,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        lineHeight = 16.sp,
        color = Color(0xFFEADDFF)
      )
    }

    // Three.js 3D Lighting & Scene Animation Presets Card
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(ArtisticSecondaryContainer)
        .border(1.dp, ArtisticBorder, RoundedCornerShape(20.dp))
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text(
        text = "3D LIGHTING & SCENE ANIMATION USES",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = ArtisticPrimary
      )

      AnimationPresetUseCard(
        title = "1. Orbital Hover / Levitate",
        equation = "yOffset = sin(time * 3.0) * 6px + Floating Aura",
        useCase = "Use: Attracts player attention to floating gems and collectibles without touching ground."
      )

      AnimationPresetUseCard(
        title = "2. Pulse Resonation",
        equation = "scale = 1.0 + sin(time * 4.0) * 0.12 + Energy Ring",
        useCase = "Use: Visual heartbeat indicator for active hero selection and energy charge."
      )

      AnimationPresetUseCard(
        title = "3. Continuous Gyro Spin",
        equation = "rot += 0.04 rad/frame + Dual-Axis Orbit Ring",
        useCase = "Use: Showcases 360° specular highlights across all facets of 3D geometry."
      )

      AnimationPresetUseCard(
        title = "4. Impact Wobble (Elastic Decay)",
        equation = "scaleY = 1.0 + sin(time * 18.0) * wobble * (0.94^t)",
        useCase = "Use: Physical squash-and-stretch feedback for crates and obstacles when hit."
      )
    }

    // Action Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End
    ) {
      Button(
        onClick = { viewModel.triggerShockwave() },
        colors = ButtonDefaults.buttonColors(
          containerColor = ArtisticTertiaryContainer,
          contentColor = ArtisticOnTertiaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("apply_script_button")
      ) {
        Text(
          text = "Apply Constants & Test Impulse",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

@Composable
fun AnimationPresetUseCard(
  title: String,
  equation: String,
  useCase: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Color.White)
      .border(1.dp, ArtisticBorder, RoundedCornerShape(12.dp))
      .padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text(
      text = title,
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      color = Color(0xFF1D1B20)
    )
    Text(
      text = equation,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.SemiBold,
      color = ArtisticPrimary
    )
    Text(
      text = useCase,
      fontSize = 11.sp,
      lineHeight = 15.sp,
      color = Color(0xFF49454F)
    )
  }
}
