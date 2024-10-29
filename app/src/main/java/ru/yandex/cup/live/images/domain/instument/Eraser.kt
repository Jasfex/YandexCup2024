package ru.yandex.cup.live.images.domain.instument

data class Eraser(
    override val strokeWidth: StrokeWidth,
) : Instrument()
