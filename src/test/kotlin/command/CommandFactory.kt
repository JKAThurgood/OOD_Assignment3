package command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CommandFactoryTest {

    private class FakeActuator : RobotActuator {

        override var leftTrackVelocity = 0.0
            private set

        override var rightTrackVelocity = 0.0
            private set

        override fun setTrackVelocities(
            left: Double,
            right: Double
        ) {
            leftTrackVelocity = left
            rightTrackVelocity = right
        }
    }

    @Test
    fun stop_sets_both_tracks_to_zero() {

        val actuator = FakeActuator()

        CommandFactory.stop(actuator).execute()

        assertEquals(0.0, actuator.leftTrackVelocity)
        assertEquals(0.0, actuator.rightTrackVelocity)
    }

    @Test
    fun forward_sets_both_tracks_forward() {

        val actuator = FakeActuator()

        CommandFactory.forward(
            actuator,
            100.0
        ).execute()

        assertEquals(100.0, actuator.leftTrackVelocity)
        assertEquals(100.0, actuator.rightTrackVelocity)
    }

    @Test
    fun reverse_sets_both_tracks_reverse() {

        val actuator = FakeActuator()

        CommandFactory.reverse(
            actuator,
            100.0
        ).execute()

        assertEquals(-100.0, actuator.leftTrackVelocity)
        assertEquals(-100.0, actuator.rightTrackVelocity)
    }

    @Test
    fun turnLeft_spins_robot_left() {

        val actuator = FakeActuator()

        CommandFactory.turnLeft(
            actuator,
            50.0
        ).execute()

        assertEquals(-50.0, actuator.leftTrackVelocity)
        assertEquals(50.0, actuator.rightTrackVelocity)
    }

    @Test
    fun turnRight_spins_robot_right() {

        val actuator = FakeActuator()

        CommandFactory.turnRight(
            actuator,
            50.0
        ).execute()

        assertEquals(50.0, actuator.leftTrackVelocity)
        assertEquals(-50.0, actuator.rightTrackVelocity)
    }

    @Test
    fun setTracks_sets_individual_track_speeds() {

        val actuator = FakeActuator()

        CommandFactory.setTracks(
            actuator,
            25.0,
            75.0
        ).execute()

        assertEquals(25.0, actuator.leftTrackVelocity)
        assertEquals(75.0, actuator.rightTrackVelocity)
    }

    @Test
    fun factory_commands_support_undo() {

        val actuator = FakeActuator()

        actuator.setTrackVelocities(10.0, 20.0)

        val command = CommandFactory.forward(
            actuator,
            100.0
        )

        command.execute()
        command.undo()

        assertEquals(10.0, actuator.leftTrackVelocity)
        assertEquals(20.0, actuator.rightTrackVelocity)
    }
}