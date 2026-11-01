package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusInfo
import com.example.data.model.Driver
import com.example.data.model.Expense
import com.example.data.model.ExtraTrip
import com.example.data.model.QueueItem
import com.example.data.model.Trip
import com.example.ui.components.ClearTesterDataDialog
import com.example.ui.components.NetworkBadge
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Red400
import com.example.ui.theme.Red500
import com.example.ui.theme.Sky400
import com.example.ui.theme.Sky500
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPortalScreen(
  drivers: List<Driver>,
  trips: List<Trip>,
  extraTrips: List<ExtraTrip>,
  expenses: List<Expense>,
  buses: List<BusInfo>,
  queueItems: List<QueueItem>,
  isOnline: Boolean,
  selectedTab: String,
  onTabSelected: (String) -> Unit,
  onToggleOnline: () -> Unit,
  onToggleDriverStatus: (String) -> Unit,
  onUpdateDriverPin: (String, String) -> Unit,
  onRandomizePin: (String) -> Unit,
  onReassignBus: (String, String) -> Unit,
  onAddDriver: (name: String, id: String, bus: String, phone: String, pin: String) -> Unit,
  onAddBus: (String) -> Unit,
  onClearTesterData: (clearShifts: Boolean, clearExtras: Boolean, clearExpenses: Boolean, clearQueue: Boolean) -> Unit,
  onSeedSampleData: () -> Unit,
  onResetFactory: () -> Unit,
  onLogout: () -> Unit
) {
  var showClearDialog by remember { mutableStateOf(false) }
  var showAddDriverDialog by remember { mutableStateOf(false) }
  var showAddBusDialog by remember { mutableStateOf(false) }

  Scaffold(
    containerColor = Slate950,
    topBar = {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .background(Slate900)
          .border(1.dp, Slate800)
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Column {
          Text(
            text = "AVA Fleet Operations",
            color = Slate400,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Admin Center",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .background(Color(0xFF7C3AED).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFFA78BFA).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
              Text(
                text = "DISPATCH",
                color = Color(0xFFA78BFA),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Quick Clear Tester Data Shortcut
          IconButton(
            onClick = { showClearDialog = true },
            modifier = Modifier
              .size(36.dp)
              .background(Red500.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
              .border(1.dp, Red500.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
              .testTag("admin_header_clean_button")
          ) {
            Icon(
              imageVector = Icons.Default.CleaningServices,
              contentDescription = "Clear Tester Data",
              tint = Red400,
              modifier = Modifier.size(17.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

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
              .testTag("admin_logout_button")
          ) {
            Icon(
              imageVector = Icons.Default.Logout,
              contentDescription = "Logout",
              tint = Slate400,
              modifier = Modifier.size(16.dp)
            )
          }
        }
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
          Triple("overview", "Overview", Icons.Default.Dashboard),
          Triple("roster", "Roster", Icons.Default.People),
          Triple("audit", "Audit", Icons.Default.History),
          Triple("buses", "Buses", Icons.Default.DirectionsBus),
          Triple("cleaner", "Cleaner", Icons.Default.CleaningServices)
        )
        navItems.forEach { (id, label, icon) ->
          NavigationBarItem(
            selected = selectedTab == id,
            onClick = { onTabSelected(id) },
            icon = { Icon(imageVector = icon, contentDescription = label) },
            label = { Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = if (id == "cleaner") Red400 else Sky400,
              selectedTextColor = if (id == "cleaner") Red400 else Sky400,
              unselectedIconColor = Slate400,
              unselectedTextColor = Slate400,
              indicatorColor = if (id == "cleaner") Red500.copy(alpha = 0.15f) else Sky500.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("admin_nav_${id}")
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
        "overview" -> AdminOverviewTab(
          drivers = drivers,
          trips = trips,
          expenses = expenses,
          extraTrips = extraTrips,
          onOpenCleaner = { showClearDialog = true }
        )
        "roster" -> AdminRosterTab(
          drivers = drivers,
          buses = buses,
          onToggleDriverStatus = onToggleDriverStatus,
          onUpdateDriverPin = onUpdateDriverPin,
          onRandomizePin = onRandomizePin,
          onReassignBus = onReassignBus,
          onOpenAddDriver = { showAddDriverDialog = true }
        )
        "audit" -> AdminAuditTab(
          trips = trips,
          extraTrips = extraTrips,
          expenses = expenses
        )
        "buses" -> AdminBusesTab(
          buses = buses,
          drivers = drivers,
          onOpenAddBus = { showAddBusDialog = true }
        )
        "cleaner" -> AdminCleanerTab(
          tripsCount = trips.size,
          extraTripsCount = extraTrips.size,
          expensesCount = expenses.size,
          queueCount = queueItems.size,
          onClearSelected = onClearTesterData,
          onResetFactory = onResetFactory,
          onSeedSampleData = onSeedSampleData
        )
      }
    }
  }

  // Clear Tester Data Modal Dialog
  if (showClearDialog) {
    ClearTesterDataDialog(
      tripsCount = trips.size,
      extraTripsCount = extraTrips.size,
      expensesCount = expenses.size,
      queueCount = queueItems.size,
      onClearSelected = { s, e, ex, q ->
        onClearTesterData(s, e, ex, q)
        showClearDialog = false
      },
      onResetFactory = {
        onResetFactory()
        showClearDialog = false
      },
      onSeedSampleData = {
        onSeedSampleData()
        showClearDialog = false
      },
      onDismiss = { showClearDialog = false }
    )
  }

  // Add Driver Dialog
  if (showAddDriverDialog) {
    AddDriverDialog(
      buses = buses,
      onConfirm = { name, id, bus, phone, pin ->
        onAddDriver(name, id, bus, phone, pin)
        showAddDriverDialog = false
      },
      onDismiss = { showAddDriverDialog = false }
    )
  }

  // Add Bus Dialog
  if (showAddBusDialog) {
    AddBusDialog(
      onConfirm = { plate ->
        onAddBus(plate)
        showAddBusDialog = false
      },
      onDismiss = { showAddBusDialog = false }
    )
  }
}

// -------------------------------------------------------------
// TAB 1: ADMIN OVERVIEW
// -------------------------------------------------------------
@Composable
private fun AdminOverviewTab(
  drivers: List<Driver>,
  trips: List<Trip>,
  expenses: List<Expense>,
  extraTrips: List<ExtraTrip>,
  onOpenCleaner: () -> Unit
) {
  val completedShifts = trips.count { it.status == "completed" }
  val inProgressShifts = trips.count { it.status == "in_progress" }
  val activeDriversCount = drivers.count { it.status == "active" }
  val totalExpenseAmount = expenses.sumOf { it.amount }
  val totalShiftKm = trips.mapNotNull { it.distance }.sum()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // PRIMARY HIGHLIGHT: Clean Tester Data Card
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Red500.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
          .testTag("admin_clean_tester_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(38.dp)
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
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Tester Data Cleaner",
                  color = Color.White,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "${trips.size} shifts • ${expenses.size} expenses • ${extraTrips.size} extras",
                  color = Red400,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            Button(
              onClick = onOpenCleaner,
              colors = ButtonDefaults.buttonColors(
                containerColor = Red500,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier.testTag("open_cleaner_dialog_button")
            ) {
              Text(text = "Clean All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
          Text(
            text = "Easily wipe all test runs, odometer records, and receipts with one click to keep your fleet ledger pristine.",
            color = Slate400,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 8.dp)
          )
        }
      }
    }

    // KPI Metrics Grid
    item {
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        KpiCard(
          title = "Completed",
          value = "$completedShifts",
          subtitle = "Shifts sealed",
          icon = Icons.Default.CheckCircle,
          tint = Emerald400,
          modifier = Modifier.weight(1f)
        )
        KpiCard(
          title = "In Progress",
          value = "$inProgressShifts",
          subtitle = "Active now",
          icon = Icons.Default.Speed,
          tint = Amber400,
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        KpiCard(
          title = "Active Drivers",
          value = "$activeDriversCount / ${drivers.size}",
          subtitle = "On roster",
          icon = Icons.Default.People,
          tint = Sky400,
          modifier = Modifier.weight(1f)
        )
        KpiCard(
          title = "Total Expenses",
          value = Formatters.formatAed(totalExpenseAmount),
          subtitle = "Fuel & operational",
          icon = Icons.Default.Speed,
          tint = Emerald400,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Live Submissions Feed Header
    item {
      Text(
        text = "Live Dispatch Activity Feed",
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
      )
    }

    val recentActivity = trips.take(8)
    if (recentActivity.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "No shifts recorded yet.", color = Slate400, fontSize = 12.sp)
        }
      }
    } else {
      items(recentActivity) { trip ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Slate900),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .background(Sky500.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.DirectionsBus, contentDescription = null, tint = Sky400, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = trip.driverName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "(${trip.bus})", color = Sky400, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
              }
              Text(
                text = "${trip.stages.size}/4 Checkpoints • Start: ${Formatters.formatKm(trip.startOdometer)}",
                color = Slate400,
                fontSize = 11.sp
              )
            }
            Text(
              text = Formatters.formatTime(trip.createdAt),
              color = Slate400,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }
  }
}

@Composable
private fun KpiCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  tint: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Slate900),
    modifier = modifier.border(1.dp, Slate800, RoundedCornerShape(16.dp))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(text = title, color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Text(text = subtitle, color = Slate400, fontSize = 10.sp, modifier = Modifier.padding(top = 1.dp))
    }
  }
}

// -------------------------------------------------------------
// TAB 2: ROSTER & PIN MANAGEMENT
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminRosterTab(
  drivers: List<Driver>,
  buses: List<BusInfo>,
  onToggleDriverStatus: (String) -> Unit,
  onUpdateDriverPin: (String, String) -> Unit,
  onRandomizePin: (String) -> Unit,
  onReassignBus: (String, String) -> Unit,
  onOpenAddDriver: () -> Unit
) {
  var editingPinDriverId by remember { mutableStateOf<String?>(null) }
  var newPinInput by remember { mutableStateOf("") }

  Scaffold(
    containerColor = Color.Transparent,
    floatingActionButton = {
      FloatingActionButton(
        onClick = onOpenAddDriver,
        containerColor = Sky500,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier.testTag("add_driver_fab")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Driver")
      }
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            Text(text = "Driver Roster", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = "Manage drivers, PIN security, and assigned vehicles", color = Slate400, fontSize = 11.sp)
          }
        }
      }

      items(drivers) { driver ->
        val isActive = driver.status == "active"

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Slate900),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(16.dp))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column {
                Text(text = driver.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = "ID: ${driver.id} • ${driver.phone}", color = Slate400, fontSize = 11.sp)
              }

              Box(
                modifier = Modifier
                  .background(
                    if (isActive) Emerald500.copy(alpha = 0.15f) else Red500.copy(alpha = 0.15f),
                    RoundedCornerShape(6.dp)
                  )
                  .padding(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(
                  text = if (isActive) "ACTIVE" else "LOCKED",
                  color = if (isActive) Emerald400 else Red400,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Assigned vehicle row
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .background(Slate950, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(text = "Assigned Bus: ", color = Slate400, fontSize = 11.sp)
              Text(
                text = driver.bus,
                color = Sky400,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // PIN and controls
            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "PIN: ", color = Slate400, fontSize = 12.sp)
                Text(
                  text = driver.pin,
                  color = Color.White,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier
                    .background(Slate950, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                TextButton(
                  onClick = { onRandomizePin(driver.id) },
                  contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(text = "Randomize", color = Sky400, fontSize = 11.sp)
                }
              }

              OutlinedButton(
                onClick = { onToggleDriverStatus(driver.id) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                  contentColor = if (isActive) Red400 else Emerald400
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                  brush = androidx.compose.ui.graphics.SolidColor(if (isActive) Red500.copy(alpha = 0.5f) else Emerald500.copy(alpha = 0.5f))
                ),
                modifier = Modifier.height(32.dp)
              ) {
                Text(text = if (isActive) "Lock" else "Activate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 3: AUDIT TRAIL & CSV EXPORT
// -------------------------------------------------------------
@Composable
private fun AdminAuditTab(
  trips: List<Trip>,
  extraTrips: List<ExtraTrip>,
  expenses: List<Expense>
) {
  var searchQuery by remember { mutableStateOf("") }
  var filterType by remember { mutableStateOf("All") }
  val context = LocalContext.current

  // Combined audit list
  val allRecords = remember(trips, extraTrips, expenses) {
    val list = mutableListOf<AuditItem>()
    trips.forEach {
      list.add(
        AuditItem(
          id = it.id,
          type = "Shift",
          driverName = it.driverName,
          bus = it.bus,
          detail = "${it.stages.size}/4 Checkpoints • Start: ${Formatters.formatKm(it.startOdometer)} • End: ${Formatters.formatKm(it.endOdometer)}",
          timestamp = it.createdAt,
          status = it.status.uppercase()
        )
      )
    }
    extraTrips.forEach {
      list.add(
        AuditItem(
          id = it.id,
          type = "Extra",
          driverName = it.driverName,
          bus = it.bus,
          detail = if (it.distance != null) "${it.distance.toInt()} km (${it.note})" else it.note,
          timestamp = it.createdAt,
          status = "LOGGED"
        )
      )
    }
    expenses.forEach {
      list.add(
        AuditItem(
          id = it.id,
          type = "Expense",
          driverName = it.driverName,
          bus = it.bus,
          detail = "${it.type}: ${Formatters.formatAed(it.amount)} (${it.note})",
          timestamp = it.createdAt,
          status = it.mode.uppercase()
        )
      )
    }
    list.sortedByDescending { it.timestamp }
  }

  val filteredRecords = remember(allRecords, searchQuery, filterType) {
    allRecords.filter { item ->
      val matchesSearch = searchQuery.isBlank() ||
        item.driverName.contains(searchQuery, ignoreCase = true) ||
        item.bus.contains(searchQuery, ignoreCase = true) ||
        item.detail.contains(searchQuery, ignoreCase = true)
      val matchesFilter = filterType == "All" || item.type == filterType
      matchesSearch && matchesFilter
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          Text(text = "Fleet Audit Trail", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Text(text = "${filteredRecords.size} logged events", color = Slate400, fontSize = 11.sp)
        }

        Button(
          onClick = {
            exportCsv(context, filteredRecords)
          },
          colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Sky400),
          shape = RoundedCornerShape(10.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.height(34.dp).testTag("export_csv_button")
        ) {
          Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Export CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Search bar
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text(text = "Search by driver, bus plate or detail...", color = Slate700) },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate400) },
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Slate900,
          unfocusedContainerColor = Slate900,
          focusedBorderColor = Sky500,
          unfocusedBorderColor = Slate800,
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("audit_search_input")
      )
    }

    // Filter Chips
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("All", "Shift", "Extra", "Expense").forEach { cat ->
          Box(
            modifier = Modifier
              .background(
                if (filterType == cat) Sky500.copy(alpha = 0.2f) else Slate900,
                RoundedCornerShape(8.dp)
              )
              .border(
                1.dp,
                if (filterType == cat) Sky400 else Slate800,
                RoundedCornerShape(8.dp)
              )
              .clickable { filterType = cat }
              .padding(horizontal = 10.dp, vertical = 5.dp)
          ) {
            Text(
              text = cat,
              color = if (filterType == cat) Sky400 else Slate400,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }

    if (filteredRecords.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "No matching audit records found.", color = Slate400, fontSize = 12.sp)
        }
      }
    } else {
      items(filteredRecords) { record ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Slate900),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .background(
                  when (record.type) {
                    "Shift" -> Sky500.copy(alpha = 0.15f)
                    "Extra" -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
                    else -> Amber500.copy(alpha = 0.15f)
                  },
                  RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
              Text(
                text = record.type.uppercase(),
                color = when (record.type) {
                  "Shift" -> Sky400
                  "Extra" -> Color(0xFFA78BFA)
                  else -> Amber400
                },
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = record.driverName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = record.bus, color = Slate400, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
              }
              Text(text = record.detail, color = Slate300, fontSize = 11.sp, modifier = Modifier.padding(top = 1.dp))
            }

            Text(
              text = Formatters.formatTime(record.timestamp),
              color = Slate400,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }
  }
}

data class AuditItem(
  val id: String,
  val type: String,
  val driverName: String,
  val bus: String,
  val detail: String,
  val timestamp: Long,
  val status: String
)

private fun exportCsv(context: Context, items: List<AuditItem>) {
  val csv = StringBuilder()
  csv.append("Type,Driver,Bus,Detail,Timestamp,Status\n")
  items.forEach {
    csv.append("\"${it.type}\",\"${it.driverName}\",\"${it.bus}\",\"${it.detail.replace("\"", "\"\"")}\",\"${Formatters.formatDate(it.timestamp)}\",\"${it.status}\"\n")
  }
  val intent = Intent(Intent.ACTION_SEND).apply {
    type = "text/csv"
    putExtra(Intent.EXTRA_SUBJECT, "AVA Fleet Audit Report")
    putExtra(Intent.EXTRA_TEXT, csv.toString())
  }
  context.startActivity(Intent.createChooser(intent, "Share Audit CSV"))
}

// -------------------------------------------------------------
// TAB 4: BUSES REGISTRY
// -------------------------------------------------------------
@Composable
private fun AdminBusesTab(
  buses: List<BusInfo>,
  drivers: List<Driver>,
  onOpenAddBus: () -> Unit
) {
  Scaffold(
    containerColor = Color.Transparent,
    floatingActionButton = {
      FloatingActionButton(
        onClick = onOpenAddBus,
        containerColor = Sky500,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier.testTag("add_bus_fab")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Bus")
      }
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Column {
          Text(text = "Fleet Bus Registry", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Text(text = "${buses.size} registered commercial vehicles", color = Slate400, fontSize = 11.sp)
        }
      }

      items(buses) { bus ->
        val assigned = drivers.filter { it.bus.equals(bus.plateNumber, ignoreCase = true) }

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Slate900),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(16.dp))
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .background(Sky500.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.DirectionsBus, contentDescription = null, tint = Sky400, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = bus.plateNumber,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "Last ODO: ${Formatters.formatKm(bus.lastOdometer)}",
                color = Emerald400,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp)
              )
              Text(
                text = if (assigned.isNotEmpty()) "Drivers: ${assigned.joinToString { it.name }}" else "No driver currently assigned",
                color = Slate400,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
              )
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 5: DEDICATED TESTER DATA CLEANER TAB (USER HIGHLIGHT)
// -------------------------------------------------------------
@Composable
private fun AdminCleanerTab(
  tripsCount: Int,
  extraTripsCount: Int,
  expensesCount: Int,
  queueCount: Int,
  onClearSelected: (Boolean, Boolean, Boolean, Boolean) -> Unit,
  onResetFactory: () -> Unit,
  onSeedSampleData: () -> Unit
) {
  var clearShifts by remember { mutableStateOf(true) }
  var clearExtras by remember { mutableStateOf(true) }
  var clearExpenses by remember { mutableStateOf(true) }
  var clearQueue by remember { mutableStateOf(true) }
  var showConfirmDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Column {
        Text(
          text = "Clear Tester Data Center",
          color = Color.White,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Easily wipe out all test logs, simulated odometer checkpoints, and dummy expenses.",
          color = Slate400,
          fontSize = 12.sp,
          modifier = Modifier.padding(top = 2.dp)
        )
      }
    }

    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Red500.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Red400, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Current Database Testing Records", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(14.dp))

          CleanerCountRow("Active & Closed Shifts", tripsCount, clearShifts) { clearShifts = it }
          CleanerCountRow("Extra / Inter-City Runs", extraTripsCount, clearExtras) { clearExtras = it }
          CleanerCountRow("Fuel & Expense Slips", expensesCount, clearExpenses) { clearExpenses = it }
          CleanerCountRow("Pending Sync Queue Items", queueCount, clearQueue) { clearQueue = it }

          Spacer(modifier = Modifier.height(18.dp))

          Button(
            onClick = { showConfirmDialog = true },
            enabled = (clearShifts && tripsCount > 0) || (clearExtras && extraTripsCount > 0) || (clearExpenses && expensesCount > 0) || (clearQueue && queueCount > 0),
            colors = ButtonDefaults.buttonColors(
              containerColor = Red500,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("admin_cleaner_purge_button")
          ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Wipe Selected Tester Data Now", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Slate800, RoundedCornerShape(18.dp))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(text = "Developer & Testing Shortcuts", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Text(
            text = "Generate dummy data to test logging again, or reset all drivers and vehicles back to factory clean.",
            color = Slate400,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
          )

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
              onClick = onSeedSampleData,
              colors = ButtonDefaults.buttonColors(
                containerColor = Sky500.copy(alpha = 0.2f),
                contentColor = Sky400
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).height(42.dp)
            ) {
              Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "Add Samples", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = onResetFactory,
              colors = ButtonDefaults.buttonColors(
                containerColor = Amber500.copy(alpha = 0.2f),
                contentColor = Amber400
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).height(42.dp)
            ) {
              Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "Factory Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  if (showConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showConfirmDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Red400)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Confirm Data Wipe", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Text(
          text = "Are you sure you want to permanently clear the selected tester data? This will purge the records from local storage.",
          color = Slate400,
          fontSize = 12.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onClearSelected(clearShifts, clearExtras, clearExpenses, clearQueue)
            showConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Red500)
        ) {
          Text(text = "Yes, Clear Tester Data")
        }
      },
      dismissButton = {
        TextButton(onClick = { showConfirmDialog = false }) {
          Text(text = "Cancel", color = Slate400)
        }
      },
      containerColor = Slate900,
      shape = RoundedCornerShape(16.dp)
    )
  }
}

@Composable
private fun CleanerCountRow(
  label: String,
  count: Int,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
      .background(Slate950, RoundedCornerShape(8.dp))
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = CheckboxDefaults.colors(
          checkedColor = Red500,
          uncheckedColor = Slate700,
          checkmarkColor = Color.White
        )
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }

    Text(
      text = "$count records",
      color = if (count > 0) Red400 else Slate400,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold
    )
  }
}

