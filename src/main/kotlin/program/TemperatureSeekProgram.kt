package program

import api.RobotApi
import command.SetTrackVelocityCommand
import observer.Observer

class TemperatureSeekProgram : RobotProgram {

    override val name = "Temperature Seeker"

    private var robot: RobotApi? = null

    private var lastTemperature: Double? = null
    private var searchingRight = true

    private val temperatureObserver = Observer<Double> { temperature ->
        val api = robot ?: return@Observer

        val previous = lastTemperature

        if (previous == null) {
            lastTemperature = temperature
            return@Observer
        }

        val command = when {
            temperature > previous -> {
                // Getting hotter, continue forward
                SetTrackVelocityCommand(
                    api.actuator,
                    120.0,
                    120.0
                )
            }

            else -> {
                // Getting colder, rotate and search
                searchingRight = !searchingRight

                if (searchingRight) {
                    SetTrackVelocityCommand(
                        api.actuator,
                        80.0,
                        -80.0
                    )
                } else {
                    SetTrackVelocityCommand(
                        api.actuator,
                        -80.0,
                        80.0
                    )
                }
            }
        }

        api.perform(command)

        lastTemperature = temperature
    }

    override fun startProgram(robot: RobotApi) {
        this.robot = robot
        lastTemperature = null

        robot.sensors.temperature.subscribe(temperatureObserver)
    }

    override fun stopProgram(robot: RobotApi) {
        robot.sensors.temperature.unsubscribe(temperatureObserver)

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