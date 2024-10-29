package ru.yandex.cup.live.images.domain.instument

import ru.yandex.cup.live.images.domain.color.Color
import ru.yandex.cup.live.images.domain.color.Colored

data class Square(
    override val color: Color,
    override val strokeWidth: StrokeWidth,
) : Instrument(), Colored, Figure