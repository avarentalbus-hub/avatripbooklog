package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

data class GpsCoordinates(
  val latitude: Double,
  val longitude: Double,
  val accuracyMeters: Float? = null,
  val altitudeMeters: Double? = null,
  val speedKmh: Float? = null,
  val timestamp: Long = System.currentTimeMillis(),
  val isSimulated: Boolean = false
) {
  fun formatWatermark(): String {
    val latDir = if (latitude >= 0) "N" else "S"
    val lngDir = if (longitude >= 0) "E" else "W"
    val absLat = kotlin.math.abs(latitude)
    val absLng = kotlin.math.abs(longitude)
    val accStr = if (accuracyMeters != null && accuracyMeters > 0) " (±${accuracyMeters.toInt()}m)" else ""
    return String.format(Locale.US, "GPS: %.5f° %s, %.5f° %s%s", absLat, latDir, absLng, lngDir, accStr)
  }

  fun toPair(): Pair<Double, Double> = Pair(latitude, longitude)

  companion object {
    val DUBAI_HQ = GpsCoordinates(
      latitude = 25.20485,
      longitude = 55.27078,
      accuracyMeters = 4.0f,
      isSimulated = true
    )
  }
}

object LocationHelper {

  private const val TAG = "LocationHelper"

  fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
  }

  fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
      locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
  }

  @SuppressLint("MissingPermission")
  suspend fun getCurrentLocation(
    context: Context,
    timeoutMs: Long = 4000L
  ): GpsCoordinates {
    if (!hasLocationPermission(context)) {
      Log.d(TAG, "Location permission not granted, using Dubai HQ fallback")
      return GpsCoordinates.DUBAI_HQ
    }

    // Try FusedLocationProviderClient first
    val freshLocation = withTimeoutOrNull(timeoutMs) {
      fetchFusedLocation(context)
    }

    if (freshLocation != null) {
      return freshLocation
    }

    // Fallback to legacy LocationManager
    val legacyLocation = fetchLegacyLocation(context)
    if (legacyLocation != null) {
      return legacyLocation
    }

    return GpsCoordinates.DUBAI_HQ
  }

  @SuppressLint("MissingPermission")
  private suspend fun fetchFusedLocation(context: Context): GpsCoordinates? {
    return try {
      val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
      val cts = CancellationTokenSource()

      suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
          cts.cancel()
        }

        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
          .addOnSuccessListener { loc: Location? ->
            if (loc != null) {
              continuation.resume(mapLocationToGps(loc))
            } else {
              // Try last known location
              fusedClient.lastLocation
                .addOnSuccessListener { lastLoc: Location? ->
                  continuation.resume(lastLoc?.let { mapLocationToGps(it) })
                }
                .addOnFailureListener {
                  continuation.resume(null)
                }
            }
          }
          .addOnFailureListener { exc ->
            Log.w(TAG, "Fused location failure: ${exc.message}")
            continuation.resume(null)
          }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error obtaining fused location", e)
      null
    }
  }

  @SuppressLint("MissingPermission")
  private fun fetchLegacyLocation(context: Context): GpsCoordinates? {
    return try {
      val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
      val gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
      val netLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
      val best = when {
        gpsLoc != null && netLoc != null -> if (gpsLoc.time >= netLoc.time) gpsLoc else netLoc
        gpsLoc != null -> gpsLoc
        netLoc != null -> netLoc
        else -> null
      }
      best?.let { mapLocationToGps(it) }
    } catch (e: Exception) {
      Log.w(TAG, "Legacy location lookup failed", e)
      null
    }
  }

  @SuppressLint("MissingPermission")
  fun getLocationUpdates(
    context: Context,
    intervalMs: Long = 3000L
  ): Flow<GpsCoordinates> = callbackFlow {
    if (!hasLocationPermission(context)) {
      trySend(GpsCoordinates.DUBAI_HQ)
      channel.close()
      return@callbackFlow
    }

    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    // Emit cached initial position immediately
    fetchLegacyLocation(context)?.let { trySend(it) } ?: trySend(GpsCoordinates.DUBAI_HQ)

    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
      .setMinUpdateIntervalMillis(intervalMs / 2)
      .setWaitForAccurateLocation(false)
      .build()

    val callback = object : LocationCallback() {
      override fun onLocationResult(result: LocationResult) {
        val lastLocation = result.lastLocation ?: return
        trySend(mapLocationToGps(lastLocation))
      }
    }

    try {
      fusedClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start location updates", e)
      trySend(GpsCoordinates.DUBAI_HQ)
    }

    awaitClose {
      try {
        fusedClient.removeLocationUpdates(callback)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to remove location updates", e)
      }
    }
  }

  private fun mapLocationToGps(loc: Location): GpsCoordinates {
    return GpsCoordinates(
      latitude = loc.latitude,
      longitude = loc.longitude,
      accuracyMeters = if (loc.hasAccuracy()) loc.accuracy else null,
      altitudeMeters = if (loc.hasAltitude()) loc.altitude else null,
      speedKmh = if (loc.hasSpeed()) loc.speed * 3.6f else null,
      timestamp = loc.time.takeIf { it > 0 } ?: System.currentTimeMillis(),
      isSimulated = false
    )
  }
}
