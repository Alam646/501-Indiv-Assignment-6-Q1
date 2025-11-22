package com.example.indivassignment6q1

import android.content.Context
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.indivassignment6q1.ui.theme.IndivAssignment6Q1Theme
import java.util.Locale
import kotlin.math.pow

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
    
    // State variables
    var pressure by remember { mutableFloatStateOf(1013.25f) } // Default P0
    var altitude by remember { mutableFloatStateOf(0f) }
    var hasSensor by remember { mutableStateOf(true) } // Fixed: Use mutableStateOf for Boolean

    // Step 1 & 2: Sensor & Calculation
    DisposableEffect(Unit) {
        // Fixed: Use Context.SENSOR_SERVICE
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        if (pressureSensor == null) {
            hasSensor = false
        }

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.sensor.type == Sensor.TYPE_PRESSURE) {
                        val currentPressure = it.values[0]
                        pressure = currentPressure

                        // Formula: h = 44330 * (1 - (P / P0)^(1/5.255))
                        // P0 = 1013.25
                        val p0 = 1013.25
                        val exponent = 1.0 / 5.255
                        val base = (currentPressure / p0).toDouble() // Redundant cast removed by logic below if needed, but safe to keep for clarity or remove if warned. 
                        // Note: Kotlin math pow expects Double.
                        
                        // Calculate altitude
                        val h = 44330.0 * (1.0 - base.pow(exponent))
                        altitude = h.toFloat()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
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

    // Dynamic Background Color Logic
    // Map altitude 0m -> 10,000m to Sky Blue -> Dark Night Blue
    val maxAltitude = 10000f
    val fraction = (altitude / maxAltitude).coerceIn(0f, 1f)
    
    val seaLevelColor = Color(0xFF87CEEB) // Light Sky Blue
    val highAltitudeColor = Color(0xFF000033) // Very Dark Blue
    
    val backgroundColor = lerp(seaLevelColor, highAltitudeColor, fraction)
    
    // Adjust text color for contrast (Dark text on light background, White text on dark)
    val textColor = if (fraction > 0.4f) Color.White else Color.Black

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Altimeter",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!hasSensor) {
                Text(
                    text = "No Pressure Sensor Detected.",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text(
                    // Fixed: Added Locale.US to format
                    text = "Pressure: ${String.format(Locale.US, "%.2f", pressure)} hPa",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Altitude",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor.copy(alpha = 0.8f)
                )
                Text(
                    // Fixed: Added Locale.US to format
                    text = "${String.format(Locale.US, "%.2f", altitude)} m",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}
