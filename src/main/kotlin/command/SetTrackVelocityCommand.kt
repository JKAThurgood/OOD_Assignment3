package command

class SetTrackVelocityCommand(
    private val receiver: RobotActuator,
    private val left: Double,
    private val right: Double
) : Command {
    private var previousLeft: Double = 0.0
    private var previousRight: Double = 0.0

    override fun execute() {
        // Store the previous values before executing the command
        previousLeft = receiver.leftTrackVelocity
        previousRight = receiver.rightTrackVelocity

        // Execute the command to set the new track velocities
        receiver.setTrackVelocities(left, right)
    }

    override fun undo() {
        // Restore the previous track velocities
        receiver.setTrackVelocities(previousLeft, previousRight)
    }
}