package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Sky400
import com.example.ui.theme.Sky500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.util.GpsCoordinates
import com.example.util.LocationHelper
import com.example.util.WatermarkUtil
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CameraCaptureScreen(
  driverName: String,
  driverId: String,
  busPlate: String,
  stageTitle: String = "Shift Checkpoint Proof",
  odometerKm: Double? = null,
  onCaptureSuccess: (photoPath: String, bitmap: Bitmap, lat: Double?, lng: Double?) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  // Permissions state
  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
  }

  // Real-time location resolution via LocationHelper
  var gpsCoords by remember { mutableStateOf(GpsCoordinates.DUBAI_HQ) }

  // Continuously listen to real-time location updates via LocationHelper
  LaunchedEffect(hasCameraPermission) {
    if (LocationHelper.hasLocationPermission(context)) {
      gpsCoords = LocationHelper.getCurrentLocation(context)
      LocationHelper.getLocationUpdates(context, intervalMs = 2500L).collect { updated ->
        gpsCoords = updated
      }
    }
  }

  // Camera use cases & state
  var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
  var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_AUTO) }
  val imageCapture = remember {
    ImageCapture.Builder()
      .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
      .setFlashMode(flashMode)
      .build()
  }
  var boundCamera by remember { mutableStateOf<Camera?>(null) }
  var isCapturing by remember { mutableStateOf(false) }

  // Review Mode state
  var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var capturedFilePath by remember { mutableStateOf<String?>(null) }

  // Live ticking timestamp for watermark overlay
  var currentTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
  LaunchedEffect(Unit) {
    while (true) {
      delay(1000)
      currentTimestamp = System.currentTimeMillis()
    }
  }

  val timeString = remember(currentTimestamp) {
    SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date(currentTimestamp))
  }
  val gpsString = remember(gpsCoords) {
    gpsCoords.formatWatermark()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .testTag("camera_capture_screen")
  ) {
    if (!hasCameraPermission) {
      // Permission request screen
      CameraPermissionFallback(
        onRequestPermission = {
          permissionLauncher.launch(
            arrayOf(
              Manifest.permission.CAMERA,
              Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.ACCESS_COARSE_LOCATION
            )
          )
        },
        onSimulate = {
          val (lat, lng) = gpsCoords.toPair()
          val simBitmap = WatermarkUtil.createSimulatedOdometerBitmap(
            bus = busPlate,
            odometerKm = odometerKm ?: 124580.0
          )
          val path = WatermarkUtil.applyWatermarkAndSave(
            context = context,
            sourceBitmap = simBitmap,
            driverName = driverName,
            driverId = driverId,
            bus = busPlate,
            latitude = lat,
            longitude = lng,
            stageTitle = stageTitle
          )
          capturedBitmap = simBitmap
          capturedFilePath = path
        },
        onDismiss = onDismiss
      )
    } else if (capturedBitmap != null && capturedFilePath != null) {
      // Photo Review Screen
      CapturedPhotoReviewScreen(
        bitmap = capturedBitmap!!,
        stageTitle = stageTitle,
        busPlate = busPlate,
        onRetake = {
          capturedBitmap = null
          capturedFilePath = null
        },
        onConfirm = {
          onCaptureSuccess(
            capturedFilePath!!,
            capturedBitmap!!,
            gpsCoords.latitude,
            gpsCoords.longitude
          )
        }
      )
    } else {
      // Live CameraX Preview
      AndroidView(
        factory = { ctx ->
          val previewView = PreviewView(ctx).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
          }
          val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
          cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
              it.setSurfaceProvider(previewView.surfaceProvider)
            }
            try {
              cameraProvider.unbindAll()
              boundCamera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
              )
            } catch (exc: Exception) {
              Log.e("CameraCaptureScreen", "Use case binding failed", exc)
            }
          }, ContextCompat.getMainExecutor(ctx))
          previewView
        },
        modifier = Modifier.fillMaxSize()
      )

      // Top Control Bar
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(42.dp)
            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            .testTag("camera_close_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Flash toggle
          IconButton(
            onClick = {
              flashMode = when (flashMode) {
                ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                else -> ImageCapture.FLASH_MODE_AUTO
              }
              imageCapture.flashMode = flashMode
            },
            modifier = Modifier
              .size(42.dp)
              .background(Color.Black.copy(alpha = 0.5f), CircleShape)
              .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
              .testTag("camera_flash_toggle")
          ) {
            val flashIcon = when (flashMode) {
              ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
              ImageCapture.FLASH_MODE_OFF -> Icons.Default.FlashOff
              else -> Icons.Default.FlashAuto
            }
            Icon(
              imageVector = flashIcon,
              contentDescription = "Flash",
              tint = if (flashMode == ImageCapture.FLASH_MODE_ON) Amber400 else Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Switch Front / Back Camera
          IconButton(
            onClick = {
              cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
              } else {
                CameraSelector.DEFAULT_BACK_CAMERA
              }
            },
            modifier = Modifier
              .size(42.dp)
              .background(Color.Black.copy(alpha = 0.5f), CircleShape)
              .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
              .testTag("camera_switch_lens_button")
          ) {
            Icon(
              imageVector = Icons.Default.FlipCameraAndroid,
              contentDescription = "Switch Camera",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      // LIVE WATERMARK OVERLAY (Positioned directly on camera preview)
      Box(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 96.dp)
      ) {
        LiveWatermarkBadge(
          stageTitle = stageTitle,
          timeString = timeString,
          driverName = driverName,
          driverId = driverId,
          busPlate = busPlate,
          gpsString = gpsString
        )
      }

      // Bottom Capture Controls Bar
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
            )
          )
          .navigationBarsPadding()
          .padding(bottom = 20.dp, start = 24.dp, end = 24.dp)
      ) {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          // Quick Digital Simulation Option (useful for emulator testing)
          IconButton(
            onClick = {
              val (lat, lng) = gpsCoords.toPair()
              val simBitmap = WatermarkUtil.createSimulatedOdometerBitmap(
                bus = busPlate,
                odometerKm = odometerKm ?: 124580.0
              )
              val path = WatermarkUtil.applyWatermarkAndSave(
                context = context,
                sourceBitmap = simBitmap,
                driverName = driverName,
                driverId = driverId,
                bus = busPlate,
                latitude = lat,
                longitude = lng,
                stageTitle = stageTitle
              )
              capturedBitmap = simBitmap
              capturedFilePath = path
            },
            modifier = Modifier
              .size(44.dp)
              .background(Color.Black.copy(alpha = 0.6f), CircleShape)
              .border(1.dp, Sky400.copy(alpha = 0.4f), CircleShape)
              .testTag("simulate_gauge_snapshot_button")
          ) {
            Icon(
              imageVector = Icons.Default.Speed,
              contentDescription = "Simulate ODO",
              tint = Sky400,
              modifier = Modifier.size(22.dp)
            )
          }

          // Shutter Button
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(76.dp)
              .border(3.dp, Color.White, CircleShape)
              .padding(5.dp)
              .background(Color.White, CircleShape)
              .clickable(enabled = !isCapturing) {
                isCapturing = true
                val tempFile = File(context.cacheDir, "camerax_${System.currentTimeMillis()}.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

                imageCapture.takePicture(
                  outputOptions,
                  ContextCompat.getMainExecutor(context),
                  object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                      try {
                        val rotatedBitmap = WatermarkUtil.decodeAndRotateBitmap(tempFile.absolutePath)
                        val (lat, lng) = gpsCoords.toPair()
                        val finalPath = WatermarkUtil.applyWatermarkAndSave(
                          context = context,
                          sourceBitmap = rotatedBitmap,
                          driverName = driverName,
                          driverId = driverId,
                          bus = busPlate,
                          latitude = lat,
                          longitude = lng,
                          stageTitle = stageTitle
                        )
                        tempFile.delete()
                        capturedBitmap = rotatedBitmap
                        capturedFilePath = finalPath
                      } catch (e: Exception) {
                        Log.e("CameraCaptureScreen", "Watermark process failed", e)
                      } finally {
                        isCapturing = false
                      }
                    }

                    override fun onError(exception: ImageCaptureException) {
                      Log.e("CameraCaptureScreen", "Photo capture failed", exception)
                      isCapturing = false
                    }
                  }
                )
              }
              .testTag("camera_shutter_button")
          ) {
            if (isCapturing) {
              CircularProgressIndicator(
                color = Sky500,
                modifier = Modifier.size(28.dp)
              )
            } else {
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .background(Sky500, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CameraAlt,
                  contentDescription = "Capture",
                  tint = Color.White,
                  modifier = Modifier.size(26.dp)
                )
              }
            }
          }

          // Empty balance box
          Box(modifier = Modifier.size(44.dp))
        }
      }
    }
  }
}

