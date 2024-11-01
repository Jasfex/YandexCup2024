package ru.yandex.cup.live.images.domain

import ru.yandex.cup.live.images.domain.instument.Instrument

interface Canvas {
    fun setInstrument(instrument: Instrument?)
    fun setActive(active: Boolean)
}
