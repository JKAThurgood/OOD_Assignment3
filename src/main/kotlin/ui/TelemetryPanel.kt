package ui

import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.paint.Color
import javafx.scene.layout.VBox
import model.Robot
import observer.Observer


/**
 * Live robot telemetry display.
 *
 * Uses the Observer pattern:
 * - Sensors push readings here.
 * - Robot actuator state pushes velocity updates here.
 */
class TelemetryPanel : VBox(6.0) {
    private var currentRobot: Robot? = null
    private val sonarObserver =
        Observer<Double> { value ->
            sonar.text = "%.2f".format(value)
        }

    private val temperatureObserver =
        Observer<Double> { value ->
            temperature.text = "%.1f".format(value)
        }

    private val visionObserver = Observer<Color> { value ->
        vision.text = value.toString()
    }

    private val leftLineObserver = Observer<Boolean> {
        currentRobot?.let(::updateLine)
    }

    private val centerLineObserver = Observer<Boolean> {
        currentRobot?.let(::updateLine)
    }

    private val rightLineObserver = Observer<Boolean> {
        currentRobot?.let(::updateLine)
    }

    private val collisionObserver = Observer<Boolean> { value ->
        collision.text = value.toString()
    }

    private val leftTrackObserver = Observer<Double> { value ->
        leftTrack.text = "%.1f".format(value)
    }

    private val rightTrackObserver = Observer<Double> { value ->
        rightTrack.text = "%.1f".format(value)
    }

    private val title =
        styledLabel("Telemetry", 15.0, bold = true)


    private val sonar =
        valueLabel()

    private val temperature =
        valueLabel()

    private val vision =
        valueLabel()

    private val line =
        valueLabel()

    private val collision =
        valueLabel()


    private val leftTrack =
        valueLabel()

    private val rightTrack =
        valueLabel()


    init {

        padding = Insets(12.0)

        prefWidth = 210.0

        style =
            "-fx-background-color: #14171c;"


        children.addAll(

            title,


            captioned(
                "Sonar (distance)",
                sonar
            ),


            captioned(
                "Temperature",
                temperature
            ),


            captioned(
                "Vision (color)",
                vision
            ),


            captioned(
                "Line L / C / R",
                line
            ),


            captioned(
                "Collision",
                collision
            ),


            captioned(
                "Left Track Speed",
                leftTrack
            ),


            captioned(
                "Right Track Speed",
                rightTrack
            )
        )
    }


    private fun updateLine(robot: Robot) {

        val left =
            if (robot.lineLeft.reading == true)
                "L"
            else
                "-"


        val center =
            if (robot.lineCenter.reading == true)
                "C"
            else
                "-"


        val right =
            if (robot.lineRight.reading == true)
                "R"
            else
                "-"


        line.text =
            "$left / $center / $right"
    }


    private fun captioned(
        caption: String,
        value: Label
    ) =
        VBox(
            2.0,
            styledLabel(
                caption,
                11.0,
                color = "#8b949e"
            ),
            value
        )


    private fun valueLabel() =
        styledLabel(
            "—",
            18.0,
            bold = true
        )


    private fun styledLabel(
        text: String,
        size: Double,
        bold: Boolean = false,
        color: String = "#e6edf3"
    ) =
        Label(text).apply {

            style =
                "-fx-font-size: ${size}px;" +
                        " -fx-text-fill: $color;" +
                        if (bold)
                            " -fx-font-weight: bold;"
                        else
                            ""
        }

    fun bindTo(robot: Robot) {
        currentRobot = robot

        robot.sonar.subscribe(sonarObserver)
        robot.temperature.subscribe(temperatureObserver)

        robot.vision.subscribe(visionObserver)

        robot.lineLeft.subscribe(leftLineObserver)
        robot.lineCenter.subscribe(centerLineObserver)
        robot.lineRight.subscribe(rightLineObserver)

        robot.collision.subscribe(collisionObserver)

        robot.subscribeLeftVelocity(leftTrackObserver)
        robot.subscribeRightVelocity(rightTrackObserver)
    }

    fun unbindFrom(robot: Robot) {
        robot.sonar.unsubscribe(sonarObserver)
        robot.temperature.unsubscribe(temperatureObserver)

        robot.vision.unsubscribe(visionObserver)

        robot.lineLeft.unsubscribe(leftLineObserver)
        robot.lineCenter.unsubscribe(centerLineObserver)
        robot.lineRight.unsubscribe(rightLineObserver)

        robot.collision.unsubscribe(collisionObserver)

        robot.unsubscribeLeftVelocity(leftTrackObserver)
        robot.unsubscribeRightVelocity(rightTrackObserver)
    }
}