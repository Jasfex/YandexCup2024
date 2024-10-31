package ru.yandex.cup.live.images

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.yandex.cup.live.images.databinding.ActivityMainBinding
import ru.yandex.cup.live.images.domain.instument.Brush
import ru.yandex.cup.live.images.domain.instument.Circle
import ru.yandex.cup.live.images.domain.instument.ColorPicker
import ru.yandex.cup.live.images.domain.instument.Eraser
import ru.yandex.cup.live.images.domain.instument.Figure
import ru.yandex.cup.live.images.domain.instument.Instrument
import ru.yandex.cup.live.images.domain.instument.Pencil
import ru.yandex.cup.live.images.domain.instument.Square
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

        // val instrument: Instrument? = null
        // when (instrument) {
        //     is Pencil -> TODO()
        //     is Brush -> TODO()
        //     is Eraser -> TODO()
        //     is Figure -> when (instrument as Figure) {
        //         is Circle -> TODO()
        //         is Square -> TODO()
        //         is Triangle -> TODO()
        //     }
        //     is ColorPicker -> TODO()
        //     null -> TODO()
        // }

        // binding.colorPicker.isSelected = true
    }
}
