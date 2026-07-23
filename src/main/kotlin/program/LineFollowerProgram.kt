package program

import api.RobotApi
import observer.Observer

class LineFollowerProgram : RobotProgram {

    override val name = "Line Follower"

    private var robot: RobotApi? = null

    private var leftOnLine = false
    private var centerOnLine = false
    private var rightOnLine = false

    private enum class State {
        FOLLOWING,
        RECOVERING
    }

    private var lastLeft = Double.NaN
    private var lastRight = Double.NaN

    private fun drive(left: Double, right: Double) {
        if (left == lastLeft && right == lastRight) {
            return
        }

        lastLeft = left
        lastRight = right

        robot?.drive(
            left,
            right
        )
    }

    private var state = State.FOLLOWING

    private val forwardSpeed = 80.0
    private val turnSpeed = 65.0

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

        drive(0.0, 0.0)

        this.robot = null
    }


    private fun updateMovement() {
        val api = robot ?: return
        val activeSensors =
            listOf(
                leftOnLine,
                centerOnLine,
                rightOnLine
            ).count { it }


        when (activeSensors) {

            // All sensors see the line
            3 -> {
                state = State.FOLLOWING

                drive(
                    forwardSpeed,
                    forwardSpeed
                )
            }


            // Two sensors see the line.
            // Make a small correction but keep moving.
            2 -> {
                state = State.FOLLOWING

                when {
                    !leftOnLine -> {
                        // Line is drifting left, steer left
                        drive(
                            forwardSpeed,
                            forwardSpeed + turnSpeed
                        )
                    }

                    !rightOnLine -> {
                        // Line is drifting right, steer right
                        drive(
                            forwardSpeed + turnSpeed,
                            forwardSpeed
                        )
                    }

                    !centerOnLine -> {
                        // Center lost, but edges still know where the line is.
                        drive(
                            -forwardSpeed,
                            -forwardSpeed
                        )
                    }
                }
            }


            // Only one sensor sees the line.
            // Slow recovery turn.
            1 -> {
                state = State.RECOVERING

                when {

                    leftOnLine -> {
                        // Pivot left until center returns
                        drive(
                            turnSpeed,
                            0.0
                        )
                    }

                    rightOnLine -> {
                        // Pivot right until center returns
                        drive(
                            0.0,
                            turnSpeed
                        )
                    }

                    centerOnLine -> {
                        // Move forward slowly
                        drive(
                            -forwardSpeed / 2,
                            -forwardSpeed / 2
                        )
                    }
                }
            }


            // Completely lost.
            // Stop and spin slowly to reacquire.
            0 -> {
                state = State.RECOVERING

                drive(
                    -turnSpeed,
                    turnSpeed
                )
            }
        }
    }
}