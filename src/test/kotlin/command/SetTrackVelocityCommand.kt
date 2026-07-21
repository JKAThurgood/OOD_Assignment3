package command

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SetTrackVelocityCommandTest {

    @Test
    fun execute_updates_track_velocities() {

        val actuator = FakeActuator()

        val command = SetTrackVelocityCommand(
            actuator,
            100.0,
            50.0
        )

        command.execute()

        assertEquals(100.0, actuator.leftTrackVelocity)
        assertEquals(50.0, actuator.rightTrackVelocity)
    }

    @Test
    fun undo_restores_previous_velocities() {

        val actuator = FakeActuator()

        actuator.setTrackVelocities(
            20.0,
            30.0
        )

        val command = SetTrackVelocityCommand(
            actuator,
            100.0,
            50.0
        )

        command.execute()
        command.undo()

        assertEquals(20.0, actuator.leftTrackVelocity)
        assertEquals(30.0, actuator.rightTrackVelocity)
    }
}