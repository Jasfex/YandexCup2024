package ru.yandex.cup.live.images.ui

interface PlayerSurface {
    fun play(layers: List<UiLayer>)
    fun stop()
}
