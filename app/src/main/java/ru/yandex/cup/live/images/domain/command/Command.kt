package ru.yandex.cup.live.images.domain.command

import ru.yandex.cup.live.images.domain.instument.Instrument

sealed interface Command

// controls

object Undo : Command // UICommand & HistoryCommand

object Redo : Command // UICommand & HistoryCommand

data class DeleteLayer(
    val layer: String,
) : Command // UICommand & HistoryCommand

object DeleteAllLayers : Command // UICommand & HistoryCommand

data class AddLayer(
    val layer: String,
) : Command // UICommand & HistoryCommand

object ShowLayersList : Command // UICommand

data class EditLayer(
    val layer: String,
) : Command // UICommand
object SwipeLeft : Command // UICommand
object SwipeRight : Command // UICommand
object DuplicateLayer : Command // UICommand & HistoryCommand

object Pause : Command // UICommand

object Play : Command // UICommand

data class SetSpeed(
    val speed: Int,
) : Command // UICommand

// instruments

object SetStrokeWidth : Command // UICommand & DrawingCommand

object SelectPencil : Command // UICommand
object DrawPencil : Command // DrawingCommand & HistoryCommand

object SelectBrush : Command // UICommand
object DrawBrush : Command // DrawingCommand & HistoryCommand

object SelectEraser : Command // UICommand
object DrawEraser : Command // DrawingCommand & HistoryCommand

object SelectFigure : Command // UICommand
object DrawFigure : Command // DrawingCommand & HistoryCommand

object ScaleFigure : Command // DrawingCommand & HistoryCommand
object RotateFigure : Command // DrawingCommand & HistoryCommand
object TranslateFigure : Command // DrawingCommand & HistoryCommand

object SelectColorPicker : Command // UICommand
data class SetColor(
    val color: String,
) : Command  // DrawingCommand & UICommand & HistoryCommand

// other

object PinchToZoomFigure : Command // TODO:SALAM
object ZoomIn : Command
object ZoomOut : Command

object ShowSemiopaqueLayer : Command // UICommand

object GenerateLayers : Command // UICommand & HistoryCommand

object ExportAsGif : Command // UICommand
