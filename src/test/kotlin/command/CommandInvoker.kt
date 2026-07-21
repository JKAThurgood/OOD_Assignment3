package command

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CommandInvokerTest {

    @Test
    fun run_executes_command() {

        var executed = false

        val command = object : Command {

            override fun execute() {
                executed = true
            }

            override fun undo() {}
        }

        val invoker = CommandInvoker()

        invoker.run(command)

        assertTrue(executed)
    }

    @Test
    fun undo_calls_command_undo() {

        var undone = false

        val command = object : Command {

            override fun execute() {}

            override fun undo() {
                undone = true
            }
        }

        val invoker = CommandInvoker()

        invoker.run(command)
        invoker.undo()

        assertTrue(undone)
    }

    @Test
    fun redo_reexecutes_command() {

        var executeCount = 0

        val command = object : Command {

            override fun execute() {
                executeCount++
            }

            override fun undo() {}
        }

        val invoker = CommandInvoker()

        invoker.run(command)
        invoker.undo()
        invoker.redo()

        assertEquals(2, executeCount)
    }

    @Test
    fun running_new_command_clears_redo_history() {

        val invoker = CommandInvoker()

        val command1 = object : Command {
            override fun execute() {}
            override fun undo() {}
        }

        val command2 = object : Command {
            override fun execute() {}
            override fun undo() {}
        }

        invoker.run(command1)

        invoker.undo()

        assertTrue(invoker.canRedo())

        invoker.run(command2)

        assertFalse(invoker.canRedo())
    }

    @Test
    fun canRedo_reflects_redo_history() {

        val invoker = CommandInvoker()

        val command = object : Command {
            override fun execute() {}
            override fun undo() {}
        }

        invoker.run(command)

        assertFalse(invoker.canRedo())

        invoker.undo()

        assertTrue(invoker.canRedo())
    }
}