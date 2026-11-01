package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusInfo
import com.example.data.model.Driver
import com.example.data.model.Expense
import com.example.data.model.ExtraTrip
import com.example.data.model.QueueItem
import com.example.data.model.StageData
import com.example.data.model.Trip
import com.example.ui.components.NetworkBadge
import com.example.ui.components.ReceiptViewerDialog
import com.example.ui.components.SyncStatusBanner
import com.example.ui.components.WatermarkCameraDialog
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Sky400
import com.example.ui.theme.Sky500
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.util.Formatters

data class StageDefinition(
  val key: String,
  val title: String,
  val subtitle: String,
  val icon: ImageVector
)

val SHIFT_STAGES = listOf(
  StageDefinition("start", "Before Trip Start", "Record starting odometer & watermark photo", Icons.Default.PlayArrow),
  StageDefinition("dest", "Reached Destination", "Arrival / destination parking reading", Icons.Default.Map),
  StageDefinition("returnStart", "Before Return Start", "Odometer before return leg", Icons.Default.RotateLeft),
  StageDefinition("final", "Final Return Parking", "Complete shift & final parking odometer", Icons.Default.Flag)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverPortalScreen(
  driver: Driver,
  isOnline: Boolean,
  queueItems: List<QueueItem>,
  activeTrip: Trip?,
  allTrips: List<Trip>,
  extraTrips: List<ExtraTrip>,
  expenses: List<Expense>,
  buses: List<BusInfo>,
  selectedTab: String,
  onTabSelected: (String) -> Unit,
  onToggleOnline: () -> Unit,
  onSyncNow: () -> Unit,
  onStartShift: (bus: String, busSource: String, startOdo: Double?) -> Unit,
  onRecordStage: (stageKey: String, stageTitle: String, odo: Double, bitmap: Bitmap?, lat: Double?, lng: Double?) -> Unit,
  onFinishShift: (endOdo: Double?) -> Unit,
  onLogExtraTrip: (startKm: Double?, endKm: Double?, note: String, time: String) -> Unit,
  onLogExpense: (type: String, mode: String, liters: Double?, unitPrice: Double?, amount: Double, note: String, receiptBitmap: Bitmap?) -> Unit,
  onLogout: () -> Unit
) {
  // Dialog state
  var stageToCapture by remember { mutableStateOf<Pair<StageDefinition, Double>?>(null) }
  var viewReceiptPath by remember { mutableStateOf<Pair<String, String>?>(null) } // title, path

  Scaffold(
    containerColor = Slate950,
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(Slate900)
          .border(1.dp, Slate800)
      ) {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "AVA APP • Driver Portal",
              color = Slate400,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = driver.name,
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "${activeTrip?.bus ?: driver.bus} • ${driver.id}",
                color = Sky400,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
              )
              if (activeTrip != null && activeTrip.busSource == "manual") {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "MANUAL BUS",
                  color = Amber400,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier
                    .background(Amber500.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            NetworkBadge(
              isOnline = isOnline,
              queueCount = queueItems.size,
              onToggle = onToggleOnline
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
              onClick = onLogout,
              modifier = Modifier
                .size(36.dp)
                .background(Slate800, RoundedCornerShape(10.dp))
                .testTag("driver_logout_button")
            ) {
              Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Logout",
                tint = Slate300,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        // Offline / Pending queue banner
        SyncStatusBanner(
          isOnline = isOnline,
          queueItems = queueItems,
          onSyncNow = onSyncNow
        )
      }
    },
    bottomBar = {
      NavigationBar(
        containerColor = Slate900,
        tonalElevation = 8.dp,
        modifier = Modifier
          .border(1.dp, Slate800)
          .navigationBarsPadding()
      ) {
        val navItems = listOf(
          Triple("shift", "Shift", Icons.Default.Speed),
          Triple("extras", "Extras", Icons.Default.Route),
          Triple("expenses", "Expenses", Icons.Default.LocalGasStation),
          Triple("history", "History", Icons.Default.History)
        )
        navItems.forEach { (id, label, icon) ->
          NavigationBarItem(
            selected = selectedTab == id,
            onClick = { onTabSelected(id) },
            icon = { Icon(imageVector = icon, contentDescription = label) },
            label = { Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Sky400,
              selectedTextColor = Sky400,
              unselectedIconColor = Slate400,
              unselectedTextColor = Slate400,
              indicatorColor = Sky500.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("driver_nav_${id}")
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTab) {
        "shift" -> ShiftTabContent(
          driver = driver,
          activeTrip = activeTrip,
          buses = buses,
          onStartShift = onStartShift,
          onRequestCapture = { stageDef, odo ->
            stageToCapture = Pair(stageDef, odo)
          },
          onFinishShift = onFinishShift
        )
        "extras" -> ExtrasTabContent(
          driver = driver,
          extraTrips = extraTrips.filter { it.driverId == driver.id },
          onLogExtraTrip = onLogExtraTrip
        )
        "expenses" -> ExpensesTabContent(
          driver = driver,
          expenses = expenses.filter { it.driverId == driver.id },
          onLogExpense = onLogExpense,
          onViewReceipt = { title, path ->
            viewReceiptPath = Pair(title, path)
          }
        )
        "history" -> HistoryTabContent(
          driver = driver,
          trips = allTrips.filter { it.driverId == driver.id }
        )
      }
    }
  }

  // Active Watermark CameraX Screen
  stageToCapture?.let { (stageDef, odo) ->
    CameraCaptureScreen(
      driverName = driver.name,
      driverId = driver.id,
      busPlate = activeTrip?.bus ?: driver.bus,
      stageTitle = stageDef.title,
      odometerKm = odo,
      onCaptureSuccess = { _, bitmap, lat, lng ->
        onRecordStage(stageDef.key, stageDef.title, odo, bitmap, lat, lng)
        stageToCapture = null
      },
      onDismiss = { stageToCapture = null }
    )
  }

  // Receipt / Proof Inspector Dialog
  viewReceiptPath?.let { (title, path) ->
    ReceiptViewerDialog(
      title = title,
      subtitle = "Verification Receipt",
      imagePath = path,
      onDismiss = { viewReceiptPath = null }
    )
  }
}

// -------------------------------------------------------------
// TAB 1: SHIFT & 4-STAGE CHECKPOINT LOGGING
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShiftTabContent(
  driver: Driver,
  activeTrip: Trip?,
  buses: List<BusInfo>,
  onStartShift: (bus: String, busSource: String, startOdo: Double?) -> Unit,
  onRequestCapture: (StageDefinition, Double) -> Unit,
  onFinishShift: (Double?) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    if (activeTrip == null) {
      // PRE-TRIP CHECK & INITIALIZATION
      item {
        PreTripCheckCard(
          driver = driver,
          buses = buses,
          onStartShift = onStartShift
        )
      }
    } else {
      // SHIFT IN PROGRESS: PROGRESS & STAGE CHECKPOINTS
      item {
        ActiveShiftHeaderCard(trip = activeTrip)
      }

      // 4 Sequential Stages
      val completedStages = activeTrip.stages.associateBy { it.key }
      val currentStageIndex = SHIFT_STAGES.indexOfFirst { !completedStages.containsKey(it.key) }

      item {
        Text(
          text = "Sequential Shift Stages (4 Checkpoints)",
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )
      }

      items(SHIFT_STAGES) { stageDef ->
        val isDone = completedStages.containsKey(stageDef.key)
        val stageData = completedStages[stageDef.key]
        val stageIdx = SHIFT_STAGES.indexOf(stageDef)
        val isCurrent = stageIdx == currentStageIndex
        val isLocked = stageIdx > currentStageIndex && currentStageIndex != -1

        StageCard(
          stageDef = stageDef,
          isDone = isDone,
          isCurrent = isCurrent,
          isLocked = isLocked,
          data = stageData,
          onCapture = { odo -> onRequestCapture(stageDef, odo) }
        )
      }

      // FINISH SHIFT CARD
      item {
        FinishShiftCard(
          activeTrip = activeTrip,
          onFinishShift = onFinishShift
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreTripCheckCard(
  driver: Driver,
  buses: List<BusInfo>,
  onStartShift: (bus: String, busSource: String, startOdo: Double?) -> Unit
) {
  var selectedBus by remember { mutableStateOf(driver.bus) }
  var isManualBus by remember { mutableStateOf(false) }
  var manualBusInput by remember { mutableStateOf("") }
  var startOdoInput by remember { mutableStateOf("") }
  var isDropdownExpanded by remember { mutableStateOf(false) }

  val effectiveBus = if (isManualBus) manualBusInput.trim().uppercase() else selectedBus
  val lastOdo = buses.firstOrNull { it.plateNumber.equals(effectiveBus, ignoreCase = true) }?.lastOdometer

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Slate900),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, Slate800, RoundedCornerShape(20.dp))
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .background(Sky500.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = Icons.Default.DirectionsBus, contentDescription = null, tint = Sky400)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Pre-Trip Inspection & Start",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Confirm vehicle and starting odometer reading",
            color = Slate400,
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Bus selector
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(text = "Assigned Vehicle", color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(
          text = if (isManualBus) "MANUAL OVERRIDE" else "FLEET AUTO",
          color = if (isManualBus) Amber400 else Emerald400,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedTextField(
          value = if (isManualBus) "Manual: $manualBusInput" else selectedBus,
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Slate950,
            unfocusedContainerColor = Slate950,
            focusedBorderColor = Sky500,
            unfocusedBorderColor = Slate800,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .menuAnchor()
            .fillMaxWidth()
            .testTag("bus_selector_dropdown")
        )

        ExposedDropdownMenu(
          expanded = isDropdownExpanded,
          onDismissRequest = { isDropdownExpanded = false },
          modifier = Modifier.background(Slate900).border(1.dp, Slate800)
        ) {
          buses.forEach { busInfo ->
            DropdownMenuItem(
              text = {
                Row(
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(text = busInfo.plateNumber, color = Color.White, fontWeight = FontWeight.SemiBold)
                  busInfo.lastOdometer?.let {
                    Text(text = "${it.toInt()} km", color = Slate400, fontSize = 11.sp)
                  }
                }
              },
              onClick = {
                selectedBus = busInfo.plateNumber
                isManualBus = false
                isDropdownExpanded = false
              }
            )
          }
          DropdownMenuItem(
            text = { Text(text = "+ Enter Other / Manual Bus", color = Sky400, fontWeight = FontWeight.Bold) },
            onClick = {
              isManualBus = true
              isDropdownExpanded = false
            }
          )
        }
      }

      if (isManualBus) {
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = manualBusInput,
          onValueChange = { manualBusInput = it.uppercase() },
          placeholder = { Text(text = "e.g. DXB-9944", color = Slate700) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Slate950,
            unfocusedContainerColor = Slate950,
            focusedBorderColor = Amber400,
            unfocusedBorderColor = Slate800,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("manual_bus_input")
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Start ODO (Optional)
      Text(
        text = "Starting Odometer Reading (km)",
        color = Slate400,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.height(6.dp))

      OutlinedTextField(
        value = startOdoInput,
        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) startOdoInput = it },
        placeholder = { Text(text = lastOdo?.let { "${it.toInt()} km (previous)" } ?: "e.g. 124580", color = Slate700) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Slate950,
          unfocusedContainerColor = Slate950,
          focusedBorderColor = Sky500,
          unfocusedBorderColor = Slate800,
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("start_odometer_input")
      )

      if (lastOdo != null) {
        Text(
          text = "Last recorded reading for $effectiveBus: ${lastOdo.toInt()} km",
          color = Slate400,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = {
          val odo = startOdoInput.toDoubleOrNull()
          onStartShift(effectiveBus, if (isManualBus) "manual" else "auto", odo)
        },
        enabled = effectiveBus.isNotBlank(),
        colors = ButtonDefaults.buttonColors(
          containerColor = Sky500,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("start_shift_button")
      ) {
        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Start Shift on $effectiveBus", fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun ActiveShiftHeaderCard(trip: Trip) {
  val completedStagesCount = trip.stages.size
  val progress = (completedStagesCount / 4f).coerceIn(0f, 1f)

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Slate900),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, Slate800, RoundedCornerShape(18.dp))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = trip.bus,
              color = Color.White,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .background(Sky500.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .border(1.dp, Sky400.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "IN PROGRESS",
                color = Sky400,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
          }
          Text(
            text = "Shift Started: ${Formatters.formatTime(trip.createdAt)}",
            color = Slate400,
            fontSize = 11.sp
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(text = "Start ODO", color = Slate400, fontSize = 10.sp)
          Text(
            text = Formatters.formatKm(trip.startOdometer),
            color = Color.White,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(text = "Shift Checkpoints Progress", color = Slate400, fontSize = 11.sp)
        Text(
          text = "$completedStagesCount / 4 Recorded",
          color = Sky400,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      LinearProgressIndicator(
        progress = { progress },
        color = Sky500,
        trackColor = Slate800,
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
      )
    }
  }
}

@Composable
private fun StageCard(
  stageDef: StageDefinition,
  isDone: Boolean,
  isCurrent: Boolean,
  isLocked: Boolean,
  data: StageData?,
  onCapture: (Double) -> Unit
) {
  var odoInput by remember { mutableStateOf("") }

  val borderColor = when {
    isDone -> Emerald500.copy(alpha = 0.4f)
    isCurrent -> Sky500
    else -> Slate800
  }

  val iconBg = when {
    isDone -> Emerald500.copy(alpha = 0.15f)
    isCurrent -> Sky500.copy(alpha = 0.15f)
    else -> Slate800
  }

  val iconColor = when {
    isDone -> Emerald400
    isCurrent -> Sky400
    else -> Slate400
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Slate900),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, borderColor, RoundedCornerShape(16.dp))
      .testTag("stage_card_${stageDef.key}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .background(iconBg, RoundedCornerShape(10.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isDone) Icons.Default.Check else if (isLocked) Icons.Default.Lock else stageDef.icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = stageDef.title,
              color = if (isLocked) Slate400 else Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
            if (isDone) {
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "RECORDED",
                color = Emerald400,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                  .background(Emerald500.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                  .padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
          Text(
            text = stageDef.subtitle,
            color = Slate400,
            fontSize = 11.sp
          )
        }
      }

      if (isDone && data != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .background(Slate950, RoundedCornerShape(10.dp))
            .border(1.dp, Slate800, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Column {
            Text(text = "Odometer Stamp", color = Slate400, fontSize = 10.sp)
            Text(
              text = Formatters.formatKm(data.odometer),
              color = Emerald400,
              fontSize = 14.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(text = "Recorded Time", color = Slate400, fontSize = 10.sp)
            Text(
              text = Formatters.formatTime(data.timestamp),
              color = Color.White,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      if (isCurrent) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = odoInput,
            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) odoInput = it },
            placeholder = { Text(text = "Enter current km", color = Slate700) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Slate950,
              unfocusedContainerColor = Slate950,
              focusedBorderColor = Sky500,
              unfocusedBorderColor = Slate800,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .weight(1f)
              .height(50.dp)
              .testTag("stage_odo_input_${stageDef.key}")
          )

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = {
              val odo = odoInput.toDoubleOrNull() ?: 0.0
              onCapture(odo)
            },
            enabled = odoInput.isNotBlank() && (odoInput.toDoubleOrNull() ?: 0.0) > 0,
            colors = ButtonDefaults.buttonColors(
              containerColor = Sky500,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .height(50.dp)
              .testTag("capture_stage_button_${stageDef.key}")
          ) {
            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Verify", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun FinishShiftCard(
  activeTrip: Trip,
  onFinishShift: (Double?) -> Unit
) {
  var isExpanded by remember { mutableStateOf(false) }
  var endOdoInput by remember { mutableStateOf("") }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Slate900),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, Emerald500.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(Emerald500.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(imageVector = Icons.Default.Flag, contentDescription = null, tint = Emerald400, modifier = Modifier.size(18.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Finish & Seal Shift",
              color = Emerald400,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Calculate total distance and close shift",
              color = Slate400,
              fontSize = 11.sp
            )
          }
        }

        Button(
          onClick = { isExpanded = !isExpanded },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isExpanded) Slate800 else Emerald500,
            contentColor = if (isExpanded) Slate300 else Slate950
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("toggle_finish_shift_button")
        ) {
          Text(
            text = if (isExpanded) "Cancel" else "Finish Shift",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
          Text(
            text = "Final Return Parking ODO (km)",
            color = Slate400,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = endOdoInput,
            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) endOdoInput = it },
            placeholder = { Text(text = "e.g. 124710", color = Slate700) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Slate950,
              unfocusedContainerColor = Slate950,
              focusedBorderColor = Emerald400,
              unfocusedBorderColor = Slate800,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("end_odometer_input")
          )

          // Preview distance
          val endVal = endOdoInput.toDoubleOrNull()
          val startVal = activeTrip.startOdometer
          if (endVal != null && startVal != null && endVal >= startVal) {
            val dist = endVal - startVal
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Total Shift Distance: ${dist.toInt()} km",
              color = Emerald400,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              onFinishShift(endOdoInput.toDoubleOrNull())
              isExpanded = false
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = Emerald500,
              contentColor = Slate950
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("confirm_finish_shift_button")
          ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Confirm & Seal Shift", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 2: EXTRAS (INTER-CITY / AD-HOC TRIPS)
// -------------------------------------------------------------
@Composable
private fun ExtrasTabContent(
  driver: Driver,
  extraTrips: List<ExtraTrip>,
  onLogExtraTrip: (startKm: Double?, endKm: Double?, note: String, time: String) -> Unit
) {
  var startKmInput by remember { mutableStateOf("") }
  var endKmInput by remember { mutableStateOf("") }
  var noteInput by remember { mutableStateOf("") }
  var timeInput by remember { mutableStateOf("") }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Slate800, RoundedCornerShape(20.dp))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF8B5CF6).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Route, contentDescription = null, tint = Color(0xFFA78BFA))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Log Extra / Inter-City Route",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Airport transfer, VIP shuttle or ad-hoc run",
                color = Slate400,
                fontSize = 11.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
              value = startKmInput,
              onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) startKmInput = it },
              placeholder = { Text(text = "Start km", color = Slate700) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Slate950,
                unfocusedContainerColor = Slate950,
                focusedBorderColor = Sky500,
                unfocusedBorderColor = Slate800,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("extra_start_km_input")
            )

            OutlinedTextField(
              value = endKmInput,
              onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) endKmInput = it },
              placeholder = { Text(text = "End km", color = Slate700) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Slate950,
                unfocusedContainerColor = Slate950,
                focusedBorderColor = Sky500,
                unfocusedBorderColor = Slate800,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("extra_end_km_input")
            )
          }

          val startVal = startKmInput.toDoubleOrNull()
          val endVal = endKmInput.toDoubleOrNull()
          if (startVal != null && endVal != null && endVal >= startVal) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Derived Distance: ${(endVal - startVal).toInt()} km",
              color = Emerald400,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = timeInput,
            onValueChange = { timeInput = it },
            placeholder = { Text(text = "Departure time (e.g. 14:30)", color = Slate700) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Slate950,
              unfocusedContainerColor = Slate950,
              focusedBorderColor = Sky500,
              unfocusedBorderColor = Slate800,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("extra_time_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = noteInput,
            onValueChange = { noteInput = it },
            placeholder = { Text(text = "Route / purpose note (e.g. DXB Terminal 3 to Abu Dhabi)", color = Slate700) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Slate950,
              unfocusedContainerColor = Slate950,
              focusedBorderColor = Sky500,
              unfocusedBorderColor = Slate800,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("extra_note_input")
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              onLogExtraTrip(startVal, endVal, noteInput, timeInput)
              startKmInput = ""
              endKmInput = ""
              noteInput = ""
              timeInput = ""
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF8B5CF6),
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("submit_extra_trip_button")
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Save Extra Trip", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    item {
      Text(
        text = "Recent Extra Trips (${extraTrips.size})",
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
      )
    }

    if (extraTrips.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "No extra trips logged in the last 30 days.", color = Slate400, fontSize = 12.sp)
        }
      }
    } else {
      items(extraTrips) { extra ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Slate900),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = extra.bus,
                  color = Color.White,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
                if (extra.time.isNotBlank()) {
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(text = extra.time, color = Slate400, fontSize = 11.sp)
                }
              }
              if (extra.note.isNotBlank()) {
                Text(text = extra.note, color = Slate300, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
              }
              Text(
                text = Formatters.formatDate(extra.createdAt),
                color = Slate400,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
              )
            }

            extra.distance?.let { dist ->
              Box(
                modifier = Modifier
                  .background(Sky500.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "${dist.toInt()} km",
                  color = Sky400,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 3: EXPENSES & FUEL SLIPS
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpensesTabContent(
  driver: Driver,
  expenses: List<Expense>,
  onLogExpense: (type: String, mode: String, liters: Double?, unitPrice: Double?, amount: Double, note: String, receiptBitmap: Bitmap?) -> Unit,
  onViewReceipt: (title: String, path: String) -> Unit
) {
  var mode by remember { mutableStateOf("calc") } // "calc" or "direct"
  var category by remember { mutableStateOf("Fuel") }
  var litersInput by remember { mutableStateOf("") }
  var priceInput by remember { mutableStateOf("2.85") }
  var amountInput by remember { mutableStateOf("") }
  var noteInput by remember { mutableStateOf("") }
  var receiptBitmap by remember { mutableStateOf<Bitmap?>(null) }

  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap ->
    if (bitmap != null) receiptBitmap = bitmap
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Slate800, RoundedCornerShape(20.dp))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .background(Amber500.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null, tint = Amber400)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Fuel & Operational Expenses",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Refill slips, vehicle wash, parking or tolls",
                color = Slate400,
                fontSize = 11.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Mode toggle
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Slate950, RoundedCornerShape(10.dp))
              .border(1.dp, Slate800, RoundedCornerShape(10.dp))
              .padding(4.dp)
          ) {
            Button(
              onClick = { mode = "calc" },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (mode == "calc") Sky500 else Color.Transparent,
                contentColor = if (mode == "calc") Color.White else Slate400
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(36.dp)
            ) {
              Text(text = "Liters × Rate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = { mode = "direct" },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (mode == "direct") Sky500 else Color.Transparent,
                contentColor = if (mode == "direct") Color.White else Slate400
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(36.dp)
            ) {
              Text(text = "Direct AED", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Category Chips
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            listOf("Fuel", "Wash", "Parking", "Maintenance").forEach { cat ->
              Box(
                modifier = Modifier
                  .background(
                    if (category == cat) Amber500.copy(alpha = 0.2f) else Slate950,
                    RoundedCornerShape(8.dp)
                  )
                  .border(
                    1.dp,
                    if (category == cat) Amber400 else Slate800,
                    RoundedCornerShape(8.dp)
                  )
                  .clickable { category = cat }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = cat,
                  color = if (category == cat) Amber400 else Slate400,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          if (mode == "calc") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              OutlinedTextField(
                value = litersInput,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) litersInput = it },
                placeholder = { Text(text = "Liters (e.g. 60)", color = Slate700) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = Slate950,
                  unfocusedContainerColor = Slate950,
                  focusedBorderColor = Sky500,
                  unfocusedBorderColor = Slate800,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).testTag("expense_liters_input")
              )

              OutlinedTextField(
                value = priceInput,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) priceInput = it },
                placeholder = { Text(text = "AED / L", color = Slate700) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = Slate950,
                  unfocusedContainerColor = Slate950,
                  focusedBorderColor = Sky500,
                  unfocusedBorderColor = Slate800,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).testTag("expense_price_input")
              )
            }

            val lVal = litersInput.toDoubleOrNull()
            val pVal = priceInput.toDoubleOrNull()
            if (lVal != null && pVal != null) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Total: ${Formatters.formatAed(lVal * pVal)}",
                color = Emerald400,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
          } else {
            OutlinedTextField(
              value = amountInput,
              onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amountInput = it },
              placeholder = { Text(text = "Total amount (AED)", color = Slate700) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Slate950,
                unfocusedContainerColor = Slate950,
                focusedBorderColor = Sky500,
                unfocusedBorderColor = Slate800,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = noteInput,
            onValueChange = { noteInput = it },
            placeholder = { Text(text = "Station / vendor note (e.g. ADNOC Al Quoz)", color = Slate700) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Slate950,
              unfocusedContainerColor = Slate950,
              focusedBorderColor = Sky500,
              unfocusedBorderColor = Slate800,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("expense_note_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Receipt Attachment Button
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
              .fillMaxWidth()
              .background(Slate950, RoundedCornerShape(12.dp))
              .border(1.dp, Slate800, RoundedCornerShape(12.dp))
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                tint = if (receiptBitmap != null) Emerald400 else Slate400,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (receiptBitmap != null) "Receipt Attached" else "Attach Receipt / Fuel Slip",
                color = if (receiptBitmap != null) Emerald400 else Slate400,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }

            OutlinedButton(
              onClick = { cameraLauncher.launch(null) },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Sky400),
              border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Slate700)),
              modifier = Modifier.height(34.dp)
            ) {
              Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = if (receiptBitmap != null) "Retake" else "Capture", fontSize = 11.sp)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              val amt = if (mode == "calc") {
                val l = litersInput.toDoubleOrNull() ?: 0.0
                val p = priceInput.toDoubleOrNull() ?: 0.0
                l * p
              } else {
                amountInput.toDoubleOrNull() ?: 0.0
              }
              onLogExpense(
                category,
                mode,
                litersInput.toDoubleOrNull(),
                priceInput.toDoubleOrNull(),
                amt,
                noteInput,
                receiptBitmap
              )
              litersInput = ""
              amountInput = ""
              noteInput = ""
              receiptBitmap = null
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = Amber500,
              contentColor = Slate950
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("submit_expense_button")
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Save Expense Record", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    item {
      Text(
        text = "Recent Expenses (${expenses.size})",
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
      )
    }

    if (expenses.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "No expenses recorded in the last 30 days.", color = Slate400, fontSize = 12.sp)
        }
      }
    } else {
      items(expenses) { exp ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Slate900),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = exp.type,
                  color = Color.White,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = exp.bus,
                  color = Slate400,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
              if (exp.note.isNotBlank()) {
                Text(text = exp.note, color = Slate300, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
              }
              Text(
                text = Formatters.formatDate(exp.createdAt),
                color = Slate400,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = Formatters.formatAed(exp.amount),
                color = Amber400,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              if (exp.receiptPath != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .background(Sky500.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .clickable { onViewReceipt("${exp.type} (${exp.bus})", exp.receiptPath) }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = Sky400, modifier = Modifier.size(11.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(text = "View Slip", color = Sky400, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
              }
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 4: SHIFT HISTORY
// -------------------------------------------------------------
@Composable
private fun HistoryTabContent(
  driver: Driver,
  trips: List<Trip>
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text(
        text = "Your Shift History (Last 30 Days)",
        color = Color.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold
      )
    }

    if (trips.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "No shifts recorded yet.", color = Slate400, fontSize = 13.sp)
        }
      }
    } else {
      items(trips) { trip ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Slate900),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(16.dp))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column {
                Text(
                  text = trip.bus,
                  color = Color.White,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = Formatters.formatDate(trip.createdAt),
                  color = Slate400,
                  fontSize = 11.sp
                )
              }

              Box(
                modifier = Modifier
                  .background(
                    if (trip.status == "completed") Emerald500.copy(alpha = 0.15f) else Amber500.copy(alpha = 0.15f),
                    RoundedCornerShape(6.dp)
                  )
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = if (trip.status == "completed") "COMPLETED" else "IN PROGRESS",
                  color = if (trip.status == "completed") Emerald400 else Amber400,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier
                .fillMaxWidth()
                .background(Slate950, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Column {
                Text(text = "Start ODO", color = Slate400, fontSize = 10.sp)
                Text(
                  text = Formatters.formatKm(trip.startOdometer),
                  color = Color.White,
                  fontSize = 12.sp,
                  fontFamily = FontFamily.Monospace
                )
              }

              Column {
                Text(text = "End ODO", color = Slate400, fontSize = 10.sp)
                Text(
                  text = Formatters.formatKm(trip.endOdometer),
                  color = Color.White,
                  fontSize = 12.sp,
                  fontFamily = FontFamily.Monospace
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(text = "Total Distance", color = Slate400, fontSize = 10.sp)
                Text(
                  text = Formatters.formatKm(trip.distance),
                  color = Sky400,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }
    }
  }
}
