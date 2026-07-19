package ui

import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import model.Robot

/**
 * A live readout of the sensor values — the consumer side of the Observer pattern.
 */
class TelemetryPanel : VBox(6.0) {

    private val title = styledLabel("Telemetry", 15.0, bold = true)
    private val sonar = valueLabel()
    private val temperature = valueLabel()
    private val vision = valueLabel()
    private val line = valueLabel()
    private val collision = valueLabel()

    private var lineLeft = false
    private var lineCenter = false
    private var lineRight = false

    init {
        padding = Insets(12.0)
        prefWidth = 210.0
        style = "-fx-background-color: #14171c;"

        children.addAll(
            title,
            captioned("Sonar (distance)", sonar),
            captioned("Temperature", temperature),
            captioned("Vision (color)", vision),
            captioned("Line L / C / R", line),
            captioned("Collision", collision),
        )
    }

    fun bindTo(robot: Robot) {

        robot.sonar.subscribe { value ->
            sonar.text = "%.2f".format(value)
        }

        robot.temperature.subscribe { value ->
            temperature.text = "%.2f".format(value)
        }

        robot.vision.subscribe { value ->
            vision.text = value.toString()
        }

        robot.collision.subscribe { value ->
            collision.text = value.toString()
        }


        robot.lineLeft.subscribe { value ->
            lineLeft = value
            updateLineDisplay()
        }

        robot.lineCenter.subscribe { value ->
            lineCenter = value
            updateLineDisplay()
        }

        robot.lineRight.subscribe { value ->
            lineRight = value
            updateLineDisplay()
        }
    }


    private fun updateLineDisplay() {
        val left = if (lineLeft) "L" else "-"
        val center = if (lineCenter) "C" else "-"
        val right = if (lineRight) "R" else "-"

        line.text = "$left / $center / $right"
    }


    private fun captioned(caption: String, value: Label): VBox =
        VBox(2.0, styledLabel(caption, 11.0, color = "#8b949e"), value)


    private fun valueLabel() =
        styledLabel("—", 18.0, bold = true)


    private fun styledLabel(
        text: String,
        size: Double,
        bold: Boolean = false,
        color: String = "#e6edf3"
    ): Label =
        Label(text).apply {
            style =
                "-fx-font-size: ${size}px; -fx-text-fill: $color;" +
                        if (bold) " -fx-font-weight: bold;" else ""
        }
}