package com.github.nthily.swsclient.components

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_GAME
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.github.nthily.swsclient.viewModel.ConsoleViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class SteeringSensor private constructor(
    context: Context
) : DefaultLifecycleObserver, SensorEventListener {

    var started = false
        private set

    private val _steeringFlow = MutableSharedFlow<SteeringEvent>(extraBufferCapacity = 1)

    val steeringFlow = _steeringFlow.asSharedFlow()

    private val _sensorManager = context.applicationContext.getSystemService(
        SENSOR_SERVICE) as SensorManager
    private val _steeringScope = CoroutineScope(Job() + Dispatchers.Default)

    private var _accelerometer: Sensor? = null
    private var _magnetometer: Sensor? = null
    private var _gamometer: Sensor? = null
    private var _supportedSensorLevel = SupportedSensorLevel.NONE

    private var _gravity = FloatArray(3)
    private var _geomagnetic = FloatArray(3)
    private var _rotation = FloatArray(3)
    private val _matrix = FloatArray(9)
    private val _orientation = FloatArray(3)

    init {
        _accelerometer = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        _magnetometer = _sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        _gamometer = _sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)

        if (_gamometer != null)
            _supportedSensorLevel = SupportedSensorLevel.FULL
        else if (_accelerometer != null && _magnetometer != null)
            _supportedSensorLevel = SupportedSensorLevel.MEDIUM
        else if (_accelerometer != null && _magnetometer == null)
            _supportedSensorLevel = SupportedSensorLevel.BASIC
    }

    fun bind(activity: ComponentActivity) {
        activity.lifecycle.addObserver(this)
    }

    fun start() {
        if(started) return

        started = true
        _start()
    }

    fun stop() {
        if(!started) return

        started = false
        _stop()
    }

    private fun _start() {
        _accelerometer?.let {
            _sensorManager.registerListener(this, it, SENSOR_DELAY_GAME)
        }
        _magnetometer?.let {
            _sensorManager.registerListener(this, it, SENSOR_DELAY_GAME)
        }
        _gamometer?.let {
            _sensorManager.registerListener(this, it, SENSOR_DELAY_GAME)
        }
    }

    private fun _stop() {
        _sensorManager.unregisterListener(this)
    }


    // DefaultLifecycleObserver

    override fun onCreate(owner: LifecycleOwner) {
        if(started) _start()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if(started) _stop()
    }

    override fun onResume(owner: LifecycleOwner) {
        if(started) _start()
    }

    override fun onPause(owner: LifecycleOwner) {
        if(started) _stop()
    }


    // SensorEventListener

    override fun onSensorChanged(event: SensorEvent?) {
        _steeringScope.launch {
            when (event?.sensor?.type) {
                Sensor.TYPE_ACCELEROMETER -> _gravity = event.values
                Sensor.TYPE_MAGNETIC_FIELD -> _geomagnetic = event.values
                Sensor.TYPE_GAME_ROTATION_VECTOR -> _rotation = event.values
            }
            val steering = when (_supportedSensorLevel) {
                SupportedSensorLevel.BASIC -> {
                    ((1 + (_gravity[1] / SensorManager.STANDARD_GRAVITY)) / 2)
                }
                SupportedSensorLevel.MEDIUM -> {
                    SensorManager.getRotationMatrix(
                        _matrix, null, _gravity, _geomagnetic
                    )
                    SensorManager.getOrientation(_matrix, _orientation)
                    (((Math.PI / 2) - _orientation[1]) / Math.PI).toFloat()
                }
                SupportedSensorLevel.FULL -> {
                    SensorManager.getRotationMatrixFromVector(_matrix, _rotation)
                    SensorManager.getOrientation(_matrix, _orientation)
                    (((Math.PI / 2) - _orientation[1]) / Math.PI).toFloat()
                }
                else -> 0.5F
            }.coerceIn(0.0F, 1.0F)
            _steeringFlow.emit(SteeringEvent(steering))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }


    companion object {

        private const val TAG = "SteeringSensor"

        @Volatile
        private var _instance: SteeringSensor? = null


        fun getInstance(): SteeringSensor? {
            return _instance
        }

        fun getInstance(context: Context): SteeringSensor {
            return _instance ?: synchronized(this) {
                val instance = SteeringSensor(context)
                _instance = instance
                instance
            }
        }


        sealed interface EventInfo
        object Event : EventInfo

        data class SteeringEvent(val steering: Float) : EventInfo


        enum class SupportedSensorLevel {
            NONE, BASIC, MEDIUM, FULL
        }

    }

}
