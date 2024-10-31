package ru.yandex.cup.live.images.domain.color

import androidx.annotation.IntRange

/**
 * @author Сергей Стилик on 31.10.2024
 */
interface Alpha {
    /** In range from 0 to 255. */
    abstract val alpha: Int
}
