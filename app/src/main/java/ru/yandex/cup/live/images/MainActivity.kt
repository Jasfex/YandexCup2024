package ru.yandex.cup.live.images

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.postDelayed
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.yandex.cup.live.images.databinding.ActivityMainBinding
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
        binding.pencil.setOnLongClickListener { viewModel.onPencilLongClicked() }
        binding.brush.setOnLongClickListener { viewModel.onBrushLongClicked() }
        binding.eraser.setOnLongClickListener { viewModel.onEraserLongClicked() }
    }

    private suspend fun collectInstumentFlow() {
        viewModel.instrumentFlow.collect { instrument ->
            binding.canvas.setInstrument(instrument)
            binding.pencil.isSelected = instrument is Pencil
            binding.brush.isSelected = instrument is Brush
            binding.eraser.isSelected = instrument is Eraser
            binding.figures.isSelected = instrument is Figure
            binding.colorPicker.isSelected = instrument is ColorPicker
        }
    }
}