// --- DIALOGS ---
@Composable
private fun AddDriverDialog(
  buses: List<BusInfo>,
  onConfirm: (name: String, id: String, bus: String, phone: String, pin: String) -> Unit,
  onDismiss: () -> Unit
) {
  var name by remember { mutableStateOf("") }
  var id by remember { mutableStateOf("") }
  var bus by remember { mutableStateOf(buses.firstOrNull()?.plateNumber ?: "DXB-4821") }
  var phone by remember { mutableStateOf("") }
  var pin by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(text = "Register New Driver", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          placeholder = { Text("Driver Full Name", color = Slate700) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Slate950,
            unfocusedContainerColor = Slate950,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = id,
          onValueChange = { id = it.uppercase() },
          placeholder = { Text("Driver ID (e.g. DRV-1005)", color = Slate700) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Slate950,
            unfocusedContainerColor = Slate950,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = bus,
          onValueChange = { bus = it.uppercase() },
          placeholder = { Text("Assigned Bus (e.g. DXB-4821)", color = Slate700) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Slate950,
            unfocusedContainerColor = Slate950,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          placeholder = { Text("Phone Number", color = Slate700) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Slate950,
            unfocusedContainerColor = Slate950,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = pin,
          onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin = it },
          placeholder = { Text("4-digit PIN", color = Slate700) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Slate950,
            unfocusedContainerColor = Slate950,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(name, id, bus, phone, pin) },
        enabled = name.isNotBlank() && id.isNotBlank() && pin.length >= 4,
        colors = ButtonDefaults.buttonColors(containerColor = Sky500)
      ) {
        Text("Save Driver")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = Slate400)
      }
    },
    containerColor = Slate900,
    shape = RoundedCornerShape(16.dp)
  )
}

@Composable
private fun AddBusDialog(
  onConfirm: (plate: String) -> Unit,
  onDismiss: () -> Unit
) {
  var plate by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(text = "Register Fleet Vehicle", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    },
    text = {
      Column {
        Text(text = "Enter vehicle plate number for UAE commercial registration.", color = Slate400, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = plate,
          onValueChange = { plate = it.uppercase() },
          placeholder = { Text("e.g. DXB-9988", color = Slate700) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Slate950,
            unfocusedContainerColor = Slate950,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(plate) },
        enabled = plate.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = Sky500)
      ) {
        Text("Register Bus")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = Slate400)
      }
    },
    containerColor = Slate900,
    shape = RoundedCornerShape(16.dp)
  )
}
