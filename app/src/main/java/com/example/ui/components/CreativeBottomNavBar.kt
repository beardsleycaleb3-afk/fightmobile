package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
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
import com.example.model.BottomNavTab
import com.example.ui.theme.ArtisticBorder
import com.example.ui.theme.ArtisticOnPrimaryContainer
import com.example.ui.theme.ArtisticPrimaryContainer
import com.example.ui.theme.ArtisticSecondaryContainer

@Composable
fun CreativeBottomNavBar(
  selectedTab: BottomNavTab,
  onTabSelected: (BottomNavTab) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(80.dp)
      .background(ArtisticSecondaryContainer)
      .border(
        width = 1.dp,
        color = ArtisticBorder,
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
      )
      .padding(horizontal = 8.dp),
    horizontalArrangement = Arrangement.SpaceAround,
    verticalAlignment = Alignment.CenterVertically
  ) {
    BottomNavTab.values().forEach { tab ->
      val isSelected = (tab == selectedTab)
      val icon: ImageVector = when (tab) {
        BottomNavTab.SCENE -> Icons.Default.Dashboard
        BottomNavTab.SCRIPT -> Icons.Default.Code
        BottomNavTab.ASSETS -> Icons.Default.Apps
        BottomNavTab.LOGS -> Icons.Default.List
      }

      val contentAlpha = if (isSelected) 1.0f else 0.6f

      Column(
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .clickable { onTabSelected(tab) }
          .padding(horizontal = 8.dp, vertical = 4.dp)
          .testTag("nav_tab_${tab.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Active indicator pill: bg-[#E8DEF8] px-5 py-1 rounded-full
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) ArtisticPrimaryContainer else Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 4.dp),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = tab.label,
            tint = ArtisticOnPrimaryContainer.copy(alpha = contentAlpha)
          )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = tab.label,
          fontSize = 11.sp,
          fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
          color = ArtisticOnPrimaryContainer.copy(alpha = contentAlpha)
        )
      }
    }
  }
}
