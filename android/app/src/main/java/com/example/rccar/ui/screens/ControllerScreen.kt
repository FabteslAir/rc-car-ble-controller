
package com.example.rccar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rccar.bluetooth.BleManager
import com.example.rccar.ui.components.*
import kotlinx.coroutines.delay

@Composable
fun ControllerScreen(bleManager: BleManager) {
    var throttle by remember { mutableStateOf(0) }
    var yaw by remember { mutableStateOf(0) }
    var pitch by remember { mutableStateOf(0) }
    var roll by remember { mutableStateOf(0) }
    var battery by remember { mutableStateOf(85) }
    var signalStrength by remember { mutableStateOf(90) }
    var isConnected by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        bleManager.onConnected = {
            isConnected = true
            errorMessage = ""
        }
        bleManager.onDisconnected = {
            isConnected = false
        }
        bleManager.onBatteryUpdate = {
            battery = it
        }
        bleManager.onError = {
            errorMessage = it
        }

        // Simuler les mises à jour du signal (en production, utiliser RSSI)
        while (true) {
            delay(1000)
            if (isConnected) {
                signalStrength = (70..95).random()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header avec infos
            HeaderBar(battery, signalStrength, isConnected)

            // Zone de contrôle principal
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Joystick gauche - Throttle/Yaw
                VirtualJoystick(
                    title = "Throttle/Yaw",
                    onMove = { x, y ->
                        throttle = y
                        yaw = x
                        bleManager.sendCommand(throttle, yaw, pitch, roll)
                    }
                )

                Spacer(modifier = Modifier.width(48.dp))

                // Joystick droit - Pitch/Roll
                VirtualJoystick(
                    title = "Pitch/Roll",
                    onMove = { x, y ->
                        roll = x
                        pitch = y
                        bleManager.sendCommand(throttle, yaw, pitch, roll)
                    }
                )
            }

            // Footer avec infos supplémentaires
            FooterBar(
                throttle = throttle,
                yaw = yaw,
                pitch = pitch,
                roll = roll
            )
        }

        // Affichage des erreurs
        if (errorMessage.isNotEmpty()) {
            ErrorSnackBar(errorMessage) {
                errorMessage = ""
            }
        }
    }
}
