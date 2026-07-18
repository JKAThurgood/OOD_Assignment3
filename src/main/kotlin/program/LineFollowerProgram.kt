package program

import api.RobotApi
import command.SetTrackVelocityCommand
import observer.Observer

class LineFollowerProgram : RobotProgram {

    override val name = "Line Follower"

    private var robot: RobotApi? = null

    private var leftOnLine = false
    private var centerOnLine = false
    private var rightOnLine = false

    private val leftObserver = Observer<Boolean> { value ->
        leftOnLine = value
        updateMovement()
    }

    private val centerObserver = Observer<Boolean> { value ->
        centerOnLine = value
        updateMovement()
    }

    private val rightObserver = Observer<Boolean> { value ->
        rightOnLine = value
        updateMovement()
    }

    override fun startProgram(robot: RobotApi) {
        this.robot = robot

        robot.sensors.lineLeft.subscribe(leftObserver)
        robot.sensors.lineCenter.subscribe(centerObserver)
        robot.sensors.lineRight.subscribe(rightObserver)
    }

    override fun stopProgram(robot: RobotApi) {
        robot.sensors.lineLeft.unsubscribe(leftObserver)
        robot.sensors.lineCenter.unsubscribe(centerObserver)
        robot.sensors.lineRight.unsubscribe(rightObserver)

        robot.perform(
            SetTrackVelocityCommand(
                robot.actuator,
                0.0,
                0.0
            )
        )

        this.robot = null
    }

    private fun updateMovement() {
        val api = robot ?: return

        val (left, right) = when {
            centerOnLine -> {
                120.0 to 120.0
            }

            leftOnLine -> {
                60.0 to 120.0
            }

            rightOnLine -> {
                120.0 to 60.0
            }

            else -> {
                80.0 to 80.0
            }
        }

        api.perform(
            SetTrackVelocityCommand(
                api.actuator,
                left,
                right
            )
        )
    }
}