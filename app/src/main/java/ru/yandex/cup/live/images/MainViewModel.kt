package ru.yandex.cup.live.images

import androidx.annotation.IntRange
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.yandex.cup.live.images.domain.color.Alpha
import ru.yandex.cup.live.images.domain.color.Color
import ru.yandex.cup.live.images.domain.instument.Brush
import ru.yandex.cup.live.images.domain.instument.Eraser
import ru.yandex.cup.live.images.domain.instument.Instrument
import ru.yandex.cup.live.images.domain.instument.Pencil
import ru.yandex.cup.live.images.domain.instument.StrokeWidth
import java.time.chrono.Era

class MainViewModel : ViewModel() {

    private val instrumentFlowInternal: MutableStateFlow<Instrument?> = MutableStateFlow(null)
    val instrumentFlow: StateFlow<Instrument?> get() = instrumentFlowInternal.asStateFlow()

    // color & alpha

    private var color: Color = defaultColor

    @IntRange(from = 0, to = 255)
    private var alpha: Int = DEFAULT_ALPHA

    // stroke width

    private var pencilStrokeWidth: StrokeWidth = defaultPencilStrokeWidth
    private var brushStrokeWidth: StrokeWidth = defaultBrushStrokeWidth
    private var eraserStrokeWidth: StrokeWidth = defaultEraserStrokeWidth

    // clicks

    fun onPencilClicked() {
        instrumentFlowInternal.value = if (instrumentFlowInternal.value is Pencil) {
            null
        } else {
            Pencil(
                alpha = alpha,
                color = color,
                strokeWidth = pencilStrokeWidth,
            )
        }
    }

    fun onBrushClicked() {
        instrumentFlowInternal.value = if (instrumentFlowInternal.value is Brush) {
            null
        } else {
            Brush(
                alpha = alpha,
                color = color,
                strokeWidth = brushStrokeWidth,
            )
        }
    }

    fun onEraserClicked() {
        instrumentFlowInternal.value = if (instrumentFlowInternal.value is Eraser) {
            null
        } else {
            Eraser(
                alpha = ALPHA_MAX,
                strokeWidth = eraserStrokeWidth,
            )
        }
    }

    fun onFiguresClicked() {
        TODO()
    }

    fun onColorPickerClicked() {
        TODO()
    }

    // long clicks

    fun onPencilLongClicked(): Boolean {
        TODO()
    }

    fun onBrushLongClicked(): Boolean {
        TODO()
    }

    fun onEraserLongClicked(): Boolean {
        TODO()
    }

    companion object {
        private const val DEFAULT_ALPHA = 255
        private const val ALPHA_MIN = 0
        private const val ALPHA_MAX = 255
        private val defaultColor: Color = Color(red = 25, green = 118, blue = 210) // #1976D2
        private val defaultPencilStrokeWidth = StrokeWidth(dp = 3f)
        private val defaultBrushStrokeWidth = StrokeWidth(dp = 8f)
        private val defaultEraserStrokeWidth = StrokeWidth(dp = 5f)
    }
}
