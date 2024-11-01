package ru.yandex.cup.live.images

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.postDelayed
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.yandex.cup.live.images.databinding.ActivityMainBinding
import ru.yandex.cup.live.images.databinding.ViewColorPickerPaletteBinding
import ru.yandex.cup.live.images.databinding.ViewStrokeWidthSeekBarBinding
import ru.yandex.cup.live.images.domain.color.Color
import ru.yandex.cup.live.images.domain.instument.Brush
import ru.yandex.cup.live.images.domain.instument.Circle
import ru.yandex.cup.live.images.domain.instument.ColorPicker
import ru.yandex.cup.live.images.domain.instument.Eraser
import ru.yandex.cup.live.images.domain.instument.Figure
import ru.yandex.cup.live.images.domain.instument.Instrument
import ru.yandex.cup.live.images.domain.instument.Pencil
import ru.yandex.cup.live.images.domain.instument.Square
import ru.yandex.cup.live.images.domain.instument.StrokeWidth
import ru.yandex.cup.live.images.domain.instument.Triangle
import ru.yandex.cup.live.images.domain.instument.toProgress
import ru.yandex.cup.live.images.ui.ShowColorPicker
import ru.yandex.cup.live.images.ui.ShowStrokeWidthSeekBar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // TODO:SALAM
        binding.figures.isEnabled = false

        // TODO:SALAM
        binding.canvas.setActive(true)

        lockPortraitOrientation()
        setupClickListeners()
        setupLongClickListeners()

        if (savedInstanceState == null) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    collectInstumentFlow()
                }
            }
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    collectEventsFlow()
                }
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun lockPortraitOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setupClickListeners() {
        binding.pencil.setOnClickListener { viewModel.onPencilClicked() }
        binding.brush.setOnClickListener { viewModel.onBrushClicked() }
        binding.eraser.setOnClickListener { viewModel.onEraserClicked() }
        binding.figures.setOnClickListener { viewModel.onFiguresClicked() }
        binding.colorPicker.setOnClickListener { viewModel.onColorPickerClicked() }
    }

    private fun setupLongClickListeners() {
        binding.pencil.setOnLongClickListener { viewModel.onViewLongClicked(R.id.pencil) }
        binding.brush.setOnLongClickListener { viewModel.onViewLongClicked(R.id.brush) }
        binding.eraser.setOnLongClickListener { viewModel.onViewLongClicked(R.id.eraser) }
    }

    private suspend fun collectInstumentFlow() {
        viewModel.instrumentFlow.collect { instrument ->
            binding.canvas.setInstrument(instrument)
            binding.pencil.isSelected = instrument is Pencil
            binding.brush.isSelected = instrument is Brush
            binding.eraser.isSelected = instrument is Eraser
            binding.figures.isSelected = instrument is Figure
            binding.colorPicker.isSelected = instrument is ColorPicker
            if (instrument is ColorPicker) {
                val colorInt = android.graphics.Color.argb(
                    instrument.alpha,
                    instrument.color.red,
                    instrument.color.green,
                    instrument.color.blue,
                )
                binding.colorPicker.backgroundTintList = ColorStateList.valueOf(colorInt)
            }
        }
    }

    private suspend fun collectEventsFlow() {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                is ShowStrokeWidthSeekBar -> showStrokeWidthSeekBar(event.instrument)
                is ShowColorPicker -> showColorPicker(event.colorPicker)
            }
        }
    }

    private fun showStrokeWidthSeekBar(instrument: Instrument) {
        val seekBar = ViewStrokeWidthSeekBarBinding.inflate(layoutInflater)
        val seekBarWidth = resources.getDimensionPixelSize(R.dimen.liveimages_stroke_width_seek_bar_width)
        val seekBarHeight = resources.getDimensionPixelSize(R.dimen.liveimages_stroke_width_seek_bar_height)
        val sourceProgress = 100 - instrument.strokeWidth.toProgress()
        seekBar.root.progress = sourceProgress

        seekBar.root.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.onStrokeWidthUpdated(instrument, 100 - progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        binding.controlsPopup.removeAllViews()
        binding.controlsPopup.addView(seekBar.root, seekBarWidth, seekBarHeight)
        binding.controlsPopup.visibility = View.VISIBLE

        binding.overlay.visibility = View.VISIBLE
        binding.overlay.setOnClickListener {
            binding.controlsPopup.visibility = View.INVISIBLE
            binding.controlsPopup.removeAllViews()
            binding.overlay.setOnClickListener(null)
            binding.overlay.visibility = View.INVISIBLE
        }
    }

    private fun showColorPicker(colorPicker: ColorPicker) {
        binding.controlsPopup.removeAllViews()
        val palette = ViewColorPickerPaletteBinding.inflate(layoutInflater)
        val paletteWidth = resources.getDimensionPixelSize(R.dimen.liveimages_color_picker_palette_width)
        val paletteHeight = resources.getDimensionPixelSize(R.dimen.liveimages_color_picker_palette_height)

        binding.controlsPopup.removeAllViews()
        binding.controlsPopup.addView(palette.root, paletteWidth, paletteHeight)
        binding.controlsPopup.visibility = View.VISIBLE

        binding.overlay.visibility = View.VISIBLE
        binding.overlay.setOnClickListener {
            binding.controlsPopup.visibility = View.INVISIBLE
            binding.controlsPopup.removeAllViews()
            binding.overlay.setOnClickListener(null)
            binding.overlay.visibility = View.INVISIBLE
            viewModel.onColorPickerDismissed()
        }

        palette.palette.setOnClickListener {
            // TODO:SALAM show ARGB seek bars
        }
        palette.colorWhite.setOnClickListener { viewModel.onColorPicked(Color.WHITE) }
        palette.colorRed.setOnClickListener { viewModel.onColorPicked(Color.RED) }
        palette.colorBlack.setOnClickListener { viewModel.onColorPicked(Color.BLACK) }
        palette.colorBlue.setOnClickListener { viewModel.onColorPicked(Color.BLUE) }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
