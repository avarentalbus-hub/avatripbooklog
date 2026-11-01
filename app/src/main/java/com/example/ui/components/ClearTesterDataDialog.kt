package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Red400
import com.example.ui.theme.Red500
import com.example.ui.theme.Sky400
import com.example.ui.theme.Sky500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun ClearTesterDataDialog(
  tripsCount: Int,
  extraTripsCount: Int,
  expensesCount: Int,
  queueCount: Int,
  onClearSelected: (clearShifts: Boolean, clearExtras: Boolean, clearExpenses: Boolean, clearQueue: Boolean) -> Unit,
  onResetFactory: () -> Unit,
  onSeedSampleData: () -> Unit,
  onDismiss: () -> Unit
) {
  var clearShifts by remember { mutableStateOf(true) }
  var clearExtras by remember { mutableStateOf(true) }
  var clearExpenses by remember { mutableStateOf(true) }
  var clearQueue by remember { mutableStateOf(true) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Slate900),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Slate800, RoundedCornerShape(20.dp))
        .testTag("clear_tester_data_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Header
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .background(Red500.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
              .border(1.dp, Red500.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CleaningServices,
              contentDescription = null,
              tint = Red400,
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Clear Tester Data",
              color = Color.White,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Easily reset fleet logs to a fresh state",
              color = Slate400,
              fontSize = 12.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats summary chips
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          StatChip(label = "Shifts", count = tripsCount, modifier = Modifier.weight(1f))
          StatChip(label = "Extras", count = extraTripsCount, modifier = Modifier.weight(1f))
          StatChip(label = "Expenses", count = expensesCount, modifier = Modifier.weight(1f))
          StatChip(label = "Queue", count = queueCount, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Select Data Categories to Purge:",
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Checkboxes
        TesterCheckboxItem(
          title = "Shift Logs & Checkpoints",
          subtitle = "$tripsCount records logged",
          checked = clearShifts,
          onCheckedChange = { clearShifts = it }
        )
        TesterCheckboxItem(
          title = "Extra / Inter-City Routes",
          subtitle = "$extraTripsCount records logged",
          checked = clearExtras,
          onCheckedChange = { clearExtras = it }
        )
        TesterCheckboxItem(
          title = "Fuel Slips & Expenses",
          subtitle = "$expensesCount receipts & slips",
          checked = clearExpenses,
          onCheckedChange = { clearExpenses = it }
        )
        TesterCheckboxItem(
          title = "Pending Offline Sync Queue",
          subtitle = "$queueCount queued uploads",
          checked = clearQueue,
          onCheckedChange = { clearQueue = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(color = Slate800, thickness = 1.dp)

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Button(
          onClick = {
            onClearSelected(clearShifts, clearExtras, clearExpenses, clearQueue)
          },
          enabled = clearShifts || clearExtras || clearExpenses || clearQueue,
          colors = ButtonDefaults.buttonColors(
            containerColor = Red500,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("confirm_clear_tester_data_button")
        ) {
          Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Clear Selected Tester Data", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedButton(
            onClick = onSeedSampleData,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Sky400),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Slate700)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("load_sample_data_button")
          ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Load Samples", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }

          OutlinedButton(
            onClick = onResetFactory,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber400),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Slate700)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("factory_reset_button")
          ) {
            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Factory Reset", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(
            containerColor = Slate800,
            contentColor = Slate400
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
        ) {
          Text(text = "Cancel", fontSize = 13.sp)
        }
      }
    }
  }
}

@Composable
private fun StatChip(label: String, count: Int, modifier: Modifier = Modifier) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .background(Slate950, RoundedCornerShape(8.dp))
      .border(1.dp, Slate800, RoundedCornerShape(8.dp))
      .padding(vertical = 8.dp)
  ) {
    Text(
      text = "$count",
      color = if (count > 0) Sky400 else Slate400,
      fontSize = 15.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = label,
      color = Slate400,
      fontSize = 10.sp
    )
  }
}

@Composable
private fun TesterCheckboxItem(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
      .background(Slate950, RoundedCornerShape(8.dp))
      .border(1.dp, Slate800, RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = CheckboxDefaults.colors(
        checkedColor = Red500,
        uncheckedColor = Slate700,
        checkmarkColor = Color.White
      ),
      modifier = Modifier.size(28.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = subtitle,
        color = Slate400,
        fontSize = 10.sp
      )
    }
  }
}
