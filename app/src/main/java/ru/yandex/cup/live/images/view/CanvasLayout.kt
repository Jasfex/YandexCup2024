package ru.yandex.cup.live.images.view

import android.content.Context
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Xfermode
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import ru.yandex.cup.live.images.ui.UiColor
import ru.yandex.cup.live.images.ui.UiInstrument

class CanvasLayout(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
    defStyleRes,
), ru.yandex.cup.live.images.domain.Canvas {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private var active: Boolean = false
    private var instrument: UiInstrument = UiInstrument.EMPTY
    private var color: UiColor = UiColor(255, 0, 0, 0)
    private var strokeWidth: Float = 3f * resources.displayMetrics.density

    override fun setActive(active: Boolean) {
        this.active = active
    }

    override fun setInstrument(instrument: UiInstrument) {
        val newInstrument = when (instrument) {
            UiInstrument.EMPTY,UiInstrument.PENCIL, UiInstrument.BRUSH, UiInstrument.ERASER -> instrument
            UiInstrument.FIGURES -> TODO()
            UiInstrument.COLOR_PICKER, UiInstrument.PALETTE -> UiInstrument.EMPTY
        }
        this.instrument = newInstrument
        updatePaint()
    }


    override fun setColor(color: UiColor) {
        this.color = color
        updatePaint()
    }

    override fun setStrokeWidth(dp: Float) {
        strokeWidth = dp * resources.displayMetrics.density
        updatePaint()
    }

    private fun updatePaint() {
        paint = when (instrument) {
            UiInstrument.PENCIL -> createPencilPaint()
            UiInstrument.BRUSH -> createBrushPaint()
            UiInstrument.ERASER -> createEraserPaint()
            UiInstrument.FIGURES -> TODO()
            UiInstrument.EMPTY, UiInstrument.COLOR_PICKER, UiInstrument.PALETTE -> paint
        }
    }

    // canvas size
    private val canvasRect = Rect()
    private val canvasRectF = RectF()

    // currently visible area
    private val zoomedRect = Rect()
    private val zoomedRectF = RectF()

    // gestures & drawing
    private val pointerCoords1 = MotionEvent.PointerCoords()
    private val pointerCoords2 = MotionEvent.PointerCoords()

    private var gestureZoomDetected: Boolean = false
    private var distanceBetweenPointers: Float = 0f

    private var gestureDrawDetected: Boolean = false

    private var paint = Paint()

    // legacy
    private var drawingPath: Path = Path()

    data class DrawingEntry(
        val path: Path,
        val paint: Paint,
    )

    private val drawQueue: ArrayDeque<DrawingEntry> = ArrayDeque(256)

    private fun createPencilPaint(): Paint = Paint().also { p ->
        p.color = android.graphics.Color.argb(255, color.red, color.green, color.blue)
        p.alpha = color.alpha
        p.style = Paint.Style.STROKE
        p.strokeWidth = strokeWidth
        p.strokeCap = Paint.Cap.SQUARE
        p.strokeJoin = Paint.Join.ROUND
    }

    private fun createBrushPaint(): Paint = Paint().also { p ->
        p.color = android.graphics.Color.argb(255, color.red, color.green, color.blue)
        p.alpha = color.alpha
        p.style = Paint.Style.STROKE
        p.strokeWidth = strokeWidth
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
    }

    private fun createEraserPaint(): Paint = Paint().also { p ->
        p.color = android.graphics.Color.TRANSPARENT
        p.alpha = 255 // color.alpha
        p.style = Paint.Style.STROKE
        p.strokeWidth = strokeWidth
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
        p.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }

    // ВАЖНО! Предполагаем, что менять размеры экрана запрещено!
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasRect.right = w
        canvasRect.bottom = h
        zoomedRect.set(canvasRect)

        canvasRectF.right = w.toFloat()
        canvasRectF.bottom = w.toFloat()
        zoomedRectF.set(canvasRectF)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev == null || !active) return super.dispatchTouchEvent(ev)

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "dispatchTouchEvent(): ACTION_DOWN; pointerCount=${ev.pointerCount}")
                gestureZoomDetected = false
                if (ev.pointerCount == 1 && instrument != UiInstrument.EMPTY) {
                    ev.getPointerCoords(0, pointerCoords1)
                    gestureDrawDetected = true
                    Log.d(TAG, "dispatchTouchEvent(): ACTION_DOWN; path started! x=${pointerCoords1.x}; y=${pointerCoords1.y}")
                    drawingPath = Path().also {
                        it.moveTo(pointerCoords1.x, pointerCoords1.y)
                    }
                } else {
                    gestureDrawDetected = false
                }
                invalidateInternal()
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
                    if (gestureZoomDetected) {
                        val zoom = newDistance / distanceBetweenPointers

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
                    }
                    distanceBetweenPointers = newDistance
                    gestureZoomDetected = true
                } else {
                    gestureZoomDetected = false
                }
                if (ev.pointerCount == 1 && gestureDrawDetected) {
                    ev.getPointerCoords(0, pointerCoords1)
                    Log.d(
                        TAG,
                        "dispatchTouchEvent(): ACTION_MOVE; path added! x=${pointerCoords1.x}; y=${pointerCoords1.y}"
                    )
                    drawingPath.lineTo(pointerCoords1.x, pointerCoords1.y)
                } else {
                    gestureDrawDetected = false
                }

                invalidateInternal()
                return true
            }

            MotionEvent.ACTION_UP -> {
                Log.d(TAG, "dispatchTouchEvent(): ACTION_UP; pointerCount=${ev.pointerCount}")
                // (0 until ev.pointerCount).forEach { index ->
                //     ev.getPointerCoords(index, pointerCoords)
                //     Log.d(TAG, "dispatchTouchEvent(): ACTION_UP; pointer[$index]: x=${pointerCoords.x}; y=${pointerCoords.y}")
                // }
                if (gestureDrawDetected && ev.pointerCount == 1) {
                    ev.getPointerCoords(0, pointerCoords1)
                    Log.d(
                        TAG,
                        "dispatchTouchEvent(): ACTION_UP; path complete! x=${pointerCoords1.x}; y=${pointerCoords1.y}"
                    )
                    drawingPath.lineTo(pointerCoords1.x, pointerCoords1.y)
                    // TODO:SALAM draw path
                    drawQueue.addLast(DrawingEntry(drawingPath, paint))
                }
                gestureDrawDetected = false
                gestureZoomDetected = false
                invalidateInternal()
                return true
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun onDraw(canvas: Canvas) {
        Log.d(TAG, "onDraw()")
        super.onDraw(canvas)

        for ((_path, _paint) in drawQueue) {
            canvas.drawPath(_path, _paint)
        }
        // drawQueue.lastOrNull()?.let {
        //     canvas.drawPath(it.path, eraserPaint)
        // }
        if (gestureDrawDetected) {
            canvas.drawPath(drawingPath, paint)
        }
    }

    private fun invalidateInternal() {
        invalidate()
        invalidateParent()
    }

    private fun invalidateParent() {
        (parent as? View)?.invalidate()
    }

    companion object {
        const val TAG = "CanvasLayout"
    }
}
