package ru.yandex.cup.live.images.domain.color

import androidx.annotation.IntRange

data class Color(
    @IntRange(from = 0, to = 255) val red: Int,
    @IntRange(from = 0, to = 255) val green: Int,
    @IntRange(from = 0, to = 255) val blue: Int,
) {
    companion object {
        val WHITE = Color(255, 255, 255)
        val RED = Color(255, 61, 0)
        val BLACK = Color(0, 0, 0)
        val BLUE = Color(25, 118, 210)
    }
}
