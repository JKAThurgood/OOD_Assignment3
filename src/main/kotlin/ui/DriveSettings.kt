package ui

import observer.AbstractSubject

data class DriveConfig(
    val speed: Double,
    val turn: Double
)

class DriveSettings(
    speed: Double = 120.0,
    turn: Double = 90.0
) : AbstractSubject<DriveConfig>() {

    var speed: Double = speed
        set(value) {
            field = value
            notifyObservers(DriveConfig(field, turn))
        }

    var turn: Double = turn
        set(value) {
            field = value
            notifyObservers(DriveConfig(speed, field))
        }
}