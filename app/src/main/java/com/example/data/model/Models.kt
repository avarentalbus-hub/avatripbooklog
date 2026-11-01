package com.example.data.model

data class Driver(
  val id: String,
  val name: String,
  val bus: String,
  val phone: String,
  val pin: String,
  val status: String // "active" or "locked"
)

data class StageData(
  val key: String,
  val title: String,
  val odometer: Double?,
  val photoPath: String? = null,
  val latitude: Double? = null,
  val longitude: Double? = null,
  val timestamp: Long = System.currentTimeMillis()
)

data class Trip(
  val id: String,
  val driverId: String,
  val driverName: String,
  val bus: String,
  val busSource: String = "auto", // "auto" or "manual"
  val assignedBus: String? = null,
  val startOdometer: Double? = null,
  val endOdometer: Double? = null,
  val status: String = "in_progress", // "in_progress" or "completed"
  val createdAt: Long = System.currentTimeMillis(),
  val completedAt: Long? = null,
  val closedEarly: Boolean = false,
  val closedBy: String? = null,
  val stages: List<StageData> = emptyList()
) {
  val distance: Double?
    get() {
      val first = startOdometer ?: stages.firstOrNull { it.odometer != null }?.odometer
      val last = endOdometer ?: stages.lastOrNull { it.odometer != null }?.odometer
      return if (first != null && last != null && last >= first) {
        last - first
      } else null
    }
}

data class ExtraTrip(
  val id: String,
  val driverId: String,
  val driverName: String,
  val bus: String,
  val startKm: Double? = null,
  val endKm: Double? = null,
  val distance: Double? = null,
  val note: String = "",
  val time: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

data class Expense(
  val id: String,
  val driverId: String,
  val driverName: String,
  val bus: String,
  val type: String = "Fuel", // Fuel, Wash, Parking, Maintenance, Other
  val mode: String = "calc", // calc, direct
  val liters: Double? = null,
  val unitPrice: Double? = null,
  val amount: Double = 0.0,
  val note: String = "",
  val receiptPath: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)

data class QueueItem(
  val id: String,
  val type: String, // trip_start, trip_stage, trip_end, extra, expense
  val title: String,
  val detail: String,
  val queuedAt: Long = System.currentTimeMillis()
)

data class BusInfo(
  val plateNumber: String,
  val lastOdometer: Double? = null,
  val assignedDriverCount: Int = 0
)
