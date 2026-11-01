package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ClearTesterDataDialog
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.AdminPortalScreen
import com.example.ui.screens.DriverLoginScreen
import com.example.ui.screens.DriverPortalScreen
import com.example.ui.screens.RoleSelectorScreen
import com.example.ui.theme.FleetShiftTheme
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.FleetViewModel
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {

  private val viewModel: FleetViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Configure Firebase App Check with launch intent before rendering Compose UI or Firebase calls
    configureAppCheck(this, intent)

    setContent {
      FleetShiftTheme {
        FleetShiftApp(viewModel)
      }
    }
  }
}

@Composable
fun FleetShiftApp(viewModel: FleetViewModel) {
  val activeRole by viewModel.activeRole.collectAsStateWithLifecycle()
  val activeDriver by viewModel.activeDriver.collectAsStateWithLifecycle()
  val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
  val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
  val showClearDialog by viewModel.showClearTesterDataDialog.collectAsStateWithLifecycle()

  val drivers by viewModel.drivers.collectAsStateWithLifecycle()
  val trips by viewModel.trips.collectAsStateWithLifecycle()
  val extraTrips by viewModel.extraTrips.collectAsStateWithLifecycle()
  val expenses by viewModel.expenses.collectAsStateWithLifecycle()
  val buses by viewModel.buses.collectAsStateWithLifecycle()
  val syncQueue by viewModel.syncQueue.collectAsStateWithLifecycle()
  val activeTrip by viewModel.activeTrip.collectAsStateWithLifecycle()

  val selectedDriverTab by viewModel.selectedDriverTab.collectAsStateWithLifecycle()
  val selectedAdminTab by viewModel.selectedAdminTab.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(toastMessage) {
    toastMessage?.let {
      snackbarHostState.showSnackbar(
        message = it,
        duration = SnackbarDuration.Short
      )
      viewModel.dismissToast()
    }
  }

  // Handle system back navigation
  BackHandler(enabled = activeRole != null) {
    when (activeRole) {
      "driver_login", "admin_login" -> viewModel.setRole(null)
      "driver", "admin" -> viewModel.logout()
      else -> viewModel.setRole(null)
    }
  }

  Scaffold(
    containerColor = Slate950,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Slate950)
        .padding(innerPadding)
    ) {
      Crossfade(targetState = activeRole, label = "role_navigation") { role ->
        when (role) {
          null -> {
            RoleSelectorScreen(
              driverCount = drivers.size,
              busCount = buses.size,
              tripsCount = trips.size,
              expensesCount = expenses.size,
              onSelectDriver = { viewModel.setRole("driver_login") },
              onSelectAdmin = { viewModel.setRole("admin_login") },
              onOpenCleaner = { viewModel.openClearTesterDataDialog() }
            )
          }

          "driver_login" -> {
            DriverLoginScreen(
              drivers = drivers,
              onLogin = { driver, pin ->
                viewModel.loginDriver(driver, pin)
              },
              onBack = { viewModel.setRole(null) }
            )
          }

          "admin_login" -> {
            AdminLoginScreen(
              onLogin = { passcode ->
                viewModel.loginAdmin(passcode)
              },
              onBack = { viewModel.setRole(null) }
            )
          }

          "driver" -> {
            activeDriver?.let { driver ->
              DriverPortalScreen(
                driver = driver,
                isOnline = isOnline,
                queueItems = syncQueue,
                activeTrip = activeTrip,
                allTrips = trips,
                extraTrips = extraTrips,
                expenses = expenses,
                buses = buses,
                selectedTab = selectedDriverTab,
                onTabSelected = { viewModel.setDriverTab(it) },
                onToggleOnline = { viewModel.toggleOnlineMode() },
                onSyncNow = { viewModel.syncAllQueue() },
                onStartShift = { bus, source, odo ->
                  viewModel.startShift(bus, source, odo)
                },
                onRecordStage = { key, title, odo, bitmap, lat, lng ->
                  viewModel.recordStageWithWatermark(key, title, odo, bitmap, lat, lng)
                },
                onFinishShift = { endOdo ->
                  viewModel.finishShift(endOdo)
                },
                onLogExtraTrip = { startKm, endKm, note, time ->
                  viewModel.logExtraTrip(startKm, endKm, note, time)
                },
                onLogExpense = { type, mode, liters, price, amount, note, bitmap ->
                  viewModel.logExpense(type, mode, liters, price, amount, note, bitmap)
                },
                onLogout = { viewModel.logout() }
              )
            }
          }

          "admin" -> {
            AdminPortalScreen(
              drivers = drivers,
              trips = trips,
              extraTrips = extraTrips,
              expenses = expenses,
              buses = buses,
              queueItems = syncQueue,
              isOnline = isOnline,
              selectedTab = selectedAdminTab,
              onTabSelected = { viewModel.setAdminTab(it) },
              onToggleOnline = { viewModel.toggleOnlineMode() },
              onToggleDriverStatus = { viewModel.toggleDriverStatus(it) },
              onUpdateDriverPin = { id, pin -> viewModel.updateDriverPin(id, pin) },
              onRandomizePin = { viewModel.randomizeDriverPin(it) },
              onReassignBus = { id, bus -> viewModel.reassignBus(id, bus) },
              onAddDriver = { name, id, bus, phone, pin ->
                viewModel.addDriver(name, id, bus, phone, pin)
              },
              onAddBus = { viewModel.addBus(it) },
              onClearTesterData = { s, e, ex, q ->
                viewModel.clearAllTesterData(s, e, ex, q)
              },
              onSeedSampleData = { viewModel.seedSampleData() },
              onResetFactory = { viewModel.resetToCleanFactoryState() },
              onLogout = { viewModel.logout() }
            )
          }
        }
      }

      // Quick Clear Tester Data Dialog (available globally or from launcher/admin)
      if (showClearDialog) {
        ClearTesterDataDialog(
          tripsCount = trips.size,
          extraTripsCount = extraTrips.size,
          expensesCount = expenses.size,
          queueCount = syncQueue.size,
          onClearSelected = { s, e, ex, q ->
            viewModel.clearAllTesterData(s, e, ex, q)
          },
          onResetFactory = { viewModel.resetToCleanFactoryState() },
          onSeedSampleData = { viewModel.seedSampleData() },
          onDismiss = { viewModel.closeClearTesterDataDialog() }
        )
      }
    }
  }
}
