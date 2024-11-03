package ru.yandex.cup.live.images.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.yandex.cup.live.images.R
import ru.yandex.cup.live.images.databinding.ActivityMainBinding
import ru.yandex.cup.live.images.domain.color.Color
import ru.yandex.cup.live.images.domain.instument.STROKE_WIDTH_MAX
import ru.yandex.cup.live.images.domain.instument.STROKE_WIDTH_MIN

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
        setupStrokeWidthSeekBar()
        setupPaletteSeekBars()

        if (savedInstanceState == null) {
            subscribeInstrument()
            subscribePopupState()
            subscribeColor()
            subscribeStrokeWidth()
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun lockPortraitOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setupClickListeners() {
        binding.pencil.setOnClickListener { viewModel.onInstrumentClicked(R.id.pencil) }
        binding.brush.setOnClickListener { viewModel.onInstrumentClicked(R.id.brush) }
        binding.eraser.setOnClickListener { viewModel.onInstrumentClicked(R.id.eraser) }
        binding.figures.setOnClickListener { viewModel.onInstrumentClicked(R.id.figures) }
        binding.colorPicker.setOnClickListener { viewModel.onInstrumentClicked(R.id.color_picker) }
        binding.palette.setOnClickListener { viewModel.onInstrumentClicked(R.id.palette) }
        binding.colorWhite.setOnClickListener { viewModel.onColorUpdated(255, 255, 255, 255) }
        binding.colorRed.setOnClickListener { viewModel.onColorUpdated(255, 255, 61, 0) }
        binding.colorBlack.setOnClickListener { viewModel.onColorUpdated(255, 0, 0, 0) }
        binding.colorBlue.setOnClickListener { viewModel.onColorUpdated(255, 25, 118, 210) }
    }

    private fun setupLongClickListeners() {
        binding.pencil.setOnLongClickListener { viewModel.onInstrumentLongClicked(R.id.pencil) }
        binding.brush.setOnLongClickListener { viewModel.onInstrumentLongClicked(R.id.brush) }
        binding.eraser.setOnLongClickListener { viewModel.onInstrumentLongClicked(R.id.eraser) }
    }

    private fun setupStrokeWidthSeekBar() {
        binding.strokeWidthSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val newProgress = 100 - progress
                    val newStrokeWidth = newProgress.toStrokeWidth()
                    viewModel.onStrokeWidthUpdated(newStrokeWidth)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    private fun setupPaletteSeekBars() {
        val seekbarsListener = object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val color = viewModel.uiState.color.value
                    val newValue = progress.scaleAsByte()
                    when (seekBar) {
                        binding.alphaSeekBar -> viewModel.onColorUpdated(
                            alpha = newValue,
                            red = color.red,
                            green = color.green,
                            blue = color.blue,
                        )

                        binding.redSeekBar -> viewModel.onColorUpdated(
                            alpha = color.alpha,
                            red = newValue,
                            green = color.green,
                            blue = color.blue,
                        )

                        binding.greenSeekBar -> viewModel.onColorUpdated(
                            alpha = color.alpha,
                            red = color.red,
                            green = newValue,
                            blue = color.blue,
                        )

                        binding.blueSeekBar -> viewModel.onColorUpdated(
                            alpha = color.alpha,
                            red = color.red,
                            green = color.green,
                            blue = newValue,
                        )
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        }
        binding.alphaSeekBar.setOnSeekBarChangeListener(seekbarsListener)
        binding.redSeekBar.setOnSeekBarChangeListener(seekbarsListener)
        binding.greenSeekBar.setOnSeekBarChangeListener(seekbarsListener)
        binding.blueSeekBar.setOnSeekBarChangeListener(seekbarsListener)
    }

    private fun subscribeInstrument() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.instrument.collect { instrument ->
                    with(binding) {
                        pencil.isSelected = instrument == UiInstrument.PENCIL
                        brush.isSelected = instrument == UiInstrument.BRUSH
                        eraser.isSelected = instrument == UiInstrument.ERASER
                        figures.isSelected = instrument == UiInstrument.FIGURES
                        colorPicker.isSelected =
                            instrument == UiInstrument.COLOR_PICKER || instrument == UiInstrument.PALETTE
                        palette.isSelected = instrument == UiInstrument.PALETTE
                    }
                    // TODO:SALAM set instrument in canvas
                }
            }
        }
    }

    private fun subscribePopupState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.popupState.collect { popupState ->
                    if (popupState == UiPopupState.EMPTY) {
                        binding.dismissPopup.setOnClickListener(null)
                        binding.dismissPopup.visibility = View.INVISIBLE
                    } else {
                        binding.dismissPopup.setOnClickListener { viewModel.onDismissPopupClicked() }
                        binding.dismissPopup.visibility = View.VISIBLE
                    }
                    binding.strokeWidthPopup.isVisible = popupState == UiPopupState.STROKE_WIDTH
                    binding.colorPickerPopup.isVisible =
                        popupState == UiPopupState.COLOR_PICKER || popupState == UiPopupState.COLOR_PICKER_AND_PALETTE
                    binding.palettePopup.isVisible = popupState == UiPopupState.COLOR_PICKER_AND_PALETTE
                    // TODO:SALAM figures popup
                }
            }
        }
    }

    private fun subscribeColor() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.color.collect { color ->
                    val colorInt = android.graphics.Color.argb(
                        color.alpha,
                        color.red,
                        color.green,
                        color.blue,
                    )
                    binding.colorPicker.backgroundTintList = ColorStateList.valueOf(colorInt)
                    binding.alphaSeekBar.progress = color.alpha.fromByteToProgress()
                    binding.redSeekBar.progress = color.red.fromByteToProgress()
                    binding.greenSeekBar.progress = color.green.fromByteToProgress()
                    binding.blueSeekBar.progress = color.blue.fromByteToProgress()
                    // TODO:SALAM set color in canvas
                }
            }
        }
    }

    private fun subscribeStrokeWidth() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.strokeWidth.collect { strokeWidth ->
                    binding.strokeWidthSeekBar.progress = 100 - strokeWidth.dp.toProgress()
                    // TODO:SALAM set strokeWidth in canvas
                }
            }
        }
    }

    private fun Int.scaleAsByte(): Int {
        return this * 255 / 100
    }

    private fun Int.fromByteToProgress(): Int {
        return this * 100 / 255
    }

    private fun Float.toProgress(): Int {
        val k = 100f / (STROKE_WIDTH_MAX - STROKE_WIDTH_MIN)
        val b = 100f / (STROKE_WIDTH_MIN - STROKE_WIDTH_MAX)
        return (k * this + b).toInt().coerceIn(0, 100)
    }

    private fun Int.toStrokeWidth(): Float {
        val b = STROKE_WIDTH_MIN
        val k = (STROKE_WIDTH_MAX * 1.5f - STROKE_WIDTH_MIN * 2f) / 150f
        return (k * this + b).coerceIn(STROKE_WIDTH_MIN, STROKE_WIDTH_MAX)
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
