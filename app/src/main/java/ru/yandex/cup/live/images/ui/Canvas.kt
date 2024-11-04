package ru.yandex.cup.live.images.ui

import kotlinx.coroutines.flow.StateFlow

interface Canvas {
    fun setInstrument(instrument: UiInstrument)
    fun setColor(color: UiColor)
    fun setStrokeWidth(strokeWidth: UiStrokeWidth)
    fun setLayer(layer: UiLayer?)
    fun setPrevLayer(prevLayer: UiLayer?)
    fun getLayer(): UiLayer

    fun undo()
    fun redo()
    fun getHistoryActionFlow(): StateFlow<UiHistoryAction>

    fun setActive(active: Boolean)
}
