package program

import api.RobotApi
import command.SetTrackVelocityCommand
import javafx.scene.paint.Color
import observer.Observer
import sensor.VisionSensor

class BallSeekProgram : RobotProgram {

    override val name = "Ball Finder"

    private var robot: RobotApi? = null

    private var ballSeen = false

    private val visionObserver = Observer<Color> { color ->
        val api = robot ?: return@Observer

        if (color == Color.RED) {
            ballSeen = true

            api.perform(
                SetTrackVelocityCommand(
                    api.actuator,
                    120.0,
                    120.0
                )
            )
        } else {
            ballSeen = false

            api.perform(
                SetTrackVelocityCommand(
                    api.actuator,
                    80.0,
                    -80.0
                )
            )
        }
    }

    private val collisionObserver = Observer<Boolean> { collided ->
        val api = robot ?: return@Observer

        if (collided) {
            api.perform(
                SetTrackVelocityCommand(
                    api.actuator,
                    0.0,
                    0.0
                )
            )
        }
    }

    override fun startProgram(robot: RobotApi) {
        this.robot = robot

        robot.sensors.vision.subscribe(visionObserver)
        robot.sensors.collision.subscribe(collisionObserver)
    }

    override fun stopProgram(robot: RobotApi) {
        robot.sensors.vision.unsubscribe(visionObserver)
        robot.sensors.collision.unsubscribe(collisionObserver)

        robot.perform(
            SetTrackVelocityCommand(
                robot.actuator,
                0.0,
                0.0
            )
        )

        this.robot = null
    }
}