@Composable
fun LiveWatermarkBadge(
  stageTitle: String,
  timeString: String,
  driverName: String,
  driverId: String,
  busPlate: String,
  gpsString: String,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(Color(0xE60A0F1A), RoundedCornerShape(14.dp))
      .border(1.dp, Sky500.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
      .padding(12.dp)
      .testTag("watermark_overlay_badge")
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .width(4.dp)
          .height(54.dp)
          .background(Sky500, RoundedCornerShape(2.dp))
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = "CHECKPOINT: $stageTitle",
          color = Sky400,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "TIME: $timeString",
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
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Emerald400,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(2.dp))
          Text(
            text = gpsString,
            color = Emerald400,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}

@Composable
private fun CapturedPhotoReviewScreen(
  bitmap: Bitmap,
  stageTitle: String,
  busPlate: String,
  onRetake: () -> Unit,
  onConfirm: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Slate950)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(16.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Header
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column {
        Text(
          text = "Review Watermarked Proof",
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "$stageTitle • $busPlate",
          color = Sky400,
          fontSize = 12.sp
        )
      }

      Box(
        modifier = Modifier
          .background(Emerald500.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
          .border(1.dp, Emerald400.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 3.dp)
      ) {
        Text(
          text = "VERIFIED",
          color = Emerald400,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // Photo Preview
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(vertical = 14.dp)
        .background(Color.Black, RoundedCornerShape(16.dp))
        .border(1.dp, Slate800, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp)),
      contentAlignment = Alignment.Center
    ) {
      Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Captured proof preview",
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
      )
    }

    // Buttons
    Row(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      OutlinedButton(
        onClick = onRetake,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = ButtonDefaults.outlinedButtonBorder.copy(
          brush = androidx.compose.ui.graphics.SolidColor(Slate700)
        ),
        modifier = Modifier
          .weight(1f)
          .height(50.dp)
          .testTag("retake_photo_button")
      ) {
        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "Retake", fontSize = 13.sp, fontWeight = FontWeight.Bold)
      }

      Button(
        onClick = onConfirm,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Emerald500,
          contentColor = Slate950
        ),
        modifier = Modifier
          .weight(1f)
          .height(50.dp)
          .testTag("confirm_photo_button")
      ) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "Confirm Proof", fontSize = 13.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun CameraPermissionFallback(
  onRequestPermission: () -> Unit,
  onSimulate: () -> Unit,
  onDismiss: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .background(Sky500.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
        .border(1.dp, Sky400.copy(alpha = 0.3f), RoundedCornerShape(18.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.CameraAlt,
        contentDescription = null,
        tint = Sky400,
        modifier = Modifier.size(32.dp)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Camera Permission Required",
      color = Color.White,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold
    )

    Text(
      text = "AVA Transport requires camera access to capture verified watermarked odometer checkpoint readings and receipts.",
      color = Slate400,
      fontSize = 12.sp,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center,
      lineHeight = 16.sp,
      modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
    )

    Button(
      onClick = onRequestPermission,
      colors = ButtonDefaults.buttonColors(
        containerColor = Sky500,
        contentColor = Color.White
      ),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("grant_camera_permission_button")
    ) {
      Text(text = "Grant Camera Permission", fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
      onClick = onSimulate,
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald400),
      border = ButtonDefaults.outlinedButtonBorder.copy(
        brush = androidx.compose.ui.graphics.SolidColor(Emerald500.copy(alpha = 0.4f))
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("simulate_dashboard_button")
    ) {
      Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(text = "Simulate Odometer Snapshot", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(12.dp))

    TextButton(onClick = onDismiss) {
      Text(text = "Cancel", color = Slate400)
    }
  }
}
