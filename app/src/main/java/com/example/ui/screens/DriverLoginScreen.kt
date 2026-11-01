package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Driver
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Sky400
import com.example.ui.theme.Sky500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverLoginScreen(
  drivers: List<Driver>,
  onLogin: (driver: Driver, pin: String) -> Unit,
  onBack: () -> Unit
) {
  val activeDrivers = remember(drivers) { drivers.filter { it.status == "active" } }
  var selectedDriver by remember(activeDrivers) { mutableStateOf(activeDrivers.firstOrNull()) }
  var pinInput by remember { mutableStateOf("") }
  var isDropdownExpanded by remember { mutableStateOf(false) }
  var showForgotPinDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Slate950)
      .padding(20.dp)
      .verticalScroll(rememberScrollState())
  ) {
    // Back navigation
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier
          .size(40.dp)
          .background(Slate900, RoundedCornerShape(12.dp))
          .border(1.dp, Slate800, RoundedCornerShape(12.dp))
          .testTag("driver_login_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = Slate400,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = "Driver Authentication",
        color = Color.White,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Slate900),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Slate800, RoundedCornerShape(24.dp))
    ) {
      Column(modifier = Modifier.padding(22.dp)) {
        Box(
          modifier = Modifier
            .size(54.dp)
            .background(Sky500.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .border(1.dp, Sky500.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = Sky400,
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Select Driver Profile",
          color = Color.White,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Choose your name and enter your 4-digit security PIN",
          color = Slate400,
          fontSize = 12.sp,
          modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
        )

        // Driver Picker Dropdown
        Text(
          text = "Driver Profile",
          color = Slate400,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(bottom = 6.dp)
        )

        ExposedDropdownMenuBox(
          expanded = isDropdownExpanded,
          onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = selectedDriver?.let { "${it.name} (${it.bus})" } ?: "Select Driver",
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
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
              .testTag("driver_profile_selector")
          )

          ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
            modifier = Modifier.background(Slate900).border(1.dp, Slate800)
          ) {
            activeDrivers.forEach { driver ->
              DropdownMenuItem(
                text = {
                  Column {
                    Text(
                      text = driver.name,
                      color = Color.White,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.SemiBold
                    )
                    Text(
                      text = "Bus: ${driver.bus} • ID: ${driver.id}",
                      color = Sky400,
                      fontSize = 11.sp,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                },
                onClick = {
                  selectedDriver = driver
                  isDropdownExpanded = false
                  pinInput = ""
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Selected driver details badge
        selectedDriver?.let { driver ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .background(Slate950, RoundedCornerShape(12.dp))
              .border(1.dp, Slate800, RoundedCornerShape(12.dp))
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.DirectionsBus,
              contentDescription = null,
              tint = Emerald400,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Assigned Vehicle: ${driver.bus}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "ID: ${driver.id} • Status: Active",
                color = Slate400,
                fontSize = 10.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))
        }

        // PIN Input
        Text(
          text = "Security PIN (4 digits)",
          color = Slate400,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
          value = pinInput,
          onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) pinInput = it },
          placeholder = { Text(text = "••••", color = Slate700) },
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(
            onDone = {
              selectedDriver?.let { onLogin(it, pinInput) }
            }
          ),
          leadingIcon = {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Sky400)
          },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Slate950,
            unfocusedContainerColor = Slate950,
            focusedBorderColor = Sky500,
            unfocusedBorderColor = Slate800,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("driver_pin_input")
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Login Button
        Button(
          onClick = {
            selectedDriver?.let { onLogin(it, pinInput) }
          },
          enabled = selectedDriver != null && pinInput.length >= 4,
          colors = ButtonDefaults.buttonColors(
            containerColor = Sky500,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("driver_login_submit_button")
        ) {
          Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Unlock Shift Portal", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Forgot PIN helper
        TextButton(
          onClick = { showForgotPinDialog = true },
          modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
          Text(
            text = "Forgot your PIN? Tap for Admin Help",
            color = Slate400,
            fontSize = 11.sp
          )
        }
      }
    }
  }

  // Forgot PIN dialog
  if (showForgotPinDialog) {
    AlertDialog(
      onDismissRequest = { showForgotPinDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Sky400)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Driver PIN Help", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column {
          Text(
            text = "For safety and audit compliance, driver PINs are managed directly by dispatch.\n\nAsk your fleet manager or administrator to open the Admin Control Center > Roster tab and reset or view your temporary PIN.",
            color = Slate400,
            fontSize = 12.sp,
            lineHeight = 16.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Default PIN for seed drivers: 1001 (Ahmed), 1002 (Mohammed), 1003 (Fatima).",
            color = Emerald400,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { showForgotPinDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = Sky500)
        ) {
          Text(text = "Understood")
        }
      },
      containerColor = Slate900,
      shape = RoundedCornerShape(16.dp)
    )
  }
}
