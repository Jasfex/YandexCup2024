package ru.yandex.cup.live.images.ui

data class UiPath(
    val instrument: UiInstrument,
    val color: UiColor,
    val strokeWidth: UiStrokeWidth,
    /** Triple(x: Float, y: Float, durationMs: Long) */
    val coords: List<Triple<Float, Float, Long>>,
)
