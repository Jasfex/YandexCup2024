package ru.yandex.cup.live.images.ui

interface Canvas {
    fun setInstrument(instrument: UiInstrument)
    fun setColor(color: UiColor)
    fun setStrokeWidth(strokeWidth: UiStrokeWidth)
    fun setLayer(layer: UiLayer)
    fun getLayer(): UiLayer?

    fun setActive(active: Boolean)
}
