package ru.yandex.cup.live.images.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.yandex.cup.live.images.ui.UiColor
import ru.yandex.cup.live.images.ui.UiHistoryAction
import ru.yandex.cup.live.images.ui.UiInstrument
import ru.yandex.cup.live.images.ui.UiLayer
import ru.yandex.cup.live.images.ui.UiPath
import ru.yandex.cup.live.images.ui.UiStrokeWidth
import java.util.LinkedList

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
), ru.yandex.cup.live.images.ui.Canvas {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private val historyActionFlow: MutableStateFlow<UiHistoryAction> = MutableStateFlow(UiHistoryAction(false, false))
    private var active: Boolean = false
    private var instrument: UiInstrument = UiInstrument.EMPTY
    private var color: UiColor = UiColor(255, 0, 0, 0)
    private var strokeWidth: UiStrokeWidth = UiStrokeWidth(dp = 3f)
    private var layerIndex: Long = -1L

    override fun setActive(active: Boolean) {
        this.active = active
    }

    override fun setInstrument(instrument: UiInstrument) {
        val newInstrument = when (instrument) {
            UiInstrument.EMPTY, UiInstrument.PENCIL, UiInstrument.BRUSH, UiInstrument.ERASER -> instrument
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

    override fun setStrokeWidth(strokeWidth: UiStrokeWidth) {
        this.strokeWidth = strokeWidth
        updatePaint()
    }

    private fun updatePaint() {
        paint = createPaint(instrument, color, strokeWidth)
    }

    override fun setLayer(layer: UiLayer?) {
        if (layer == null) {
            layerIndex = -1
            undoStack.clear()
            redoStack.clear()
            undoNativeStack.clear()
            redoNativeStack.clear()
            historyActionFlow.value = historyActionFlow.value.copy(undoStack.isNotEmpty(), redoStack.isNotEmpty())
        } else {
            layerIndex = layer.index
            val newUiPathQueue = layer.drawingQueue.map { uiPath ->
                TransitiveUiPath(
                    uiPath.instrument,
                    uiPath.color,
                    uiPath.strokeWidth,
                    LinkedList(uiPath.coords),
                )
            }
            undoStack.clear()
            redoStack.clear()
            undoStack.addAll(newUiPathQueue)
            historyActionFlow.value = historyActionFlow.value.copy(undoStack.isNotEmpty(), redoStack.isNotEmpty())
            val newNativePathQueue = layer.drawingQueue.map { uiPath ->
                val _path = Path()
                for (index in uiPath.coords.indices) {
                    val (x, y, _) = uiPath.coords[index]
                    if (index == 0) {
                        _path.moveTo(x, y)
                    } else {
                        _path.lineTo(x, y)
                    }
                }
                val _paint = createPaint(uiPath.instrument, uiPath.color, uiPath.strokeWidth)
                _path to _paint
            }
            undoNativeStack.clear()
            redoNativeStack.clear()
            undoNativeStack.addAll(newNativePathQueue)
        }
        invalidate()
    }

    override fun setPrevLayer(prevLayer: UiLayer?) {
        val newPrevNativePathQueue = prevLayer?.drawingQueue?.map { uiPath ->
            val _path = Path()
            for (index in uiPath.coords.indices) {
                val (x, y, _) = uiPath.coords[index]
                if (index == 0) {
                    _path.moveTo(x, y)
                } else {
                    _path.lineTo(x, y)
                }
            }
            val color = uiPath.color.copy(alpha = uiPath.color.alpha / 2)
            val _paint = createPaint(uiPath.instrument, color, uiPath.strokeWidth)
            _path to _paint
        }
        prevNativePathQueue.clear()
        if (newPrevNativePathQueue != null) {
            prevNativePathQueue.addAll(newPrevNativePathQueue)
        }
        invalidate()
    }

    override fun getLayer(): UiLayer {
        val queue = undoStack.map { transitiveUiPath ->
            UiPath(
                transitiveUiPath.instrument,
                transitiveUiPath.color,
                transitiveUiPath.strokeWidth,
                transitiveUiPath.coords.toList(),
            )
        }
        return UiLayer(layerIndex, queue)
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

    data class TransitiveUiPath(
        val instrument: UiInstrument,
        val color: UiColor,
        val strokeWidth: UiStrokeWidth,
        val coords: LinkedList<Triple<Float, Float, Long>>,
    )

    private var uiPath: TransitiveUiPath? = null
    private val undoStack: LinkedList<TransitiveUiPath> = LinkedList()
    private val redoStack: LinkedList<TransitiveUiPath> = LinkedList()

    private var nativePath: Path = Path()
    private val prevNativePathQueue: LinkedList<Pair<Path, Paint>> = LinkedList()
    private val undoNativeStack: LinkedList<Pair<Path, Paint>> = LinkedList()
    private val redoNativeStack: LinkedList<Pair<Path, Paint>> = LinkedList()

    private var pointTimestampMs: Long = 0L

    override fun undo() {
        val action = undoStack.removeLastOrNull()
        if (action != null) {
            redoStack.addLast(action)
        }
        val nativeAction = undoNativeStack.removeLastOrNull()
        if (nativeAction != null) {
            redoNativeStack.addLast(nativeAction)
        }
        historyActionFlow.value = historyActionFlow.value.copy(undoStack.isNotEmpty(), redoStack.isNotEmpty())
        invalidate()
    }

    override fun redo() {
        val action = redoStack.removeLastOrNull()
        if (action != null) {
            undoStack.addLast(action)
        }
        val nativeAction = redoNativeStack.removeLastOrNull()
        if (nativeAction != null) {
            undoNativeStack.addLast(nativeAction)
        }
        historyActionFlow.value = historyActionFlow.value.copy(undoStack.isNotEmpty(), redoStack.isNotEmpty())
        invalidate()
    }

    override fun getHistoryActionFlow(): StateFlow<UiHistoryAction> = historyActionFlow.asStateFlow()

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
                gestureZoomDetected = false
                if (ev.pointerCount == 1 && instrument != UiInstrument.EMPTY) {
                    ev.getPointerCoords(0, pointerCoords1)
                    gestureDrawDetected = true
                    uiPath = TransitiveUiPath(
                        instrument = instrument,
                        color = color,
                        strokeWidth = strokeWidth,
                        coords = LinkedList(),
                    )
                    pointTimestampMs = SystemClock.uptimeMillis()
                    uiPath?.coords?.addLast(Triple(pointerCoords1.x, pointerCoords1.y, 0))
                    nativePath = Path().apply {
                        moveTo(pointerCoords1.x, pointerCoords1.y)
                    }
                } else {
                    gestureDrawDetected = false
                }
                invalidateInternal()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
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
                    val newPointTimestampMs = SystemClock.uptimeMillis()
                    val diffMs = newPointTimestampMs - pointTimestampMs
                    pointTimestampMs = newPointTimestampMs
                    uiPath?.coords?.addLast(Triple(pointerCoords1.x, pointerCoords1.y, diffMs))
                    nativePath.lineTo(pointerCoords1.x, pointerCoords1.y)
                } else {
                    gestureDrawDetected = false
                }

                invalidateInternal()
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (gestureDrawDetected && ev.pointerCount == 1) {
                    ev.getPointerCoords(0, pointerCoords1)
                    val newPointTimestampMs = SystemClock.uptimeMillis()
                    val diffMs = newPointTimestampMs - pointTimestampMs
                    pointTimestampMs = newPointTimestampMs
                    uiPath?.coords?.addLast(Triple(pointerCoords1.x, pointerCoords1.y, diffMs))
                    uiPath?.let {
                        redoStack.clear()
                        undoStack.addLast(it)
                    }
                    nativePath.lineTo(pointerCoords1.x, pointerCoords1.y)
                    redoNativeStack.clear()
                    undoNativeStack.addLast(nativePath to paint)
                    uiPath = null
                    historyActionFlow.value = historyActionFlow.value.copy(undoStack.isNotEmpty(), redoStack.isNotEmpty())
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
        for ((_path, _paint) in prevNativePathQueue) {
            canvas.drawPath(_path, _paint)
        }
        for ((_path, _paint) in undoNativeStack) {
            canvas.drawPath(_path, _paint)
        }
        if (gestureDrawDetected && !nativePath.isEmpty && instrument != UiInstrument.EMPTY) {
            canvas.drawPath(nativePath, paint)
        }
    }

    private fun invalidateInternal() {
        invalidate()
        invalidateParent()
    }

    private fun invalidateParent() {
        (parent as? View)?.invalidate()
    }

    private fun createPaint(instrument: UiInstrument, color: UiColor, strokeWidth: UiStrokeWidth): Paint {
        return when (instrument) {
            UiInstrument.PENCIL -> createPencilPaint(color, strokeWidth)
            UiInstrument.BRUSH -> createBrushPaint(color, strokeWidth)
            UiInstrument.ERASER -> createEraserPaint(strokeWidth)
            UiInstrument.FIGURES -> TODO()
            UiInstrument.EMPTY, UiInstrument.COLOR_PICKER, UiInstrument.PALETTE -> Paint()
        }
    }

    private fun createPencilPaint(color: UiColor, strokeWidth: UiStrokeWidth): Paint = Paint().also { p ->
        p.color = android.graphics.Color.argb(255, color.red, color.green, color.blue)
        p.alpha = color.alpha
        p.style = Paint.Style.STROKE
        p.strokeWidth = strokeWidth.dp * resources.displayMetrics.density
        p.strokeCap = Paint.Cap.SQUARE
        p.strokeJoin = Paint.Join.MITER
    }

    private fun createBrushPaint(color: UiColor, strokeWidth: UiStrokeWidth): Paint = Paint().also { p ->
        p.color = android.graphics.Color.argb(255, color.red, color.green, color.blue)
        p.alpha = color.alpha
        p.style = Paint.Style.STROKE
        p.strokeWidth = strokeWidth.dp * resources.displayMetrics.density
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
    }

    private fun createEraserPaint(strokeWidth: UiStrokeWidth): Paint = Paint().also { p ->
        p.color = android.graphics.Color.TRANSPARENT
        p.alpha = 255 // color.alpha
        p.style = Paint.Style.STROKE
        p.strokeWidth = strokeWidth.dp * resources.displayMetrics.density
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
        p.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }

    companion object {
        const val TAG = "CanvasLayout"
    }
}
