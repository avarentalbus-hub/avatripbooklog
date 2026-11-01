package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WatermarkUtil {

  fun decodeAndRotateBitmap(filePath: String): Bitmap {
    val bitmap = android.graphics.BitmapFactory.decodeFile(filePath)
      ?: throw IllegalArgumentException("Could not decode image at $filePath")
    try {
      val exif = android.media.ExifInterface(filePath)
      val orientation = exif.getAttributeInt(
        android.media.ExifInterface.TAG_ORIENTATION,
        android.media.ExifInterface.ORIENTATION_NORMAL
      )
      val matrix = android.graphics.Matrix()
      when (orientation) {
        android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        else -> return bitmap
      }
      return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } catch (e: Exception) {
      return bitmap
    }
  }

  fun applyWatermarkAndSave(
    context: Context,
    sourceBitmap: Bitmap,
    driverName: String,
    driverId: String,
    bus: String,
    latitude: Double? = null,
    longitude: Double? = null,
    stageTitle: String? = null
  ): String {
    val width = sourceBitmap.width
    val height = sourceBitmap.height

    // Create mutable copy
    val outputBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(outputBitmap)

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
    val timeStr = dateFormat.format(Date())
    val gpsStr = if (latitude != null && longitude != null) {
      String.format(Locale.US, "GPS: %.5f, %.5f", latitude, longitude)
    } else {
      "GPS: 25.20485, 55.27078 (Dubai HQ)"
    }

    val lines = mutableListOf<String>()
    if (!stageTitle.isNullOrBlank()) {
      lines.add("STAGE: $stageTitle")
    }
    lines.add("TIME: $timeStr")
    lines.add("DRIVER: $driverName ($driverId)")
    lines.add("BUS: $bus")
    lines.add(gpsStr)

    // Calculate dimensions based on image size
    val baseScale = (width.coerceAtMost(height) / 800f).coerceIn(1.0f, 3.0f)
    val textSize = 18f * baseScale
    val lineSpacing = 24f * baseScale
    val padding = 16f * baseScale
    val boxHeight = (lines.size * lineSpacing) + (padding * 2)
    val boxWidth = (width - (32f * baseScale)).coerceAtMost(600f * baseScale)
    val boxLeft = 16f * baseScale
    val boxTop = height - boxHeight - (16f * baseScale)

    // Draw background translucent pill
    val bgPaint = Paint().apply {
      color = Color.parseColor("#D90A0F1A") // ~85% slate 950
      style = Paint.Style.FILL
      isAntiAlias = true
    }
    val rect = RectF(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight)
    canvas.drawRoundRect(rect, 16f * baseScale, 16f * baseScale, bgPaint)

    // Left accent bar
    val accentPaint = Paint().apply {
      color = Color.parseColor("#0EA5E9") // Sky blue
      style = Paint.Style.FILL
      isAntiAlias = true
    }
    val accentRect = RectF(boxLeft, boxTop, boxLeft + (6f * baseScale), boxTop + boxHeight)
    canvas.drawRoundRect(accentRect, 4f * baseScale, 4f * baseScale, accentPaint)

    // Text paint
    val textPaint = Paint().apply {
      color = Color.WHITE
      this.textSize = textSize
      typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
      isAntiAlias = true
    }

    // Draw lines
    lines.forEachIndexed { index, line ->
      val y = boxTop + padding + ((index + 1) * lineSpacing) - (4f * baseScale)
      canvas.drawText(line, boxLeft + padding + (10f * baseScale), y, textPaint)
    }

    // Save to cache file
    val fileName = "watermark_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg"
    val file = File(context.cacheDir, fileName)
    FileOutputStream(file).use { out ->
      outputBitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
    }

    return file.absolutePath
  }

  fun createSimulatedOdometerBitmap(
    bus: String,
    odometerKm: Double
  ): Bitmap {
    val width = 1200
    val height = 800
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Gradient background
    val bgPaint = Paint().apply {
      color = Color.parseColor("#0F172A")
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Center circular gauge dial
    val dialPaint = Paint().apply {
      color = Color.parseColor("#1E293B")
      style = Paint.Style.STROKE
      strokeWidth = 24f
      isAntiAlias = true
    }
    canvas.drawCircle(600f, 380f, 220f, dialPaint)

    val innerDialPaint = Paint().apply {
      color = Color.parseColor("#0EA5E9")
      style = Paint.Style.STROKE
      strokeWidth = 8f
      isAntiAlias = true
    }
    canvas.drawCircle(600f, 380f, 190f, innerDialPaint)

    // Title
    val titlePaint = Paint().apply {
      color = Color.parseColor("#38BDF8")
      textSize = 34f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
      isAntiAlias = true
    }
    canvas.drawText("AVA TRANSPORT FLEET DASHBOARD", 600f, 260f, titlePaint)

    // Bus Plate
    val busPaint = Paint().apply {
      color = Color.parseColor("#94A3B8")
      textSize = 28f
      typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
      textAlign = Paint.Align.CENTER
      isAntiAlias = true
    }
    canvas.drawText("VEHICLE: $bus", 600f, 320f, busPaint)

    // ODOMETER DISPLAY
    val odoLabelPaint = Paint().apply {
      color = Color.parseColor("#10B981")
      textSize = 22f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
      isAntiAlias = true
    }
    canvas.drawText("VERIFIED ODOMETER READING", 600f, 400f, odoLabelPaint)

    val odoValuePaint = Paint().apply {
      color = Color.WHITE
      textSize = 62f
      typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
      isAntiAlias = true
    }
    canvas.drawText("${String.format(Locale.US, "%,.1f", odometerKm)} km", 600f, 480f, odoValuePaint)

    return bitmap
  }
}
