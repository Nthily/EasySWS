package com.github.nthily.swsclient.ui.view

import android.os.Build
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/*
    Original Author: aakarshrestha https://github.com/aakarshrestha
 */

private const val MAX_VALUE = 100
private const val MIN_VALUE = 0

/**
 * Creates a [ComposeVerticalSliderState] that is remembered across compositions.
 */
@Composable
fun rememberComposeVerticalSliderState(): ComposeVerticalSliderState {
    return remember {
        ComposeVerticalSliderState()
    }
}

/**
 * [ComposeVerticalSliderState] maintains the state of [ComposeVerticalSlider].
 */
class ComposeVerticalSliderState {

    var isEnabled = mutableStateOf(true)
        internal set

    var adjustTop = mutableStateOf(0f)
        internal set

    var progressValue = mutableStateOf(0)
        internal set

    private fun updateAdjustTopValue(value: Float) {
        this.adjustTop.value = value
    }

    private fun updateProgressValue(value: Int) {
        this.progressValue.value = value
    }

    var globalRect: Rect? = null

    var motionId: Int? = null
    
    private var height: Int? = null

    fun update(progress: Int) {
        if (!isEnabled.value) return
        if (height == null) return

        updateProgressValue(progress)

        val adjustTop = calculateAdjustTopFromProgressValue(progress, height!!)
        updateAdjustTopValue(adjustTop)
    }

    /**
     * This method is executed when [MotionEvent] is [MotionEvent.ACTION_MOVE] and [MotionEvent.ACTION_UP]
     */
    internal fun updateOnTouch(
        motionEvent: MotionEvent,
        canvasHeight: Int
    ) {
        height = canvasHeight

        if (!isEnabled.value) return
        if (motionId == null) return

        var idx = motionEvent.findPointerIndex(motionId!!)
        if (idx == -1) return

        var y = motionEvent.getY(idx).roundToInt()
        updateAdjustTopValue(y.toFloat())

        val progress = calculateProgress(y.toFloat(), canvasHeight).coerceIn(MIN_VALUE, MAX_VALUE)
        updateProgressValue(progress)

    }

    /**
     * Outputs the progress value when slider is updated in vertical axis.
     */
    private fun calculateProgress(adjustTop: Float, canvasHeight: Int): Int {
        return MAX_VALUE - (adjustTop / canvasHeight).times(100).roundToInt()
    }

    /**
     * Calculate the y axis value based on progress value.
     */
    fun calculateAdjustTopFromProgressValue(progressValue: Int, canvasHeight: Int): Float {
        return (MAX_VALUE - progressValue).times(canvasHeight).div(100).toFloat()
    }
}

/**
 * [ComposeVerticalSlider] allows users to make selections from the range of values
 * by dragging the slider in vertical axis.
 *
 * The min value that is allowed to choose is 0 and max value is 100.
 *
 * @param state maintains the state of ComposeVerticalSlider.
 * @param progressValue current value of the Slider. It can be null or the value can be assigned to it.
 * @param enabled whether or not component is enabled and can we interacted with or not.
 * @param width width of the slider
 * @param height height of the slider
 * @param radius corner curves of the slider
 * @param trackColor that can be set to a desired color.
 * @param progressTrackColor that can be set to a desired color.
 * @param onProgressChanged lambda that is invoked when the slider value changes when [MotionEvent.ACTION_MOVE] is triggered.
 * @param onStopTrackingTouch lambda that is invoked when the slider value changes when [MotionEvent.ACTION_UP] is triggered.
 */

@ExperimentalComposeUiApi
@Composable
fun ComposeVerticalSlider(
    state: ComposeVerticalSliderState,
    progressValue: Int? = null,
    enabled: Boolean = true,
    width: Dp = 140.dp,
    height: Dp = 300.dp,
    radius: CornerRadius = CornerRadius(80f, 80f),
    trackColor: Color = Color.LightGray,
    progressTrackColor: Color = Color.Green,
    onProgressChanged: (Int) -> Unit,
    onStopTrackingTouch: (Int) -> Unit
) {

    val left = 0f
    val top = 0f
    var right by remember { mutableStateOf(0f) }
    var bottom by remember { mutableStateOf(0f) }

    var canvasHeight by remember { mutableStateOf(0) }

    val radiusX = radius.x
    val radiusY = radius.y

    val enabledState by rememberSaveable { state.isEnabled }
    var adjustTop by rememberSaveable { state.adjustTop }
    var progressValueData by rememberSaveable {

        if (progressValue != null) {
            state.progressValue.value = progressValue
            onProgressChanged(state.progressValue.value)
            onStopTrackingTouch(state.progressValue.value)
        }

        state.progressValue

    }

    val rect = Rect(left, top, right, bottom)
    val trackPaint = Paint().apply {
        color = trackColor
        isAntiAlias = true
        strokeWidth = 10f
    }
    val progressPaint = Paint().apply {
        color = if (enabledState) progressTrackColor else Color.Gray
        isAntiAlias = true
        strokeWidth = 10f
    }
    val path = Path()

    LaunchedEffect(enabledState) {
        state.isEnabled.value = enabled
    }

    Canvas(
        modifier = Modifier
            .width(width)
            .height(height)
            .pointerInteropFilter { motionEvent ->
                when(motionEvent.actionMasked) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_POINTER_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        if (enabledState) {
                            if (state.motionId == null) {
                                for (i in 0 until motionEvent.pointerCount) {
                                    if (state.globalRect?.contains(Offset(
                                            motionEvent.rawX, motionEvent.rawY)
                                        ) == true) {
                                        state.motionId = motionEvent.getPointerId(i)
                                        break
                                    }
                                }
                            }
                            state.updateOnTouch(motionEvent, canvasHeight)
                            adjustTop = state.adjustTop.value
                            progressValueData = state.progressValue.value
                            onProgressChanged(progressValueData)
                        }

                        enabledState
                    }
                    MotionEvent.ACTION_POINTER_UP -> {
                        if (enabledState && motionEvent.getPointerId(
                                motionEvent.actionIndex) == state.motionId) {
                            state.updateOnTouch(motionEvent, canvasHeight)
                            state.motionId = null
                            adjustTop = state.adjustTop.value
                            progressValueData = state.progressValue.value
                            onStopTrackingTouch(progressValueData)
                        }

                        enabledState
                    }
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        if (enabledState) {
                            state.updateOnTouch(motionEvent, canvasHeight)
                            state.motionId = null
                            adjustTop = state.adjustTop.value
                            progressValueData = state.progressValue.value
                            onStopTrackingTouch(progressValueData)
                        }

                        enabledState
                    }
                    else -> enabledState
                }
            }
            .onGloballyPositioned { coordinates ->
                state.globalRect = Rect(
                    coordinates.positionInWindow(),
                    Size(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
                )
            }
    ) {

        canvasHeight = size.height.roundToInt()

        right = size.width
        bottom = size.height

        val aCanvas = drawContext.canvas

        path.addRoundRect(roundRect = RoundRect(left, top, right, bottom, CornerRadius(x = radiusX, y = radiusY)) )
        aCanvas.clipPath(path = path, ClipOp.Intersect)

        aCanvas.drawRect(rect, trackPaint)

        if (rect.width > MIN_VALUE && rect.height > MIN_VALUE) {
            adjustTop = state.calculateAdjustTopFromProgressValue(progressValueData, canvasHeight)
            aCanvas.drawRect(left, adjustTop, right, bottom, progressPaint)
        }
    }
}