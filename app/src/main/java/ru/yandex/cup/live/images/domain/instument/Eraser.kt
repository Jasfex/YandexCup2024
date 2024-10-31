package ru.yandex.cup.live.images.domain.instument

import ru.yandex.cup.live.images.domain.color.Alpha

data class Eraser(
    override val alpha: Int = 255,
    override val strokeWidth: StrokeWidth,
) : Instrument(), Alpha
