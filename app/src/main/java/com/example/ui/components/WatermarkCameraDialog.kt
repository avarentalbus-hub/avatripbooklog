package com.example.ui.components

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Sky400
import com.example.ui.theme.Sky500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WatermarkCameraDialog(
  driverName: String,
  driverId: String,
  busPlate: String,
  stageTitle: String,
  odometerKm: Double,
  onCaptureConfirmed: (capturedBitmap: Bitmap?, lat: Double?, lng: Double?) -> Unit,
  onDismiss: () -> Unit
) {
  val context = androidx.compose.ui.platform.LocalContext.current
  var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap ->
    if (bitmap != null) {
      previewBitmap = bitmap
    }
  }

  val currentTime = remember {
    SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date())
  }
  var gpsCoords by remember { mutableStateOf(com.example.util.GpsCoordinates.DUBAI_HQ) }

  androidx.compose.runtime.LaunchedEffect(Unit) {
    if (com.example.util.LocationHelper.hasLocationPermission(context)) {
      gpsCoords = com.example.util.LocationHelper.getCurrentLocation(context)
      com.example.util.LocationHelper.getLocationUpdates(context, intervalMs = 2500L).collect {
        gpsCoords = it
      }
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Slate900),
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .border(1.dp, Slate800, RoundedCornerShape(24.dp))
        .padding(vertical = 12.dp)
        .testTag("watermark_camera_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Title Bar
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            Text(
              text = "Watermarked Checkpoint Proof",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = stageTitle,
              color = Sky400,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate400)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Watermark Visual Container
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(Slate950, RoundedCornerShape(16.dp))
            .border(1.dp, Slate800, RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center
        ) {
          if (previewBitmap != null) {
            Image(
              bitmap = previewBitmap!!.asImageBitmap(),
              contentDescription = "Captured preview",
              contentScale = ContentScale.Crop,
              modifier = Modifier.matchParentSize()
            )
          } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = Sky400.copy(alpha = 0.6f),
                modifier = Modifier.size(54.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "${odometerKm.toInt()} km",
                color = Color.White,
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Vehicle: $busPlate",
                color = Slate400,
                fontSize = 12.sp
              )
            }
          }

          // Live Watermark Stamp Overlay at Bottom
          Box(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .fillMaxWidth()
              .padding(10.dp)
              .background(Color(0xE60A0F1A), RoundedCornerShape(12.dp))
              .border(1.dp, Sky500.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
              .padding(10.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .width(4.dp)
                  .height(48.dp)
                  .background(Sky500, RoundedCornerShape(2.dp))
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "STAGE: $stageTitle",
                  color = Sky400,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = "TIME: $currentTime",
                  color = Color.White,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = "DRIVER: $driverName ($driverId) | BUS: $busPlate",
                  color = Color(0xFFCBD5E1),
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = gpsCoords.formatWatermark(),
                  color = Emerald400,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Capture Controls
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedButton(
            onClick = { cameraLauncher.launch(null) },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Slate700)),
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .testTag("launch_camera_button")
          ) {
            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Take Photo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }

          Button(
            onClick = {
              onCaptureConfirmed(previewBitmap, gpsCoords.latitude, gpsCoords.longitude)
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Sky500,
              contentColor = Color.White
            ),
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .testTag("confirm_save_stage_button")
          ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Save Proof", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Watermark includes real-time timestamp, vehicle license, and GPS coordinates for audit compliance.",
          color = Slate400,
          fontSize = 10.sp,
          lineHeight = 14.sp
        )
      }
    }
  }
}
