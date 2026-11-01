package com.example.data.repository

import android.content.Context
import com.example.data.db.BusEntity
import com.example.data.db.DriverEntity
import com.example.data.db.ExpenseEntity
import com.example.data.db.ExtraTripEntity
import com.example.data.db.FleetDao
import com.example.data.db.FleetDatabase
import com.example.data.db.QueueEntity
import com.example.data.db.TripEntity
import com.example.data.model.BusInfo
import com.example.data.model.Driver
import com.example.data.model.Expense
import com.example.data.model.ExtraTrip
import com.example.data.model.QueueItem
import com.example.data.model.StageData
import com.example.data.model.Trip
import com.example.util.JsonUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class FleetRepository(private val dao: FleetDao) {

  companion object {
    val DEFAULT_DRIVERS = listOf(
      Driver("DRV-1001", "Ahmed Al-Mansoori", "DXB-4821", "+971501234001", "1001", "active"),
      Driver("DRV-1002", "Mohammed Hassan", "SHJ-3190", "+971501234002", "1002", "active"),
      Driver("DRV-1003", "Fatima Al-Kaabi", "AUH-7742", "+971501234003", "1003", "active"),
      Driver("DRV-1004", "Rajesh Kumar", "DXB-5518", "+971501234004", "1004", "locked")
    )

    val DEFAULT_BUSES = listOf(
      "DXB-4821", "SHJ-3190", "AUH-7742", "DXB-5518", "DXB-2201", "AUH-8800"
    )

    const val ADMIN_PASSCODE = "admin123"
  }

  // --- SEEDING ---
  suspend fun ensureDefaultSeeds() = withContext(Dispatchers.IO) {
    val currentDrivers = dao.getAllDrivers().first()
    if (currentDrivers.isEmpty()) {
      dao.insertDrivers(DEFAULT_DRIVERS.map { it.toEntity() })
    }
    val currentBuses = dao.getAllBuses().first()
    if (currentBuses.isEmpty()) {
      dao.insertBuses(DEFAULT_BUSES.map { BusEntity(it) })
    }
  }

  // --- DRIVERS ---
  fun getDrivers(): Flow<List<Driver>> {
    return dao.getAllDrivers().map { list -> list.map { it.toDomain() } }
  }

  suspend fun getDriverById(id: String): Driver? = withContext(Dispatchers.IO) {
    dao.getDriverById(id)?.toDomain()
  }

  suspend fun saveDriver(driver: Driver) = withContext(Dispatchers.IO) {
    dao.insertDriver(driver.toEntity())
    dao.insertBus(BusEntity(driver.bus))
  }

  suspend fun toggleDriverStatus(driverId: String) = withContext(Dispatchers.IO) {
    val current = dao.getDriverById(driverId) ?: return@withContext
    val newStatus = if (current.status == "active") "locked" else "active"
    dao.updateDriver(current.copy(status = newStatus))
  }

  suspend fun updateDriverPin(driverId: String, newPin: String) = withContext(Dispatchers.IO) {
    val current = dao.getDriverById(driverId) ?: return@withContext
    dao.updateDriver(current.copy(pin = newPin))
  }

  suspend fun reassignDriverBus(driverId: String, newBus: String) = withContext(Dispatchers.IO) {
    val current = dao.getDriverById(driverId) ?: return@withContext
    dao.updateDriver(current.copy(bus = newBus))
    dao.insertBus(BusEntity(newBus))
  }

  suspend fun deleteDriver(id: String) = withContext(Dispatchers.IO) {
    dao.deleteDriver(id)
  }

  // --- TRIPS (SHIFTS) ---
  fun getTrips(): Flow<List<Trip>> {
    return dao.getAllTrips().map { list -> list.map { it.toDomain() } }
  }

  fun getTripsForDriver(driverId: String): Flow<List<Trip>> {
    return dao.getTripsForDriver(driverId).map { list -> list.map { it.toDomain() } }
  }

  suspend fun startTrip(
    driver: Driver,
    bus: String,
    busSource: String,
    startOdo: Double?
  ): Trip = withContext(Dispatchers.IO) {
    val trip = Trip(
      id = "TRIP-${System.currentTimeMillis()}-${(100..999).random()}",
      driverId = driver.id,
      driverName = driver.name,
      bus = bus,
      busSource = busSource,
      assignedBus = driver.bus,
      startOdometer = startOdo,
      status = "in_progress",
      createdAt = System.currentTimeMillis()
    )
    dao.insertTrip(trip.toEntity())
    dao.insertQueueItem(
      QueueEntity(
        id = UUID.randomUUID().toString(),
        type = "trip_start",
        title = "Shift Start - $bus",
        detail = if (startOdo != null) "Start ODO: ${startOdo.toInt()} km" else "Shift initiated",
        queuedAt = System.currentTimeMillis()
      )
    )
    trip
  }

  suspend fun updateTripStage(
    tripId: String,
    stageKey: String,
    stageTitle: String,
    odometer: Double,
    photoPath: String?,
    lat: Double?,
    lng: Double?
  ): Trip? = withContext(Dispatchers.IO) {
    val entity = dao.getTripById(tripId) ?: return@withContext null
    val currentStages = JsonUtil.deserializeStages(entity.stagesJson).toMutableList()
    
    // Replace or add stage
    val newStage = StageData(
      key = stageKey,
      title = stageTitle,
      odometer = odometer,
      photoPath = photoPath,
      latitude = lat,
      longitude = lng,
      timestamp = System.currentTimeMillis()
    )
    val existingIdx = currentStages.indexOfFirst { it.key == stageKey }
    if (existingIdx >= 0) {
      currentStages[existingIdx] = newStage
    } else {
      currentStages.add(newStage)
    }

    // Check if all 4 stages are completed
    val allFourDone = currentStages.size >= 4
    val updatedEntity = entity.copy(
      stagesJson = JsonUtil.serializeStages(currentStages),
      status = if (allFourDone) "completed" else entity.status,
      completedAt = if (allFourDone) System.currentTimeMillis() else entity.completedAt,
      endOdometer = if (stageKey == "final") odometer else entity.endOdometer
    )
    dao.updateTrip(updatedEntity)
    dao.insertQueueItem(
      QueueEntity(
        id = UUID.randomUUID().toString(),
        type = "trip_stage",
        title = "Checkpoint: $stageTitle",
        detail = "ODO: ${odometer.toInt()} km ($stageKey)",
        queuedAt = System.currentTimeMillis()
      )
    )
    updatedEntity.toDomain()
  }

  suspend fun finishTrip(
    tripId: String,
    endOdo: Double?
  ): Trip? = withContext(Dispatchers.IO) {
    val entity = dao.getTripById(tripId) ?: return@withContext null
    val currentStages = JsonUtil.deserializeStages(entity.stagesJson)
    val isClosedEarly = currentStages.size < 4
    val now = System.currentTimeMillis()

    val updatedEntity = entity.copy(
      endOdometer = endOdo ?: entity.endOdometer,
      status = "completed",
      completedAt = now,
      closedEarly = isClosedEarly,
      closedBy = if (endOdo != null) "end_odo" else "no_odo"
    )
    dao.updateTrip(updatedEntity)
    dao.insertQueueItem(
      QueueEntity(
        id = UUID.randomUUID().toString(),
        type = "trip_end",
        title = "Shift Completed - ${entity.bus}",
        detail = if (endOdo != null) "Final ODO: ${endOdo.toInt()} km" else "Closed shift",
        queuedAt = now
      )
    )
    updatedEntity.toDomain()
  }

  suspend fun deleteTrip(id: String) = withContext(Dispatchers.IO) {
    dao.deleteTrip(id)
  }

  // --- EXTRA TRIPS ---
  fun getExtraTrips(): Flow<List<ExtraTrip>> {
    return dao.getAllExtraTrips().map { list -> list.map { it.toDomain() } }
  }

  fun getExtraTripsForDriver(driverId: String): Flow<List<ExtraTrip>> {
    return dao.getExtraTripsForDriver(driverId).map { list -> list.map { it.toDomain() } }
  }

  suspend fun addExtraTrip(extra: ExtraTrip) = withContext(Dispatchers.IO) {
    dao.insertExtraTrip(extra.toEntity())
    dao.insertQueueItem(
      QueueEntity(
        id = UUID.randomUUID().toString(),
        type = "extra",
        title = "Extra Route - ${extra.bus}",
        detail = if (extra.distance != null) "${extra.distance.toInt()} km (${extra.note})" else extra.note,
        queuedAt = System.currentTimeMillis()
      )
    )
  }

  suspend fun deleteExtraTrip(id: String) = withContext(Dispatchers.IO) {
    dao.deleteExtraTrip(id)
  }

  // --- EXPENSES ---
  fun getExpenses(): Flow<List<Expense>> {
    return dao.getAllExpenses().map { list -> list.map { it.toDomain() } }
  }

  fun getExpensesForDriver(driverId: String): Flow<List<Expense>> {
    return dao.getExpensesForDriver(driverId).map { list -> list.map { it.toDomain() } }
  }

  suspend fun addExpense(expense: Expense) = withContext(Dispatchers.IO) {
    dao.insertExpense(expense.toEntity())
    dao.insertQueueItem(
      QueueEntity(
        id = UUID.randomUUID().toString(),
        type = "expense",
        title = "Expense: ${expense.type} (${expense.bus})",
        detail = "${String.format(java.util.Locale.US, "%.2f", expense.amount)} AED",
        queuedAt = System.currentTimeMillis()
      )
    )
  }

  suspend fun deleteExpense(id: String) = withContext(Dispatchers.IO) {
    dao.deleteExpense(id)
  }

  // --- BUSES & LAST ODOMETERS ---
  fun getBuses(): Flow<List<String>> {
    return dao.getAllBuses().map { list -> list.map { it.plateNumber } }
  }

  fun getBusInfoList(): Flow<List<BusInfo>> {
    return combine(dao.getAllBuses(), dao.getAllTrips(), dao.getAllDrivers()) { buses, trips, drivers ->
      buses.map { busEntity ->
        val plate = busEntity.plateNumber
        // Find highest odometer across all trips for this bus
        var highestOdo: Double? = null
        trips.filter { it.bus.equals(plate, ignoreCase = true) }.forEach { tripEntity ->
          tripEntity.startOdometer?.let {
            if (highestOdo == null || it > highestOdo!!) highestOdo = it
          }
          tripEntity.endOdometer?.let {
            if (highestOdo == null || it > highestOdo!!) highestOdo = it
          }
          JsonUtil.deserializeStages(tripEntity.stagesJson).forEach { stage ->
            stage.odometer?.let {
              if (highestOdo == null || it > highestOdo!!) highestOdo = it
            }
          }
        }
        val driverCount = drivers.count { it.bus.equals(plate, ignoreCase = true) }
        BusInfo(
          plateNumber = plate,
          lastOdometer = highestOdo,
          assignedDriverCount = driverCount
        )
      }
    }
  }

  suspend fun addBus(plateNumber: String) = withContext(Dispatchers.IO) {
    dao.insertBus(BusEntity(plateNumber.trim().uppercase()))
  }

  suspend fun deleteBus(plateNumber: String) = withContext(Dispatchers.IO) {
    dao.deleteBus(plateNumber)
  }

  suspend fun getLastOdometerForBus(bus: String): Double? = withContext(Dispatchers.IO) {
    val trips = dao.getAllTrips().first()
    var highest: Double? = null
    trips.filter { it.bus.equals(bus, ignoreCase = true) }.forEach { tripEntity ->
      tripEntity.startOdometer?.let { if (highest == null || it > highest!!) highest = it }
      tripEntity.endOdometer?.let { if (highest == null || it > highest!!) highest = it }
      JsonUtil.deserializeStages(tripEntity.stagesJson).forEach { stage ->
        stage.odometer?.let { if (highest == null || it > highest!!) highest = it }
      }
    }
    highest
  }

  // --- QUEUE & SYNC ---
  fun getQueue(): Flow<List<QueueItem>> {
    return dao.getAllQueue().map { list -> list.map { it.toDomain() } }
  }

  suspend fun clearQueue() = withContext(Dispatchers.IO) {
    dao.clearAllQueue()
  }

  // --- TESTER DATA CLEANER (PRIMARY USER REQUIREMENT) ---
  suspend fun getTesterDataCounts(): Map<String, Int> = withContext(Dispatchers.IO) {
    mapOf(
      "trips" to dao.getTripsCount(),
      "extras" to dao.getExtraTripsCount(),
      "expenses" to dao.getExpensesCount(),
      "queue" to dao.getQueueCount()
    )
  }

  suspend fun clearTesterData(
    clearShifts: Boolean = true,
    clearExtras: Boolean = true,
    clearExpenses: Boolean = true,
    clearQueue: Boolean = true
  ): Int = withContext(Dispatchers.IO) {
    val countBefore = dao.getTripsCount() + dao.getExtraTripsCount() + dao.getExpensesCount() + dao.getQueueCount()
    dao.purgeAllTesterData(
      clearShifts = clearShifts,
      clearExtras = clearExtras,
      clearExpenses = clearExpenses,
      clearQueue = clearQueue
    )
    val countAfter = dao.getTripsCount() + dao.getExtraTripsCount() + dao.getExpensesCount() + dao.getQueueCount()
    countBefore - countAfter
  }

  suspend fun resetToCleanFactoryState() = withContext(Dispatchers.IO) {
    dao.clearAllTrips()
    dao.clearAllExtraTrips()
    dao.clearAllExpenses()
    dao.clearAllQueue()
    dao.deleteAllDrivers()
    dao.deleteAllBuses()
    dao.insertDrivers(DEFAULT_DRIVERS.map { it.toEntity() })
    dao.insertBuses(DEFAULT_BUSES.map { BusEntity(it) })
  }

  suspend fun seedSampleTestData() = withContext(Dispatchers.IO) {
    // Add sample trips
    val now = System.currentTimeMillis()
    val day = 86_400_000L

    val sampleTrips = listOf(
      TripEntity(
        id = "TRIP-SAMPLE-01",
        driverId = "DRV-1001",
        driverName = "Ahmed Al-Mansoori",
        bus = "DXB-4821",
        busSource = "auto",
        assignedBus = "DXB-4821",
        startOdometer = 124500.0,
        endOdometer = 124618.0,
        status = "completed",
        createdAt = now - (day * 2),
        completedAt = now - (day * 2) + 28800000,
        closedEarly = false,
        closedBy = "end_odo",
        stagesJson = JsonUtil.serializeStages(
          listOf(
            StageData("start", "Before Trip Start", 124500.0, null, 25.2048, 55.2708, now - (day * 2)),
            StageData("dest", "Reached Destination", 124558.0, null, 25.1972, 55.2744, now - (day * 2) + 7200000),
            StageData("returnStart", "Before Return Start", 124560.0, null, 25.1972, 55.2744, now - (day * 2) + 21600000),
            StageData("final", "Final Return Parking", 124618.0, null, 25.2048, 55.2708, now - (day * 2) + 28800000)
          )
        )
      ),
      TripEntity(
        id = "TRIP-SAMPLE-02",
        driverId = "DRV-1002",
        driverName = "Mohammed Hassan",
        bus = "SHJ-3190",
        busSource = "auto",
        assignedBus = "SHJ-3190",
        startOdometer = 89340.0,
        endOdometer = 89415.0,
        status = "completed",
        createdAt = now - day,
        completedAt = now - day + 25200000,
        closedEarly = false,
        closedBy = "end_odo",
        stagesJson = JsonUtil.serializeStages(
          listOf(
            StageData("start", "Before Trip Start", 89340.0, null, 25.3463, 55.4209, now - day),
            StageData("dest", "Reached Destination", 89377.0, null, 25.2048, 55.2708, now - day + 7200000),
            StageData("returnStart", "Before Return Start", 89377.0, null, 25.2048, 55.2708, now - day + 18000000),
            StageData("final", "Final Return Parking", 89415.0, null, 25.3463, 55.4209, now - day + 25200000)
          )
        )
      )
    )
    dao.insertTrips(sampleTrips)

    val sampleExtras = listOf(
      ExtraTripEntity(
        id = "EXT-SAMPLE-01",
        driverId = "DRV-1001",
        driverName = "Ahmed Al-Mansoori",
        bus = "DXB-4821",
        startKm = 124620.0,
        endKm = 124665.0,
        distance = 45.0,
        note = "Airport Terminal 3 Passenger Shuttle Transfer",
        time = "14:30",
        createdAt = now - 43200000
      )
    )
    dao.insertExtraTrips(sampleExtras)

    val sampleExpenses = listOf(
      ExpenseEntity(
        id = "EXP-SAMPLE-01",
        driverId = "DRV-1001",
        driverName = "Ahmed Al-Mansoori",
        bus = "DXB-4821",
        type = "Fuel",
        mode = "calc",
        liters = 65.0,
        unitPrice = 2.85,
        amount = 185.25,
        note = "ADNOC Station Al Quoz - Diesel refill",
        receiptPath = null,
        createdAt = now - (day * 2) + 3600000
      ),
      ExpenseEntity(
        id = "EXP-SAMPLE-02",
        driverId = "DRV-1002",
        driverName = "Mohammed Hassan",
        bus = "SHJ-3190",
        type = "Wash",
        mode = "direct",
        liters = null,
        unitPrice = null,
        amount = 55.00,
        note = "Full exterior bus wash & sanitization",
        receiptPath = null,
        createdAt = now - day + 10800000
      )
    )
    dao.insertExpenses(sampleExpenses)
  }

  // --- MAPPERS ---
  private fun DriverEntity.toDomain() = Driver(id, name, bus, phone, pin, status)
  private fun Driver.toEntity() = DriverEntity(id, name, bus, phone, pin, status)

  private fun TripEntity.toDomain() = Trip(
    id = id,
    driverId = driverId,
    driverName = driverName,
    bus = bus,
    busSource = busSource,
    assignedBus = assignedBus,
    startOdometer = startOdometer,
    endOdometer = endOdometer,
    status = status,
    createdAt = createdAt,
    completedAt = completedAt,
    closedEarly = closedEarly,
    closedBy = closedBy,
    stages = JsonUtil.deserializeStages(stagesJson)
  )

  private fun Trip.toEntity() = TripEntity(
    id = id,
    driverId = driverId,
    driverName = driverName,
    bus = bus,
    busSource = busSource,
    assignedBus = assignedBus,
    startOdometer = startOdometer,
    endOdometer = endOdometer,
    status = status,
    createdAt = createdAt,
    completedAt = completedAt,
    closedEarly = closedEarly,
    closedBy = closedBy,
    stagesJson = JsonUtil.serializeStages(stages)
  )

  private fun ExtraTripEntity.toDomain() = ExtraTrip(id, driverId, driverName, bus, startKm, endKm, distance, note, time, createdAt)
  private fun ExtraTrip.toEntity() = ExtraTripEntity(id, driverId, driverName, bus, startKm, endKm, distance, note, time, createdAt)

  private fun ExpenseEntity.toDomain() = Expense(id, driverId, driverName, bus, type, mode, liters, unitPrice, amount, note, receiptPath, createdAt)
  private fun Expense.toEntity() = ExpenseEntity(id, driverId, driverName, bus, type, mode, liters, unitPrice, amount, note, receiptPath, createdAt)

  private fun QueueEntity.toDomain() = QueueItem(id, type, title, detail, queuedAt)
}
