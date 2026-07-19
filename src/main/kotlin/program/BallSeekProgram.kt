package program

import api.RobotApi
import command.SetTrackVelocityCommand
import javafx.scene.paint.Color
import observer.Observer
import kotlin.random.Random

/**
 * Autonomous program that searches for and drives to a red ball.
 */
class BallSeekProgram : RobotProgram {

    override val name = "Ball Finder"

    private var robot: RobotApi? = null

    private enum class State {
        SEARCHING,      // Full 360 scan
        EXPLORE_SPIN,   // Spinning a random amount
        EXPLORING,      // Driving forward for up to 2 seconds
        APPROACHING,    // Ball detected, moving towards it
        STOPPED         // Ball reached
    }

    private var state = State.SEARCHING

    // Ticks tracking
    private var spinTicks = 0
    private val maxSpinTicks = 200 // Ticks required for a full 360 scan

    private var exploreTicks = 0
    private var currentExploreSpinLimit = 0

    private val maxExploreTicks = 600

    // Movement speeds
    private val spinSpeed = 90.0
    private val exploreSpeed = 120.0
    private val approachSpeed = 140.0

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
     * Phase 1: Reset and do a clean 360 scan
     */
    private fun beginSearching() {
        state = State.SEARCHING
        spinTicks = 0
        drive(spinSpeed, -spinSpeed)
    }

    /**
     * Phase 2a: Spin a random amount before charging forward
     */
    private fun beginExploreSpin() {
        state = State.EXPLORE_SPIN
        spinTicks = 0
        // Randomize how long it spins (e.g., anywhere up to a full 360 degree spin)
        currentExploreSpinLimit = Random.nextInt(50, maxSpinTicks)

        // Randomly pick clockwise or counter-clockwise
        val direction = if (Random.nextBoolean()) 1.0 else -1.0
        drive(spinSpeed * direction, -spinSpeed * direction)
    }

    /**
     * Phase 2b: Move forward until collision or 2 seconds pass
     */
    private fun beginExploring() {
        state = State.EXPLORING
        exploreTicks = 0
        drive(exploreSpeed, exploreSpeed)
    }

    /**
     * Vision sensor handling logic
     */
    private val visionObserver = Observer<Color> { color ->
        if (state == State.STOPPED) return@Observer

        // ALWAYS interrupt any search/explore behavior if the ball is spotted
        if (isRed(color)) {
            state = State.APPROACHING
            drive(approachSpeed, approachSpeed)
            return@Observer
        }

        when (state) {
            State.SEARCHING -> {
                spinTicks++
                // If a full 360 is finished and nothing was seen, go to step 2 (random spin & move)
                if (spinTicks >= maxSpinTicks) {
                    beginExploreSpin()
                }
            }

            State.EXPLORE_SPIN -> {
                spinTicks++
                if (spinTicks >= currentExploreSpinLimit) {
                    beginExploring()
                }
            }

            State.EXPLORING -> {
                exploreTicks++
                // 2 seconds have passed without finding anything -> Repeat the full 360 search
                if (exploreTicks >= maxExploreTicks) {
                    beginSearching()
                }
            }

            State.APPROACHING -> {
                // Keep moving toward it. If it loses visual track, you could optionally 
                // revert to beginSearching() here if desired.
            }

            State.STOPPED -> {}
        }
    }

    /**
     * Collision sensor handling logic
     */
    private val collisionObserver = Observer<Boolean> { collided ->
        if (!collided) return@Observer

        when (state) {
            State.APPROACHING -> {
                // Hit the ball successfully!
                state = State.STOPPED
                drive(0.0, 0.0)
            }

            State.EXPLORING -> {
                // Hit something while exploring -> immediately drop everything and repeat search
                beginSearching()
            }

            else -> {
                // If a collision happens during a spin setup, safely reset to full search
                beginSearching()
            }
        }
    }

    override fun startProgram(robot: RobotApi) {
        this.robot = robot
        robot.sensors.vision.subscribe(visionObserver)
        robot.sensors.collision.subscribe(collisionObserver)
        beginSearching()
    }

    override fun stopProgram(robot: RobotApi) {
        robot.sensors.vision.unsubscribe(visionObserver)
        robot.sensors.collision.unsubscribe(collisionObserver)
        drive(0.0, 0.0)
        this.robot = null
    }
}