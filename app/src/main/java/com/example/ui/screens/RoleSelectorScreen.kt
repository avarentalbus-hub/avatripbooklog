package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Red400
import com.example.ui.theme.Red500
import com.example.ui.theme.Sky400
import com.example.ui.theme.Sky500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun RoleSelectorScreen(
  driverCount: Int,
  busCount: Int,
  tripsCount: Int,
  expensesCount: Int,
  onSelectDriver: () -> Unit,
  onSelectAdmin: () -> Unit,
  onOpenCleaner: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(Slate950, Slate900, Slate950)
        )
      )
      .padding(horizontal = 20.dp, vertical = 24.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // App Branding Logo
    Box(
      modifier = Modifier
        .size(72.dp)
        .background(
          Brush.linearGradient(
            listOf(Sky500.copy(alpha = 0.25f), Emerald400.copy(alpha = 0.25f))
          ),
          RoundedCornerShape(22.dp)
        )
        .border(1.5.dp, Sky400.copy(alpha = 0.4f), RoundedCornerShape(22.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.DirectionsBus,
        contentDescription = "Fleet Logo",
        tint = Sky400,
        modifier = Modifier.size(38.dp)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "FleetShift",
        color = Color.White,
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.5).sp
      )
      Spacer(modifier = Modifier.width(8.dp))
      Box(
        modifier = Modifier
          .background(Emerald400.copy(alpha = 0.15f), CircleShape)
          .border(1.dp, Emerald400.copy(alpha = 0.35f), CircleShape)
          .padding(horizontal = 8.dp, vertical = 2.dp)
      ) {
        Text(
          text = "AVA FLEET",
          color = Emerald400,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Text(
      text = "AVA Transport & Rental • Fleet Shift Logger",
      color = Slate400,
      fontSize = 12.sp,
      modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
    )

    // Option 1: Driver Portal Card
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Slate900),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Slate800, RoundedCornerShape(20.dp))
        .clickable(onClick = onSelectDriver)
        .testTag("driver_portal_card")
    ) {
      Row(
        modifier = Modifier.padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(50.dp)
            .background(Sky500.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = Sky400,
            modifier = Modifier.size(26.dp)
          )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Driver Portal",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Pre-trip check, 4-stage ODO proof, extra trips & fuel slips",
            color = Slate400,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            modifier = Modifier.padding(top = 2.dp)
          )
        }

        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = null,
          tint = Slate700
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Option 2: Admin Control Center Card
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Slate900),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Slate800, RoundedCornerShape(20.dp))
        .clickable(onClick = onSelectAdmin)
        .testTag("admin_portal_card")
    ) {
      Row(
        modifier = Modifier.padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(50.dp)
            .background(Color(0xFF7C3AED).copy(alpha = 0.18f), RoundedCornerShape(14.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = Color(0xFFA78BFA),
            modifier = Modifier.size(26.dp)
          )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Admin Control Center",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Roster & PIN resets, audit trail, bus registry & exports",
            color = Slate400,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            modifier = Modifier.padding(top = 2.dp)
          )
        }

        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = null,
          tint = Slate700
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // PROMINENT QUICK FEATURE: Clear All Tester Data Easily (USER'S PRIMARY REQUEST)
    Card(
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.85f)),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Red500.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
        .clickable(onClick = onOpenCleaner)
        .testTag("quick_clear_tester_data_card")
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .background(Red500.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CleaningServices,
            contentDescription = null,
            tint = Red400,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Clear Tester Data",
              color = Color.White,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${tripsCount + expensesCount} test items",
              color = Red400,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
          Text(
            text = "Tap to purge dummy shifts, test fuel slips & extra trips easily",
            color = Slate400,
            fontSize = 11.sp
          )
        }

        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = null,
          tint = Red400.copy(alpha = 0.6f)
        )
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Footer info
    Row(
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Shield,
        contentDescription = null,
        tint = Emerald400,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "Offline-First • Watermarked Camera Verification",
        color = Slate400,
        fontSize = 11.sp
      )
    }
  }
}
