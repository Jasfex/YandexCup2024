package ru.yandex.cup.live.images

import android.util.Log
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.yandex.cup.live.images.domain.color.Color
import ru.yandex.cup.live.images.domain.instument.Brush
import ru.yandex.cup.live.images.domain.instument.Circle
import ru.yandex.cup.live.images.domain.instument.ColorPicker
import ru.yandex.cup.live.images.domain.instument.Eraser
import ru.yandex.cup.live.images.domain.instument.Figure
import ru.yandex.cup.live.images.domain.instument.Instrument
import ru.yandex.cup.live.images.domain.instument.Pencil
import ru.yandex.cup.live.images.domain.instument.STROKE_WIDTH_MAX
import ru.yandex.cup.live.images.domain.instument.Square
import ru.yandex.cup.live.images.domain.instument.StrokeWidth
import ru.yandex.cup.live.images.domain.instument.Triangle
import ru.yandex.cup.live.images.domain.instument.toStrokeWidth
import ru.yandex.cup.live.images.ui.ShowColorPicker
import ru.yandex.cup.live.images.ui.ShowStrokeWidthSeekBar
import ru.yandex.cup.live.images.ui.UiEvent

class MainViewModel : ViewModel() {

    private val instrumentFlowInternal: MutableStateFlow<Instrument?> = MutableStateFlow(null)
    val instrumentFlow: StateFlow<Instrument?> = instrumentFlowInternal.asStateFlow()

    private val eventsFlowInternal: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    val eventsFlow: SharedFlow<UiEvent> = eventsFlowInternal.asSharedFlow()

    // instruments

    private var pencil: Pencil = defaultPencil
    private var brush: Brush = defaultBrush
    private var eraser: Eraser = defaultEraser
    private var colorPicker: ColorPicker = defaultColorPicker

    fun onColorPickerDismissed() {
        instrumentFlowInternal.value = null
    }

    fun onColorPicked(alphaValue: Int, red: Int, green: Int, blue: Int) {
        val alpha = alphaValue.coerceIn(ALPHA_MIN, ALPHA_MAX)
        val color = Color(red = red, green = green, blue = blue)
        instrumentFlowInternal.value = colorPicker.copy(
            alpha = alpha,
            color = color,
        ).also {
            colorPicker = it
            pencil = pencil.copy(alpha = alpha, color = color)
            brush = brush.copy(alpha = alpha, color = color)
            // TODO:SALAM figure
        }
    }

    fun onColorPicked(color: Color) {
        instrumentFlowInternal.value = colorPicker.copy(color = color).also {
            colorPicker = it
            pencil = pencil.copy(color = color)
            brush = brush.copy(color = color)
            // TODO:SALAM figure
        }
    }

    // stroke width

    fun onStrokeWidthUpdated(instrument: Instrument, @IntRange(from = 0, to = 1) progress: Int) {
        val strokeWidth = progress.toStrokeWidth()
        instrumentFlowInternal.value = when (instrument) {
            is Pencil -> instrument.copy(strokeWidth = strokeWidth).also {
                pencil = it
            }

            is Brush -> instrument.copy(strokeWidth = strokeWidth).also {
                brush = it
            }

            is Eraser -> instrument.copy(strokeWidth = strokeWidth).also {
                eraser = it
            }

            is ColorPicker -> instrument
            is Figure -> when (val figure = instrument as Figure) {
                is Circle -> figure.copy(strokeWidth = strokeWidth)
                is Square -> figure.copy(strokeWidth = strokeWidth)
                is Triangle -> figure.copy(strokeWidth = strokeWidth)
            }
        }
    }

    // clicks

    fun onPencilClicked() {
        instrumentFlowInternal.value = if (instrumentFlowInternal.value is Pencil) {
            null
        } else {
            pencil
        }
    }

    fun onBrushClicked() {
        instrumentFlowInternal.value = if (instrumentFlowInternal.value is Brush) {
            null
        } else {
            brush
        }
    }

    fun onEraserClicked() {
        instrumentFlowInternal.value = if (instrumentFlowInternal.value is Eraser) {
            null
        } else {
            eraser
        }
    }

    fun onFiguresClicked() {
        TODO()
    }

    fun onColorPickerClicked() {
        instrumentFlowInternal.value = if (instrumentFlowInternal.value is ColorPicker) {
            null
        } else {
            viewModelScope.launch {
                eventsFlowInternal.emit(ShowColorPicker(colorPicker))
            }
            colorPicker
        }
    }

    // long clicks

    fun onViewLongClicked(viewId: Int): Boolean {
        viewModelScope.launch {
            val instrument = when (viewId) {
                R.id.pencil -> pencil
                R.id.brush -> brush
                R.id.eraser -> eraser
                else -> null
            }
            if (instrument != null) {
                instrumentFlowInternal.value = instrument
                eventsFlowInternal.emit(ShowStrokeWidthSeekBar(instrument))
            }
        }
        return true
    }

    companion object {
        const val TAG = "MainViewModel"

        private const val DEFAULT_ALPHA = 255
        private const val ALPHA_MIN = 0
        private const val ALPHA_MAX = 255

        private val defaultColor: Color = Color(red = 25, green = 118, blue = 210) // #1976D2

        // pencil
        private val defaultPencilStrokeWidth = StrokeWidth(dp = 3f)
        private val defaultPencil: Pencil = Pencil(
            alpha = DEFAULT_ALPHA,
            color = defaultColor,
            strokeWidth = defaultPencilStrokeWidth,
        )

        // brush
        private val defaultBrushStrokeWidth = StrokeWidth(dp = 8f)
        private val defaultBrush: Brush = Brush(
            alpha = DEFAULT_ALPHA,
            color = defaultColor,
            strokeWidth = defaultBrushStrokeWidth,
        )

        // eraser
        private val defaultEraserStrokeWidth = StrokeWidth(dp = 5f)
        private val defaultEraser: Eraser = Eraser(
            alpha = ALPHA_MAX,
            strokeWidth = defaultEraserStrokeWidth,
        )

        // color picker
        private val defaultColorPicker: ColorPicker = ColorPicker(
            alpha = DEFAULT_ALPHA,
            color = defaultColor,
            strokeWidth = StrokeWidth(dp = STROKE_WIDTH_MAX), // TODO:SALAM remove
        )
    }
}
