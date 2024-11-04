package ru.yandex.cup.live.images.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.yandex.cup.live.images.ui.UiLayer
import java.util.LinkedList
import java.util.NoSuchElementException
import java.util.concurrent.ConcurrentLinkedDeque

class PlayerSurfaceView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
    defStyleRes,
), ru.yandex.cup.live.images.ui.PlayerSurface {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null
    private val drawingPaths: ConcurrentLinkedDeque<Pair<Path, Paint>> = ConcurrentLinkedDeque()

    override fun play(layers: List<UiLayer>) {
        drawingPaths.clear()
        job?.cancel()
        job = scope.launch {
            while (isActive) {
                for (layer in layers) {
                    val _drawingPaths = mutableListOf<Pair<Path, Paint>>()
                    drawingPaths.addAll(_drawingPaths)
                    for (path in layer.drawingQueue) {
                        ensureActive()
                        val paint = PaintUtils.createPaint(
                            resources.displayMetrics.density,
                            path.instrument,
                            path.color,
                            path.strokeWidth
                        )
                        val nativePath = Path()
                        if (path.coords.isNotEmpty()) {
                            val (x0, y0, _) = path.coords[0]
                            nativePath.moveTo(x0, y0)
                            (1 until path.coords.size).forEach { index ->
                                val (x1, y1, durationMs) = path.coords[index]
                                nativePath.lineTo(x1, y1)
                                delay(durationMs)
                                _drawingPaths.add(Path(nativePath) to paint)
                                if (drawingPaths.isEmpty()) {
                                    drawingPaths.addAll(_drawingPaths)
                                    postInvalidate()
                                }
                            }
                        }
                    }
                }
            }
        }
        postInvalidate()
    }

    override fun stop() {
        drawingPaths.clear()
        job?.cancel()
        job = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val _drawingPath = drawingPaths
        if (_drawingPath.isNotEmpty()) {
            _drawingPath.forEach { (path, paint) ->
                canvas.drawPath(path, paint)
            }
        }
        drawingPaths.clear()
    }
}
