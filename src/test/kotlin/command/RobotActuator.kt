package command

class FakeActuator : RobotActuator {

    override var leftTrackVelocity: Double = 0.0
    override var rightTrackVelocity: Double = 0.0

    override fun setTrackVelocities(
        left: Double,
        right: Double
    ) {
        leftTrackVelocity = left
        rightTrackVelocity = right
    }
}