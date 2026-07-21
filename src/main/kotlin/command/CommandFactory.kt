package command

object CommandFactory {

    fun stop(
        actuator: RobotActuator
    ): Command =
        SetTrackVelocityCommand(
            actuator,
            0.0,
            0.0
        )

    fun forward(
        actuator: RobotActuator,
        speed: Double
    ): Command =
        SetTrackVelocityCommand(
            actuator,
            speed,
            speed
        )

    fun reverse(
        actuator: RobotActuator,
        speed: Double
    ): Command =
        SetTrackVelocityCommand(
            actuator,
            -speed,
            -speed
        )

    fun turnLeft(
        actuator: RobotActuator,
        speed: Double
    ): Command =
        SetTrackVelocityCommand(
            actuator,
            -speed,
            speed
        )

    fun turnRight(
        actuator: RobotActuator,
        speed: Double
    ): Command =
        SetTrackVelocityCommand(
            actuator,
            speed,
            -speed
        )

    fun setTracks(
        actuator: RobotActuator,
        left: Double,
        right: Double
    ): Command =
        SetTrackVelocityCommand(
            actuator,
            left,
            right
        )
}