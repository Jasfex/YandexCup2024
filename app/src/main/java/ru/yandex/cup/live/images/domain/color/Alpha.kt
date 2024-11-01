package ru.yandex.cup.live.images.domain.color

import androidx.annotation.IntRange

interface Alpha {
    /** In range from 0 to 255. */
    abstract val alpha: Int
}
