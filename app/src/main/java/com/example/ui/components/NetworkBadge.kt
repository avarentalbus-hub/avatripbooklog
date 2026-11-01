package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Red400

@Composable
fun NetworkBadge(
  isOnline: Boolean,
  queueCount: Int,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  val bgColor by animateColorAsState(
    if (isOnline) Color(0x2610B981) else Color(0x26EF4444),
    label = "badgeBg"
  )
  val borderColor by animateColorAsState(
    if (isOnline) Color(0x4D10B981) else Color(0x4DEF4444),
    label = "badgeBorder"
  )
  val textColor = if (isOnline) Emerald400 else Red400

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .testTag("network_badge_toggle")
      .border(1.dp, borderColor, RoundedCornerShape(999.dp))
      .background(bgColor, RoundedCornerShape(999.dp))
      .clickable(onClick = onToggle)
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Icon(
      imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
      contentDescription = if (isOnline) "Online" else "Offline",
      tint = textColor,
      modifier = Modifier.size(13.dp)
    )
    Spacer(modifier = Modifier.width(5.dp))
    Text(
      text = if (isOnline) "Online Sync" else "Offline Mode",
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold
    )
    if (queueCount > 0) {
      Spacer(modifier = Modifier.width(6.dp))
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .background(Color.Black.copy(alpha = 0.5f), CircleShape)
          .padding(horizontal = 6.dp, vertical = 1.dp)
      ) {
        Text(
          text = "$queueCount",
          color = Color.White,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
