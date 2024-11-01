package ru.yandex.cup.live.images.ui

import ru.yandex.cup.live.images.domain.instument.ColorPicker
import ru.yandex.cup.live.images.domain.instument.Instrument

sealed interface UiEvent

data class ShowStrokeWidthSeekBar(val instrument: Instrument) : UiEvent

data class ShowColorPicker(val colorPicker: ColorPicker) : UiEvent
