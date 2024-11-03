package ru.yandex.cup.live.images.domain

import ru.yandex.cup.live.images.ui.UiColor
import ru.yandex.cup.live.images.ui.UiInstrument

interface Canvas {
    fun setInstrument(instrument: UiInstrument)
    fun setColor(color: UiColor)
    fun setStrokeWidth(dp: Float)
    fun setActive(active: Boolean)
}
