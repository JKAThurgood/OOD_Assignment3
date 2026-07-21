package ui

import observer.Observer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DriveSettingsTest {

    @Test
    fun initial_values_are_set() {
        val settings = DriveSettings(
            speed = 100.0,
            turn = 25.0
        )

        assertEquals(100.0, settings.speed)
        assertEquals(25.0, settings.turn)
    }

    @Test
    fun changing_speed_notifies_observers() {

        val settings = DriveSettings()

        var received: DriveConfig? = null

        settings.subscribe(
            Observer { config ->
                received = config
            }
        )

        settings.speed = 120.0

        assertEquals(
            DriveConfig(
                speed = 120.0,
                turn = 50.0
            ),
            received
        )
    }

    @Test
    fun changing_turn_notifies_observers() {

        val settings = DriveSettings()

        var received: DriveConfig? = null

        settings.subscribe(
            Observer { config ->
                received = config
            }
        )

        settings.turn = 35.0

        assertEquals(
            DriveConfig(
                speed = 80.0,
                turn = 35.0
            ),
            received
        )
    }

    @Test
    fun speed_change_preserves_current_turn() {

        val settings = DriveSettings(
            speed = 80.0,
            turn = 20.0
        )

        var received: DriveConfig? = null

        settings.subscribe(
            Observer { config ->
                received = config
            }
        )

        settings.speed = 150.0

        assertEquals(150.0, received?.speed)
        assertEquals(20.0, received?.turn)
    }

    @Test
    fun turn_change_preserves_current_speed() {

        val settings = DriveSettings(
            speed = 90.0,
            turn = 30.0
        )

        var received: DriveConfig? = null

        settings.subscribe(
            Observer { config ->
                received = config
            }
        )

        settings.turn = 45.0

        assertEquals(90.0, received?.speed)
        assertEquals(45.0, received?.turn)
    }

    @Test
    fun multiple_observers_receive_updates() {

        val settings = DriveSettings()

        var observer1: DriveConfig? = null
        var observer2: DriveConfig? = null

        settings.subscribe(Observer { observer1 = it })
        settings.subscribe(Observer { observer2 = it })

        settings.speed = 200.0

        assertEquals(
            DriveConfig(200.0, 50.0),
            observer1
        )

        assertEquals(
            DriveConfig(200.0, 50.0),
            observer2
        )
    }
}