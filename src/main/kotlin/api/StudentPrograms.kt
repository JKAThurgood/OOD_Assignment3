package api

import program.BallSeekProgram
import program.LineFollowerProgram
import program.TemperatureSeekProgram

/**
 * The one place programs are registered with the system. Each program you register shows up in the
 * "Program" dropdown and can be launched with "Run Program".
 */
object StudentPrograms {
    fun registerAll(registry: ProgramRegistry) {
        registry.register(LineFollowerProgram())
        registry.register(TemperatureSeekProgram())
        registry.register(BallSeekProgram())
    }
}