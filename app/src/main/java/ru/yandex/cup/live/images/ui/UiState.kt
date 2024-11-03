package ru.yandex.cup.live.images.ui

import kotlinx.coroutines.flow.StateFlow

data class UiState(
    val layer: StateFlow<UiLayer>,
    val instrument: StateFlow<UiInstrument>,
    val color: StateFlow<UiColor>,
    val strokeWidth: StateFlow<UiStrokeWidth>,
    val popupState: StateFlow<UiPopupState>,
)
