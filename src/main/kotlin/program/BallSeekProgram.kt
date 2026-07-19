package program

import api.RobotApi
import command.SetTrackVelocityCommand
import javafx.scene.paint.Color
import observer.Observer

/**
 * Searches for a red ball.
 *
 * Behavior:
 * 1. Spin left looking for the ball.
 * 2. If no ball is found after a short scan, drive forward.
 * 3. If collision occurs or sonar reports an immediate obstacle,
 *    return to discovery mode (searching).
 * 4. If the ball is seen, drive straight toward it.
 * 5. If collision occurs while approaching, assume we reached the ball.
 */
class BallSeekProgram : RobotProgram {

    override val name = "Ball Finder"

    private var robot: RobotApi? = null

    private enum class State {
        SEARCHING,
        EXPLORING,
        APPROACHING,
        STOPPED
    }

    private var state = State.SEARCHING

    private var searchTicks = 0
    private val maxSearchTicks = 300

    private val searchSpinSpeed = 90.0
    private val exploreSpeed = 120.0
    private val approachSpeed = 140.0

    /**
     * Treat anything extremely close as blocked.
     */
    private val blockedDistance = 5.0

    private fun isRed(color: Color): Boolean {
        return color.red > 0.7 &&
                color.green < 0.4 &&
                color.blue < 0.4
    }

    private fun drive(left: Double, right: Double) {
        val api = robot ?: return

        api.perform(
            SetTrackVelocityCommand(
                api.actuator,
                left,
                right
            )
        )
    }

    /**
     * Discovery mode.
     * Continuously rotate left looking for the ball.
     */
    private fun beginSearching() {

        state = State.SEARCHING
        searchTicks = 0

        drive(
            searchSpinSpeed,
            -searchSpinSpeed
        )
    }

    /**
     * Move through the world looking for new viewpoints.
     */
    private fun beginExploring() {

        state = State.EXPLORING

        drive(
            exploreSpeed,
            exploreSpeed
        )
    }

    /**
     * Ball spotted.
     */
    private fun beginApproaching() {

        state = State.APPROACHING

        drive(
            approachSpeed,
            approachSpeed
        )
    }

    private val visionObserver = Observer<Color> { color ->

        if (state == State.STOPPED) {
            return@Observer
        }

        if (isRed(color)) {

            if (state != State.APPROACHING) {
                beginApproaching()
            }

            return@Observer
        }

        when (state) {

            State.SEARCHING -> {

                searchTicks++

                // After scanning for a while,
                // move somewhere new.
                if (searchTicks >= maxSearchTicks) {
                    beginExploring()
                }
            }

            State.APPROACHING -> {
                // Lost visual contact with the ball.
                beginSearching()
            }

            else -> {}
        }
    }

    /**
     * Sonar is only used as an immediate obstacle detector.
     * We no longer wait for sonar to clear.
     */
    private val sonarObserver = Observer<Double> { distance ->

        if (state == State.STOPPED) {
            return@Observer
        }

        if (state == State.APPROACHING) {
            return@Observer
        }

        if (distance <= blockedDistance) {

            beginSearching()
        }
    }

    private val collisionObserver = Observer<Boolean> { collided ->

        if (!collided) {
            return@Observer
        }


        // Hit a wall or obstacle.
        // Restart discovery.
        beginSearching()


    }

    override fun startProgram(robot: RobotApi) {

        this.robot = robot

        robot.sensors.vision.subscribe(
            visionObserver
        )

        robot.sensors.sonar.subscribe(
            sonarObserver
        )

        robot.sensors.collision.subscribe(
            collisionObserver
        )

        beginSearching()
    }

    override fun stopProgram(robot: RobotApi) {

        robot.sensors.vision.unsubscribe(
            visionObserver
        )

        robot.sensors.sonar.unsubscribe(
            sonarObserver
        )

        robot.sensors.collision.unsubscribe(
            collisionObserver
        )

        drive(
            0.0,
            0.0
        )

        this.robot = null
    }
}