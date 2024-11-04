package ru.yandex.cup.live.images.view

import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import ru.yandex.cup.live.images.ui.UiColor
import ru.yandex.cup.live.images.ui.UiInstrument
import ru.yandex.cup.live.images.ui.UiStrokeWidth

object PaintUtils {

    fun createPaint(
        density: Float,
        instrument: UiInstrument,
        color: UiColor,
        strokeWidth: UiStrokeWidth,
    ): Paint {
        return when (instrument) {
            UiInstrument.PENCIL -> createPencilPaint(density, color, strokeWidth)
            UiInstrument.BRUSH -> createBrushPaint(density, color, strokeWidth)
            UiInstrument.ERASER -> createEraserPaint(density, strokeWidth)
            UiInstrument.FIGURES -> TODO()
            UiInstrument.EMPTY, UiInstrument.COLOR_PICKER, UiInstrument.PALETTE -> Paint()
        }
    }

    fun createPencilPaint(density: Float, color: UiColor, strokeWidth: UiStrokeWidth): Paint = Paint().also { p ->
        p.color = android.graphics.Color.argb(255, color.red, color.green, color.blue)
        p.alpha = color.alpha
        p.style = Paint.Style.STROKE
        p.strokeWidth = strokeWidth.dp * density
        p.strokeCap = Paint.Cap.SQUARE
        p.strokeJoin = Paint.Join.MITER
    }

    fun createBrushPaint(density: Float, color: UiColor, strokeWidth: UiStrokeWidth): Paint = Paint().also { p ->
        p.color = android.graphics.Color.argb(255, color.red, color.green, color.blue)
        p.alpha = color.alpha
        p.style = Paint.Style.STROKE
        p.strokeWidth = strokeWidth.dp * density
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
    }

    fun createEraserPaint(density: Float, strokeWidth: UiStrokeWidth): Paint = Paint().also { p ->
        p.color = android.graphics.Color.TRANSPARENT
        p.alpha = 255 // color.alpha
        p.style = Paint.Style.STROKE
        p.strokeWidth = strokeWidth.dp * density
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
        p.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }
}
