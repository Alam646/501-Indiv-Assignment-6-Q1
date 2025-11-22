package com.example.indivassignment6q1

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.indivassignment6q1.ui.theme.IndivAssignment6Q1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IndivAssignment6Q1Theme {
                AltimeterScreen()
            }
        }
    }
}

@Composable
fun AltimeterScreen() {
    val context = LocalContext.current
    // State to hold the current pressure reading
    var pressure by remember { mutableFloatStateOf(0f) }
    var hasSensor by remember { mutableFloatStateOf(true) }

    // Step 1: Sensor Implementation
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(ComponentActivity.SENSOR_SERVICE) as SensorManager
        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        if (pressureSensor == null) {
            hasSensor = false
        }

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.sensor.type == Sensor.TYPE_PRESSURE) {
                        // Update the UI with the raw pressure value
                        pressure = it.values[0]
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No action needed for this assignment
            }
        }

        if (pressureSensor != null) {
            sensorManager.registerListener(sensorEventListener, pressureSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            if (pressureSensor != null) {
                sensorManager.unregisterListener(sensorEventListener)
            }
        }
    }

    // Basic UI to display the raw data
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sensor Test",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!hasSensor) {
                Text(
                    text = "No Pressure Sensor Detected.",
                    color = Color.Red
                )
            } else {
                Text(
                    text = "Current Pressure:",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$pressure hPa",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
