package ru.yandex.cup.live.images.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import ru.yandex.cup.live.images.domain.color.Color

/**
 * @author Сергей Стилик on 30.10.2024
 */
class Canvas(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
    defStyleRes,
) {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    init {
        setWillNotDraw(false)
    }

    private val pointerCoords1 = MotionEvent.PointerCoords()
    private val pointerCoords2 = MotionEvent.PointerCoords()

    private var distance: Float = 0f
    private var zoomStarted: Boolean = false

    private var globalZoom: Float = 1f
    private var globalZoomOffsetX: Float = 0f
    private var globalZoomOffsetY: Float = 0f

    private var pathStarted: Boolean = false
    private var path: Path = Path()

    private val paint: Paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 3f * context.resources.displayMetrics.density
        strokeCap = Paint.Cap.ROUND // TODO:SALAM BRUSH
        // strokeCap = Paint.Cap.SQUARE // TODO:SALAM pencil
        strokeJoin = Paint.Join.ROUND
    }

    private val drawQueue: ArrayDeque<Path> = ArrayDeque(256)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev == null) return super.dispatchTouchEvent(ev)

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "dispatchTouchEvent(): ACTION_DOWN; pointerCount=${ev.pointerCount}")
                zoomStarted = false
                if (ev.pointerCount == 1) {
                    ev.getPointerCoords(0, pointerCoords1)
                    pathStarted = true
                    Log.d(
                        TAG,
                        "dispatchTouchEvent(): ACTION_DOWN; path started! x=${pointerCoords1.x}; y=${pointerCoords1.y}"
                    )
                    path = Path().also {
                        it.moveTo(pointerCoords1.x * globalZoom, pointerCoords1.y * globalZoom)
                    }
                } else {
                    pathStarted = false
                }
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                Log.d(TAG, "dispatchTouchEvent(): ACTION_MOVE; pointerCount=${ev.pointerCount}")
                if (ev.pointerCount == 2) {
                    ev.getPointerCoords(0, pointerCoords1)
                    Log.d(
                        TAG,
                        "dispatchTouchEvent(): ACTION_MOVE; pointer1: x=${pointerCoords1.x}; y=${pointerCoords1.y}"
                    )
                    ev.getPointerCoords(1, pointerCoords2)
                    Log.d(
                        TAG,
                        "dispatchTouchEvent(): ACTION_MOVE; pointer2: x=${pointerCoords2.x}; y=${pointerCoords2.y}"
                    )
                    val diffX2 = (pointerCoords1.x - pointerCoords2.x) * (pointerCoords1.x - pointerCoords2.x)
                    val diffY2 = (pointerCoords1.y - pointerCoords2.y) * (pointerCoords1.y - pointerCoords2.y)
                    val newDistance = kotlin.math.sqrt(diffX2 + diffY2)
                    Log.d(TAG, "dispatchTouchEvent(): ACTION_MOVE; distance=$newDistance")
                    if (zoomStarted) {
                        val zoom = newDistance / distance

                        // TODO:SALAM !!!
                        // ПОСЛЕ КАЖДОГО ЗУМА БРАТЬ ТЕКУЩИЕ РАЗМЕРЫ CANVAS
                        // КАЖДУЮ ТОЧКУ ВЫЧИСЛЯТЬ В ОТНОСИТЕЛЬНЫХ ТОЧКАХ
                        // ПЕРЕВОДИТЬ ОТНОСИТЕЛЬНЫЕ ТОЧКИ НА РЕАЛЬНЫЕ
                        // ПРИ ОТРИСОВКЕ ОТРИСОВЫВАТЬ ТОЛЬКО РЕАЛЬНЫЕ ТОЧКИ, НО ТОЛЬКО В ВИДИМОЙ ЗОНЕ, С УЧЁТОМ МАСШТАБА


                        // TODO:SALAM Идея! Нужно запомнить центр зума и учесть его как zoomOffsetCoord
                        // При добавлении каждой точки добавлять её offset: x * globalZoom + zoomOffsetX
                        val zoomX = (pointerCoords1.x + pointerCoords2.x) / 2f
                        val zoomY = (pointerCoords1.y + pointerCoords2.y) / 2f

                        Log.d(TAG, "dispatchTouchEvent(): ACTION_MOVE; zoom=$zoom")
                        // TODO:SALAM send zoom
                        globalZoom = globalZoom * zoom
                    }
                    distance = newDistance
                    zoomStarted = true
                } else {
                    zoomStarted = false
                }
                if (ev.pointerCount == 1 && pathStarted) {
                    ev.getPointerCoords(0, pointerCoords1)
                    Log.d(
                        TAG,
                        "dispatchTouchEvent(): ACTION_MOVE; path added! x=${pointerCoords1.x}; y=${pointerCoords1.y}"
                    )
                    path.lineTo(pointerCoords1.x * globalZoom, pointerCoords1.y * globalZoom)
                } else {
                    pathStarted = false
                }

                invalidate()
                return true
            }

            MotionEvent.ACTION_UP -> {
                Log.d(TAG, "dispatchTouchEvent(): ACTION_UP; pointerCount=${ev.pointerCount}")
                // (0 until ev.pointerCount).forEach { index ->
                //     ev.getPointerCoords(index, pointerCoords)
                //     Log.d(TAG, "dispatchTouchEvent(): ACTION_UP; pointer[$index]: x=${pointerCoords.x}; y=${pointerCoords.y}")
                // }
                if (pathStarted && ev.pointerCount == 1) {
                    ev.getPointerCoords(0, pointerCoords1)
                    Log.d(
                        TAG,
                        "dispatchTouchEvent(): ACTION_UP; path complete! x=${pointerCoords1.x}; y=${pointerCoords1.y}"
                    )
                    path.lineTo(pointerCoords1.x * globalZoom, pointerCoords1.y * globalZoom)
                    // TODO:SALAM draw path
                    drawQueue.addLast(path)
                }
                pathStarted = false
                zoomStarted = false
                invalidate()
                return true
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun onDraw(canvas: Canvas) {
        Log.d(TAG, "onDraw()")
        super.onDraw(canvas)
        for (p in drawQueue) {
            Log.d(TAG, "onDraw(): $p; isEmpty=${p.isEmpty};")
            canvas.drawPath(p, paint)
        }
        if (pathStarted) {
            canvas.drawPath(path, paint)
        }
    }

    companion object {
        const val TAG = "Canvas"
    }
}
