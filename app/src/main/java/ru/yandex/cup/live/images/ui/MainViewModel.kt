package ru.yandex.cup.live.images.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.yandex.cup.live.images.R
import ru.yandex.cup.live.images.domain.instument.STROKE_WIDTH_MAX
import ru.yandex.cup.live.images.domain.instument.STROKE_WIDTH_MIN

class MainViewModel : ViewModel() {

    private var layerIndex: Int = 0
    private val layers: MutableList<UiLayer> = mutableListOf()
    private val layerFlow: MutableStateFlow<UiLayer> = MutableStateFlow(UiLayer(layerIndex++, emptyList()))
    private val instrumentFlow: MutableStateFlow<UiInstrument> = MutableStateFlow(DEFAULT_INSTRUMENT)
    private val colorFlow: MutableStateFlow<UiColor> = MutableStateFlow(DEFAULT_COLOR)
    private val strokeWidthFlow: MutableStateFlow<UiStrokeWidth> = MutableStateFlow(DEFAULT_STROKE_WIDTH)
    private val popupStateFlow: MutableStateFlow<UiPopupState> = MutableStateFlow(UiPopupState.EMPTY)

    val uiState: UiState = UiState(
        layer = layerFlow,
        instrument = instrumentFlow,
        color = colorFlow,
        strokeWidth = strokeWidthFlow,
        popupState = popupStateFlow,
    )

    private var instrumentBeforeColorPicker: UiInstrument = UiInstrument.EMPTY

    init {
        instrumentFlow.onEach { instrument ->
            val popupState = popupStateFlow.value
            popupStateFlow.value = when (instrument) {
                UiInstrument.EMPTY -> UiPopupState.EMPTY
                UiInstrument.COLOR_PICKER -> UiPopupState.COLOR_PICKER
                UiInstrument.PALETTE -> UiPopupState.COLOR_PICKER_AND_PALETTE
                UiInstrument.PENCIL, UiInstrument.BRUSH, UiInstrument.ERASER, UiInstrument.FIGURES -> if (popupState == UiPopupState.STROKE_WIDTH) {
                    UiPopupState.STROKE_WIDTH
                } else {
                    UiPopupState.EMPTY
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onDeleteLayerClicked() {
        layers.removeLastOrNull()
        val topLayer = layers.lastOrNull()
        layerFlow.value = topLayer ?: UiLayer(layerIndex++, emptyList())
    }

    fun onAddLayerClicked(layer: UiLayer?) {
        if (layer != null) {
            val indexToUpdate = layers.indexOfLast { it.index == layer.index }
            if (indexToUpdate != -1) {
                layers[indexToUpdate] = layer
            } else {
                layers.add(layer)
            }
        }

        val newLayer = UiLayer(layerIndex++, emptyList())
        layers.add(newLayer)
        layerFlow.value = newLayer
    }

    fun onSaveLayer(layer: UiLayer?) {
        if (layer != null) {
            val indexToUpdate = layers.indexOfLast { it.index == layer.index }
            if (indexToUpdate != -1) {
                layers[indexToUpdate] = layer
            } else {
                layers.add(layer)
            }
            layerFlow.value = layer
        }
    }

    fun onInstrumentClicked(viewId: Int) {
        val instrument = instrumentFlow.value
        val newInstrument = when (viewId) {
            R.id.pencil -> if (instrument == UiInstrument.PENCIL) UiInstrument.EMPTY else UiInstrument.PENCIL
            R.id.brush -> if (instrument == UiInstrument.BRUSH) UiInstrument.EMPTY else UiInstrument.BRUSH
            R.id.eraser -> if (instrument == UiInstrument.ERASER) UiInstrument.EMPTY else UiInstrument.ERASER
            R.id.figures -> if (instrument == UiInstrument.FIGURES) UiInstrument.EMPTY else UiInstrument.FIGURES
            R.id.color_picker -> {
                if (instrument == UiInstrument.COLOR_PICKER || instrument == UiInstrument.PALETTE) {
                    UiInstrument.EMPTY
                } else {
                    if (instrument in DRAWING_INSTRUMENTS) {
                        instrumentBeforeColorPicker = instrument
                    } else {
                        instrumentBeforeColorPicker = UiInstrument.EMPTY
                    }
                    UiInstrument.COLOR_PICKER
                }
            }

            R.id.palette -> if (instrument == UiInstrument.PALETTE) UiInstrument.COLOR_PICKER else UiInstrument.PALETTE
            else -> null
        }
        if (newInstrument != instrument) {
            popupStateFlow.value = UiPopupState.EMPTY
        }
        if (newInstrument != null) {
            instrumentFlow.value = newInstrument
        }
    }

    fun onInstrumentLongClicked(viewId: Int): Boolean {
        val currentInstrument = instrumentFlow.value
        val instrumentToSelect = when (viewId) {
            R.id.pencil -> UiInstrument.PENCIL
            R.id.brush -> UiInstrument.BRUSH
            R.id.eraser -> UiInstrument.ERASER
            else -> null
        }
        if (instrumentToSelect != null && currentInstrument != instrumentToSelect) {
            onInstrumentClicked(viewId)
        }
        if (instrumentToSelect != null) {
            popupStateFlow.value = UiPopupState.STROKE_WIDTH
        }
        return instrumentToSelect != null
    }

    fun onStrokeWidthUpdated(dp: Float) {
        strokeWidthFlow.value = UiStrokeWidth(dp = dp.coerceIn(STROKE_WIDTH_MIN, STROKE_WIDTH_MAX))
    }

    fun onColorUpdated(alpha: Int, red: Int, green: Int, blue: Int) {
        colorFlow.value = UiColor(
            alpha = alpha.coerceIn(0, 255),
            red = red.coerceIn(0, 255),
            green = green.coerceIn(0, 255),
            blue = blue.coerceIn(0, 255),
        )
    }

    fun onDismissPopupClicked() {
        popupStateFlow.value = UiPopupState.EMPTY
        val instrument = instrumentFlow.value
        if (instrument == UiInstrument.COLOR_PICKER || instrument == UiInstrument.PALETTE) {
            instrumentFlow.value = instrumentBeforeColorPicker
        }
    }

    companion object {
        const val TAG = "MainViewModel"
        private val DEFAULT_INSTRUMENT = UiInstrument.BRUSH
        private val DEFAULT_COLOR = UiColor(alpha = 255, red = 25, green = 118, blue = 210)
        private val DEFAULT_STROKE_WIDTH = UiStrokeWidth(dp = 8f)
        private val DRAWING_INSTRUMENTS = listOf(UiInstrument.PENCIL, UiInstrument.BRUSH, UiInstrument.FIGURES)
    }
}
