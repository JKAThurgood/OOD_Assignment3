package program

import api.RobotApi
import command.SetTrackVelocityCommand
import observer.Observer

class TemperatureSeekProgram : RobotProgram {

    override val name = "Temperature Seeker"
    private var robot: RobotApi? = null

    // Tracks the absolute highest temperature recorded in this run
    private var bestTemperature: Double? = null

    private enum class State {
        FORWARD,
        TURNING_LEFT,
        COLLISION_BACKUP,
        COLLISION_FORWARD,
        COLLISION_TURN,
        EDGE_OF_MAP_TURN,
    }

    private var state = State.FORWARD
    private var tickCounter = 0

    private val forwardSpeed = 50.0
    private val blockedDistance = 0.0
    private val reverseSpeed = -50.0
    private val turnSpeed = 95.0

    private fun drive(left: Double, right: Double) {
        val api = robot ?: return
        api.perform(SetTrackVelocityCommand(api.actuator, left, right))
    }

    private val sonarObserver = Observer<Double> { distance ->


        if (distance <= blockedDistance) {

            state = State.EDGE_OF_MAP_TURN
            tickCounter = 0
            drive(-turnSpeed, turnSpeed)
        }
    }
    private val temperatureObserver = Observer<Double> { temperature ->
        val currentBest = bestTemperature

        // Establish initial baseline
        if (currentBest == null) {
            bestTemperature = temperature
            drive(forwardSpeed, forwardSpeed)
            return@Observer
        }

        // Update the best-ever temperature if we just found a hotter spot
        if (temperature > currentBest) {
            bestTemperature = temperature
        }

        when (state) {
            State.FORWARD -> {
                // As long as we are matching or exceeding our best ever temp, keep crushing it forward
                if (temperature >= currentBest) {
                    drive(forwardSpeed, forwardSpeed)
                } else {
                    // We are dropping below our best-ever baseline. Pivot left for one frame.
                    state = State.TURNING_LEFT
                    drive(-turnSpeed, turnSpeed)
                }
            }

            State.TURNING_LEFT -> {
                tickCounter++
                if (tickCounter >= 90) {
                    state = State.FORWARD
                    drive(forwardSpeed, forwardSpeed)
                }
            }

            State.COLLISION_BACKUP -> {
                tickCounter++
                if (tickCounter >= 80) {
                    state = State.COLLISION_TURN
                    tickCounter = 0
                    drive(-turnSpeed, turnSpeed)
                }
            }

            State.COLLISION_TURN -> {
                tickCounter++
                if (tickCounter >= 5) {
                    state = State.COLLISION_FORWARD
                    tickCounter = 0
                    drive(forwardSpeed, forwardSpeed)
                }
            }
            // Avoid the object, ignore temperature for a while, then resume seeking
            State.COLLISION_FORWARD -> {
                tickCounter++
                if (tickCounter >= 500) {
                    bestTemperature = null
                    state = State.FORWARD
                    tickCounter = 0
                    drive(forwardSpeed, forwardSpeed)
                }
            }

            State.EDGE_OF_MAP_TURN -> {
                tickCounter++
                if (tickCounter >= 80) {
                    state = State.FORWARD
                    tickCounter = 0
                    drive(forwardSpeed, forwardSpeed)
                }
            }
        }
    }

    private val collisionObserver = Observer<Boolean> { collided ->
        if (!collided) return@Observer

        state = State.COLLISION_BACKUP
        tickCounter = 0
        drive(reverseSpeed, reverseSpeed)
    }

    override fun startProgram(robot: RobotApi) {
        this.robot = robot
        state = State.FORWARD
        bestTemperature = null
        tickCounter = 0

        robot.sensors.temperature.subscribe(temperatureObserver)
        robot.sensors.collision.subscribe(collisionObserver)
        robot.sensors.sonar.subscribe(
            sonarObserver
        )

        drive(forwardSpeed, forwardSpeed)
    }

    override fun stopProgram(robot: RobotApi) {
        robot.sensors.temperature.unsubscribe(temperatureObserver)
        robot.sensors.collision.unsubscribe(collisionObserver)
        robot.sensors.sonar.unsubscribe(
            sonarObserver
        )

        drive(0.0, 0.0)
        this.robot = null
    }
}