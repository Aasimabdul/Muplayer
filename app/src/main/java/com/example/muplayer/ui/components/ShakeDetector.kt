package com.example.muplayer.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {

    private var lastShakeTimestamp: Long = 0L

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

        if (gForce > 2.7f) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTimestamp >= 1200L) {
                lastShakeTimestamp = now
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun ShakeToSkipListener(
    isEnabled: Boolean,
    onShake: () -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(isEnabled) {
        if (!isEnabled) {
            onDispose { }
        } else {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val detector = ShakeDetector(onShake)

            if (sensorManager != null && accelerometer != null) {
                sensorManager.registerListener(detector, accelerometer, SensorManager.SENSOR_DELAY_UI)
            }

            onDispose {
                sensorManager?.unregisterListener(detector)
            }
        }
    }
}
