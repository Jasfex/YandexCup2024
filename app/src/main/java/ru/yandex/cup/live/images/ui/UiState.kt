package ru.yandex.cup.live.images.ui

import kotlinx.coroutines.flow.StateFlow

data class UiState(
    val play: StateFlow<Boolean>,
    val layers: StateFlow<List<UiLayer>>,
    val instrument: StateFlow<UiInstrument>,
    val color: StateFlow<UiColor>,
    val strokeWidth: StateFlow<UiStrokeWidth>,
    val popupState: StateFlow<UiPopupState>,
)
