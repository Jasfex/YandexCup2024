package ru.yandex.cup.live.images.domain

import ru.yandex.cup.live.images.domain.instument.Instrument

/**
 * @author Сергей Стилик on 31.10.2024
 */
interface Canvas {
    fun setInstrument(instrument: Instrument?)
    fun setActive(active: Boolean)
}
