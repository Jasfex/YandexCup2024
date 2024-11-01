package ru.yandex.cup.live.images.ui

import ru.yandex.cup.live.images.domain.instument.Instrument

sealed interface UiEvent

data class ShowStrokeWidthSeekBar(val instrument: Instrument) : UiEvent
