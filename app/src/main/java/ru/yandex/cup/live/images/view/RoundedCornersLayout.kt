package ru.yandex.cup.live.images.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearancePathProvider
import ru.yandex.cup.live.images.R

/**
 * @author Сергей Стилик on 28.10.2024
 */
class RoundedCornersLayout(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    private val radius: Float
    private val paint: Paint
    private val rectF: RectF
    private val path: Path
    private val pathMeasure: PathMeasure
    private val shapeAppearancePathProvider: ShapeAppearancePathProvider
    private val model: ShapeAppearanceModel

    init {
        radius = context.resources.getDimension(R.dimen.liveimages_canvas_radius)
        paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        rectF = RectF()
        path = Path()
        pathMeasure = PathMeasure()
        shapeAppearancePathProvider = ShapeAppearancePathProvider()
        model = ShapeAppearanceModel.builder()
            .setAllCornerSizes(radius)
            .build()
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            super.dispatchDraw(canvas)
            canvas.drawDoubleRoundRect(rectF, 0f, 0f, rectF, radius, radius, paint)
        } else {
            val state = canvas.save()
            canvas.clipPath(path)
            super.dispatchDraw(canvas)
            canvas.restoreToCount(state)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            super.onDraw(canvas)
        } else {
            shapeAppearancePathProvider.calculatePath(
                model,
                1f,
                rectF,
                path,
            )
            pathMeasure.setPath(path, false)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rectF.set(0f, 0f, w.toFloat(), h.toFloat())
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            shapeAppearancePathProvider.calculatePath(
                model,
                1f,
                rectF,
                path,
            )
            pathMeasure.setPath(path, false)
        }
    }
}
