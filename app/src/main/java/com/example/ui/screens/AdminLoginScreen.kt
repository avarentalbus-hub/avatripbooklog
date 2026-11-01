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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.FleetRepository
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun AdminLoginScreen(
  onLogin: (passcode: String) -> Unit,
  onBack: () -> Unit
) {
  var passcodeInput by remember { mutableStateOf("") }

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
          .testTag("admin_login_back_button")
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
        text = "Fleet Operations Dispatch",
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
            .background(Color(0xFF7C3AED).copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFA78BFA).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = Color(0xFFA78BFA),
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Admin Passcode",
          color = Color.White,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Enter master passcode for fleet roster, audit logs & tester data cleaning tools",
          color = Slate400,
          fontSize = 12.sp,
          lineHeight = 16.sp,
          modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
        )

        // Passcode Autofill Card
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .background(Slate950, RoundedCornerShape(12.dp))
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Column {
            Text(
              text = "Default Passcode: admin123",
              color = Color.White,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Click autofill for instant access",
              color = Slate400,
              fontSize = 10.sp
            )
          }

          Button(
            onClick = { passcodeInput = FleetRepository.ADMIN_PASSCODE },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF7C3AED).copy(alpha = 0.25f),
              contentColor = Color(0xFFA78BFA)
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier
              .height(32.dp)
              .testTag("admin_passcode_autofill_button")
          ) {
            Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Autofill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input
        Text(
          text = "Master Passcode",
          color = Slate400,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
          value = passcodeInput,
          onValueChange = { passcodeInput = it },
          placeholder = { Text(text = "Enter passcode", color = Slate700) },
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(
            onDone = { onLogin(passcodeInput) }
          ),
          leadingIcon = {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFFA78BFA))
          },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Slate950,
            unfocusedContainerColor = Slate950,
            focusedBorderColor = Color(0xFFA78BFA),
            unfocusedBorderColor = Slate800,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_passcode_input")
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = { onLogin(passcodeInput) },
          enabled = passcodeInput.isNotBlank(),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF7C3AED),
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("admin_login_submit_button")
        ) {
          Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Unlock Control Center", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
