package com.example.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BottomNavTab
import com.example.model.CreativeEngineViewModel
import com.example.model.SpawnableAsset
import com.example.model.defaultSpawnableAssets
import com.example.ui.theme.ArtisticBg
import com.example.ui.theme.ArtisticBorder
import com.example.ui.theme.ArtisticOnBg
import com.example.ui.theme.ArtisticOnPrimaryContainer
import com.example.ui.theme.ArtisticPrimary
import com.example.ui.theme.ArtisticPrimaryContainer
import com.example.ui.theme.ArtisticSecondaryContainer

@Composable
fun AssetsCatalogView(
  viewModel: CreativeEngineViewModel,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(ArtisticBg)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text(
        text = "SPAWNABLE 2D PHYSICS RIGID BODIES",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = ArtisticPrimary
      )
      Spacer(modifier = Modifier.height(4.dp))
    }

    items(defaultSpawnableAssets) { asset ->
      AssetCard(
        asset = asset,
        onSpawnClick = {
          viewModel.spawnObject(asset.jsType)
          viewModel.selectTab(BottomNavTab.SCENE)
        }
      )
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun AssetCard(
  asset: SpawnableAsset,
  onSpawnClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .background(ArtisticSecondaryContainer)
      .border(1.dp, ArtisticBorder, RoundedCornerShape(20.dp))
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Colored Shape Preview Icon
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(asset.color)
            .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
        )
        Column {
          Text(
            text = asset.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = ArtisticOnBg
          )
          Text(
            text = "Type: ${asset.jsType.uppercase()}",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = ArtisticPrimary
          )
        }
      }

      // Spawn Action Button
      Button(
        onClick = onSpawnClick,
        colors = ButtonDefaults.buttonColors(
          containerColor = ArtisticPrimaryContainer,
          contentColor = ArtisticOnPrimaryContainer
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.testTag("spawn_btn_${asset.id}")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Spawn ${asset.name}",
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Spawn",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Text(
      text = asset.description,
      fontSize = 12.sp,
      color = ArtisticOnBg.copy(alpha = 0.8f)
    )

    // Physics parameters pills
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      PhysicsParamPill(
        label = "REST: ${String.format("%.2f", asset.restitution)}",
        color = Color(0xFFD0BCFF)
      )
      PhysicsParamPill(
        label = if (asset.mass == 0.0f) "MASS: STATIC" else "MASS: ${asset.mass} kg",
        color = Color(0xFFE8DEF8)
      )
    }

    // 3D Scene Animation & Specific Use Box
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(Color(0xFFF3EDF7))
        .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(12.dp))
        .padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        text = "ANIMATION: ${asset.animationName.uppercase()}",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = ArtisticPrimary
      )
      Text(
        text = asset.animationUse,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        color = Color(0xFF49454F)
      )
    }
  }
}

@Composable
fun PhysicsParamPill(
  label: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    color = color.copy(alpha = 0.35f),
    shape = RoundedCornerShape(10.dp),
    modifier = modifier
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      color = ArtisticOnBg,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    )
  }
}
