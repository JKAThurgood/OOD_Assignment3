package ui

import api.RobotApi
import command.SetTrackVelocityCommand
import environment.Environment
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

/**
 * Manual controller. Each drive button should build one of YOUR command classes and submit it
 * through the [RobotApi] — so manual driving shares the same undoable action path as a program.
 *
 * The layout, the environment selector, and Undo / Redo / Reset are provided. Wiring the five drive
 * buttons (and the keyboard, in RobotSimulationApp) to your commands is your job — see [drive].
 */
class ControlPanel(
    private val api: RobotApi,
    private val driveSettings: DriveSettings,
    environments: List<Environment>,
    onSelectEnvironment: (Environment) -> Unit,
    onReset: () -> Unit,
) : VBox(8.0) {

    init {
        padding = Insets(10.0)
        style = "-fx-background-color: #1b1f24;"

        val envBox = HBox(8.0).apply {
            alignment = Pos.CENTER_LEFT

            val combo = ComboBox<Environment>().apply {
                items.addAll(environments)
                selectionModel.selectFirst()
                setCellFactory { listCell() }
                buttonCell = listCell()
                valueProperty().addListener { _, _, env ->
                    if (env != null) onSelectEnvironment(env)
                }
                prefWidth = 320.0
            }

            children.addAll(caption("Environment:"), combo)
        }

        val settingsBox = HBox(8.0).apply {
            alignment = Pos.CENTER_LEFT

            val speedField = TextField(driveSettings.speed.toString()).apply {
                prefWidth = 80.0
                textProperty().addListener { _, _, value ->
                    driveSettings.speed = value.toDoubleOrNull() ?: driveSettings.speed
                }
            }

            val turnField = TextField(driveSettings.turn.toString()).apply {
                prefWidth = 80.0
                textProperty().addListener { _, _, value ->
                    driveSettings.turn = value.toDoubleOrNull() ?: driveSettings.turn
                }
            }

            children.addAll(
                caption("Speed:"),
                speedField,
                caption("Turn:"),
                turnField
            )
        }

        val driveBox = HBox(8.0).apply {
            alignment = Pos.CENTER_LEFT

            children.addAll(
                button("◄ Left") { drive(driveSettings.turn, -driveSettings.turn) },
                button("▲ Forward") { drive(driveSettings.speed, driveSettings.speed) },
                button("▼ Back") { drive(-driveSettings.speed, -driveSettings.speed) },
                button("► Right") { drive(-driveSettings.turn, driveSettings.turn) },
                button("■ Stop") { drive(0.0, 0.0) },
                spacer(),
                button("Undo") { api.undo() },
                button("Redo") { api.redo() },
                button("Reset") { onReset() },
            )
        }

        children.addAll(envBox, settingsBox, driveBox)
    }

    private fun drive(left: Double, right: Double) {
        api.perform(
            SetTrackVelocityCommand(
                api.actuator,
                left,
                right
            )
        )
    }

    private fun button(text: String, action: () -> Unit) =
        Button(text).apply { setOnAction { action() } }

    private fun caption(text: String) =
        Label(text).apply { style = "-fx-text-fill: #c9d1d9;" }

    private fun spacer() =
        javafx.scene.layout.Region().apply {
            HBox.setHgrow(this, javafx.scene.layout.Priority.ALWAYS)
        }

    private fun listCell() =
        object : javafx.scene.control.ListCell<Environment>() {
            override fun updateItem(item: Environment?, empty: Boolean) {
                super.updateItem(item, empty)
                text = if (empty || item == null) null else item.name
            }
        }
}