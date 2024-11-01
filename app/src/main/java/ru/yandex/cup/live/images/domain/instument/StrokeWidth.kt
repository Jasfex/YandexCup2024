package ru.yandex.cup.live.images.domain.instument

const val STROKE_WIDTH_MIN = 1f
const val STROKE_WIDTH_MAX = 18f

data class StrokeWidth(
    val dp: Float,
)

fun StrokeWidth.toProgress(): Int {
    val k = 100f / (STROKE_WIDTH_MAX - STROKE_WIDTH_MIN)
    val b = 100f / (STROKE_WIDTH_MIN - STROKE_WIDTH_MAX)
    return (k * dp + b).toInt().coerceIn(0, 100)
}

fun Int.toStrokeWidth(): StrokeWidth {
    val b = STROKE_WIDTH_MIN
    val k = (STROKE_WIDTH_MAX * 1.5f - STROKE_WIDTH_MIN * 2f) / 150f
    val dp = (k * this + b).coerceIn(STROKE_WIDTH_MIN, STROKE_WIDTH_MAX)
    return StrokeWidth(dp)
}
