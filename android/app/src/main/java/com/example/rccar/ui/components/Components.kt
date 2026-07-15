
package com.example.rccar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HeaderBar(battery: Int, signal: Int, isConnected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2A2A2A))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "RC VOITURE",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            SignalIndicator(signal, isConnected)
            BatteryIndicator(battery)
            ConnectionDot(isConnected)
        }
    }
}

@Composable
fun SignalIndicator(signal: Int, isConnected: Boolean) {
    val color = when {
        !isConnected -> Color.Gray
        signal > 75 -> Color.Green
        signal > 50 -> Color(0xFFFFC107)
        else -> Color.Red
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Signal",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            getSignalBars(signal),
            color = color,
            fontSize = 10.sp
        )
        Text(
            "$signal%",
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BatteryIndicator(level: Int) {
    val color = when {
        level > 60 -> Color.Green
        level > 30 -> Color(0xFFFFC107)
        else -> Color.Red
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Batterie",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "$level%",
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ConnectionDot(isConnected: Boolean) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(
                if (isConnected) Color.Green else Color.Red
            )
    )
}

@Composable
fun VirtualJoystick(
    title: String,
    onMove: (Int, Int) -> Unit
) {
    var stickX by remember { mutableStateOf(0) }
    var stickY by remember { mutableStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.gap(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A3A3A))
                .border(3.dp, Color(0xFF00BCD4), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            stickX = (stickX + dragAmount.x.toInt()).coerceIn(-100, 100)
                            stickY = (stickY - dragAmount.y.toInt()).coerceIn(-100, 100)
                            onMove(stickX, stickY)
                        },
                        onDragEnd = {
                            stickX = 0
                            stickY = 0
                            onMove(0, 0)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00BCD4))
                    .offset(
                        x = (stickX / 2.5).dp,
                        y = (stickY / 2.5).dp
                    )
            )
        }

        Text(
            title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.gap(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "X: $stickX",
                color = Color(0xFF888888),
                fontSize = 12.sp
            )
            Text(
                "Y: $stickY",
                color = Color(0xFF888888),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun FooterBar(throttle: Int, yaw: Int, pitch: Int, roll: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2A2A2A))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        InfoItem("Mode", "Manuel")
        InfoItem("Throttle", "$throttle")
        InfoItem("Yaw", "$yaw")
        InfoItem("Status", "Actif")
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            color = Color(0xFF888888),
            fontSize = 10.sp
        )
        Text(
            value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ErrorSnackBar(message: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF44336))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    message,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "✕",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }
        }
    }
}

fun getSignalBars(signal: Int): String {
    return when {
        signal > 75 -> "████"
        signal > 50 -> "███░"
        signal > 25 -> "██░░"
        else -> "█░░░"
    }
}
