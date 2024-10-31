package ru.yandex.cup.live.images

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.postDelayed
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding.canvas.setActive(true)
        randomize()

        // binding.colorPicker.isSelected = true
    }

    var i = 0
    val brush1 = Brush(color = Color(red = 255, green = 0, blue = 0), strokeWidth = StrokeWidth(dp = 7f))
    val brush2 = Brush(alpha = 128, color = Color(red = 0, green = 255, blue = 0), strokeWidth = StrokeWidth(dp = 3f))
    val pencil = Pencil(color = Color(red = 0, green = 0, blue = 255), strokeWidth = StrokeWidth(dp = 16f))
    val eraser = Eraser(strokeWidth = StrokeWidth(dp = 6f))
    val instruments = listOf<Instrument>(brush1, brush2, pencil, eraser)

    private fun randomize() {
        binding.canvas.postDelayed(3_000L) {
            Log.d("MainActivity", "randomize()")
            binding.canvas.setInstrument(instruments[i % instruments.size])
            i++
            randomize()
        }
    }
}
