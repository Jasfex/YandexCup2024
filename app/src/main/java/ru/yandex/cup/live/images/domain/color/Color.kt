package ru.yandex.cup.live.images.domain.color

data class Color(
    val argb: Int,
) {
    companion object {
        const val BLACK: Int = android.graphics.Color.BLACK
        const val WHITE: Int = android.graphics.Color.WHITE
    }
}
