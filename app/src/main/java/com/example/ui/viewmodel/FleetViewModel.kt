package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.FleetDatabase
import com.example.data.model.BusInfo
import com.example.data.model.Driver
import com.example.data.model.Expense
import com.example.data.model.ExtraTrip
import com.example.data.model.QueueItem
import com.example.data.model.Trip
import com.example.data.repository.FleetRepository
import com.example.util.WatermarkUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FleetViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: FleetRepository

  init {
    val database = FleetDatabase.getDatabase(application)
    repository = FleetRepository(database.fleetDao())
    viewModelScope.launch {
      val prefs = application.getSharedPreferences("fleetshift_prefs", android.content.Context.MODE_PRIVATE)
      val isCleanInitialized = prefs.getBoolean("fresh_app_clean_reset_done", false)
      if (!isCleanInitialized) {
        repository.resetToCleanFactoryState()
        prefs.edit().putBoolean("fresh_app_clean_reset_done", true).apply()
      } else {
        repository.ensureDefaultSeeds()
      }
    }
  }

  // Session & Navigation State
  private val _activeRole = MutableStateFlow<String?>(null) // null = Role Selector, "driver", "admin"
  val activeRole: StateFlow<String?> = _activeRole.asStateFlow()

  private val _activeDriver = MutableStateFlow<Driver?>(null)
  val activeDriver: StateFlow<Driver?> = _activeDriver.asStateFlow()

  private val _isOnline = MutableStateFlow(true)
  val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  private val _showClearTesterDataDialog = MutableStateFlow(false)
  val showClearTesterDataDialog: StateFlow<Boolean> = _showClearTesterDataDialog.asStateFlow()

  private val _selectedDriverTab = MutableStateFlow("shift")
  val selectedDriverTab: StateFlow<String> = _selectedDriverTab.asStateFlow()

  private val _selectedAdminTab = MutableStateFlow("overview")
  val selectedAdminTab: StateFlow<String> = _selectedAdminTab.asStateFlow()

  // Room DB Data Streams
  val drivers: StateFlow<List<Driver>> = repository.getDrivers()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val trips: StateFlow<List<Trip>> = repository.getTrips()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val extraTrips: StateFlow<List<ExtraTrip>> = repository.getExtraTrips()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val expenses: StateFlow<List<Expense>> = repository.getExpenses()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val buses: StateFlow<List<BusInfo>> = repository.getBusInfoList()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val syncQueue: StateFlow<List<QueueItem>> = repository.getQueue()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Current Active Trip for Logged-In Driver
  val activeTrip: StateFlow<Trip?> = combine(_activeDriver, trips) { driver, allTrips ->
    if (driver == null) null
    else allTrips.firstOrNull { it.driverId == driver.id && it.status == "in_progress" }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  fun showToast(msg: String) {
    _toastMessage.value = msg
  }

  fun dismissToast() {
    _toastMessage.value = null
  }

  fun setRole(role: String?) {
    _activeRole.value = role
  }

  fun setDriverTab(tab: String) {
    _selectedDriverTab.value = tab
  }

  fun setAdminTab(tab: String) {
    _selectedAdminTab.value = tab
  }

  fun toggleOnlineMode() {
    _isOnline.value = !_isOnline.value
    showToast(if (_isOnline.value) "Switched to Online Sync mode" else "Switched to Offline Mode (caching on device)")
  }

  fun openClearTesterDataDialog() {
    _showClearTesterDataDialog.value = true
  }

  fun closeClearTesterDataDialog() {
    _showClearTesterDataDialog.value = false
  }

  // --- AUTHENTICATION ---
  fun loginDriver(driver: Driver, pin: String): Boolean {
    if (driver.status != "active") {
      showToast("This driver profile is currently locked by dispatch.")
      return false
    }
    if (driver.pin.trim() != pin.trim()) {
      showToast("Incorrect PIN. Please try again.")
      return false
    }
    _activeDriver.value = driver
    _activeRole.value = "driver"
    _selectedDriverTab.value = "shift"
    showToast("Welcome, ${driver.name}!")
    return true
  }

  fun loginAdmin(passcode: String): Boolean {
    if (passcode.trim() == FleetRepository.ADMIN_PASSCODE) {
      _activeRole.value = "admin"
      _selectedAdminTab.value = "overview"
      showToast("Admin Control Center Unlocked")
      return true
    } else {
      showToast("Incorrect master passcode.")
      return false
    }
  }

  fun logout() {
    _activeRole.value = null
    _activeDriver.value = null
    _selectedDriverTab.value = "shift"
  }

  // --- SHIFTS & STAGES ---
  fun startShift(bus: String, busSource: String, startOdo: Double?) {
    val driver = _activeDriver.value ?: return
    viewModelScope.launch {
      val lastOdo = repository.getLastOdometerForBus(bus)
      if (startOdo != null && lastOdo != null && startOdo < lastOdo) {
        showToast("Error: Start ODO cannot be lower than last reading (${lastOdo.toInt()} km)")
        return@launch
      }
      repository.startTrip(driver, bus, busSource, startOdo)
      showToast("Shift started on bus $bus")
    }
  }

  fun recordStageWithWatermark(
    stageKey: String,
    stageTitle: String,
    odometer: Double,
    capturedBitmap: Bitmap?,
    latitude: Double?,
    longitude: Double?
  ) {
    val driver = _activeDriver.value ?: return
    val trip = activeTrip.value ?: return

    viewModelScope.launch {
      var photoPath: String? = null
      val context = getApplication<Application>()

      if (capturedBitmap != null) {
        photoPath = WatermarkUtil.applyWatermarkAndSave(
          context = context,
          sourceBitmap = capturedBitmap,
          driverName = driver.name,
          driverId = driver.id,
          bus = trip.bus,
          latitude = latitude,
          longitude = longitude,
          stageTitle = stageTitle
        )
      } else {
        // Generate dashboard proof with digital odometer & watermark
        val simBitmap = WatermarkUtil.createSimulatedOdometerBitmap(trip.bus, odometer)
        photoPath = WatermarkUtil.applyWatermarkAndSave(
          context = context,
          sourceBitmap = simBitmap,
          driverName = driver.name,
          driverId = driver.id,
          bus = trip.bus,
          latitude = latitude,
          longitude = longitude,
          stageTitle = stageTitle
        )
      }

      repository.updateTripStage(
        tripId = trip.id,
        stageKey = stageKey,
        stageTitle = stageTitle,
        odometer = odometer,
        photoPath = photoPath,
        lat = latitude,
        lng = longitude
      )
      showToast("Checkpoint '$stageTitle' recorded & watermarked")
    }
  }

  fun finishShift(endOdo: Double?) {
    val trip = activeTrip.value ?: return
    viewModelScope.launch {
      if (endOdo != null && trip.startOdometer != null && endOdo < trip.startOdometer) {
        showToast("Error: End ODO cannot be lower than start ODO (${trip.startOdometer.toInt()} km)")
        return@launch
      }
      repository.finishTrip(trip.id, endOdo)
      showToast("Shift completed and sealed successfully!")
    }
  }

  // --- EXTRAS ---
  fun logExtraTrip(startKm: Double?, endKm: Double?, note: String, time: String) {
    val driver = _activeDriver.value ?: return
    val bus = activeTrip.value?.bus ?: driver.bus
    if (startKm != null && endKm != null && endKm < startKm) {
      showToast("End km cannot be lower than start km")
      return
    }
    val distance = if (startKm != null && endKm != null) endKm - startKm else null
    viewModelScope.launch {
      repository.addExtraTrip(
        ExtraTrip(
          id = "EXT-${System.currentTimeMillis()}-${(100..999).random()}",
          driverId = driver.id,
          driverName = driver.name,
          bus = bus,
          startKm = startKm,
          endKm = endKm,
          distance = distance,
          note = note,
          time = time
        )
      )
      showToast(if (distance != null) "Extra trip logged: ${distance.toInt()} km" else "Extra trip logged")
    }
  }

  // --- EXPENSES ---
  fun logExpense(
    type: String,
    mode: String,
    liters: Double?,
    unitPrice: Double?,
    amount: Double,
    note: String,
    receiptBitmap: Bitmap?
  ) {
    val driver = _activeDriver.value ?: return
    val bus = activeTrip.value?.bus ?: driver.bus
    viewModelScope.launch {
      var receiptPath: String? = null
      if (receiptBitmap != null) {
        val context = getApplication<Application>()
        receiptPath = WatermarkUtil.applyWatermarkAndSave(
          context = context,
          sourceBitmap = receiptBitmap,
          driverName = driver.name,
          driverId = driver.id,
          bus = bus,
          stageTitle = "RECEIPT: $type"
        )
      }
      repository.addExpense(
        Expense(
          id = "EXP-${System.currentTimeMillis()}-${(100..999).random()}",
          driverId = driver.id,
          driverName = driver.name,
          bus = bus,
          type = type,
          mode = mode,
          liters = liters,
          unitPrice = unitPrice,
          amount = amount,
          note = note,
          receiptPath = receiptPath
        )
      )
      showToast("Expense of ${String.format(java.util.Locale.US, "%.2f", amount)} AED recorded")
    }
  }

  // --- ADMIN OPERATIONS ---
  fun toggleDriverStatus(driverId: String) {
    viewModelScope.launch {
      repository.toggleDriverStatus(driverId)
      showToast("Driver status updated")
    }
  }

  fun updateDriverPin(driverId: String, newPin: String) {
    viewModelScope.launch {
      repository.updateDriverPin(driverId, newPin)
      showToast("Driver PIN updated")
    }
  }

  fun randomizeDriverPin(driverId: String) {
    val randomPin = (1000..9999).random().toString()
    viewModelScope.launch {
      repository.updateDriverPin(driverId, randomPin)
      showToast("New PIN generated: $randomPin")
    }
  }

  fun reassignBus(driverId: String, newBus: String) {
    viewModelScope.launch {
      repository.reassignDriverBus(driverId, newBus)
      showToast("Vehicle reassigned to $newBus")
    }
  }

  fun addDriver(name: String, id: String, bus: String, phone: String, pin: String) {
    viewModelScope.launch {
      repository.saveDriver(
        Driver(
          id = id.trim().uppercase(),
          name = name.trim(),
          bus = bus.trim().uppercase(),
          phone = phone.trim(),
          pin = pin.trim(),
          status = "active"
        )
      )
      showToast("Driver $name registered to fleet")
    }
  }

  fun addBus(plateNumber: String) {
    viewModelScope.launch {
      repository.addBus(plateNumber)
      showToast("Bus $plateNumber added to registry")
    }
  }

  // --- TESTER DATA CLEANER (PRIMARY USER REQUIREMENT) ---
  fun clearAllTesterData(
    clearShifts: Boolean = true,
    clearExtras: Boolean = true,
    clearExpenses: Boolean = true,
    clearQueue: Boolean = true
  ) {
    viewModelScope.launch {
      val deletedCount = repository.clearTesterData(
        clearShifts = clearShifts,
        clearExtras = clearExtras,
        clearExpenses = clearExpenses,
        clearQueue = clearQueue
      )
      _showClearTesterDataDialog.value = false
      showToast("Cleaned $deletedCount tester log records! System is fresh.")
    }
  }

  fun seedSampleData() {
    viewModelScope.launch {
      repository.seedSampleTestData()
      showToast("Sample testing shifts, extras, and fuel slips added.")
    }
  }

  fun resetToCleanFactoryState() {
    viewModelScope.launch {
      repository.resetToCleanFactoryState()
      _showClearTesterDataDialog.value = false
      showToast("Fleet system completely reset to clean factory state.")
    }
  }

  fun syncAllQueue() {
    viewModelScope.launch {
      repository.clearQueue()
      showToast("All pending logs synced with cloud dispatch.")
    }
  }
}
