package ru.yandex.cup.live.images.domain.color

import androidx.annotation.IntRange

data class Color(
    @IntRange(from = 0, to = 255) val red: Int,
    @IntRange(from = 0, to = 255) val green: Int,
    @IntRange(from = 0, to = 255) val blue: Int,
)
