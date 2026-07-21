package command

/**
 * The Invoker. It runs commands and keeps an undo/redo history.
 */
class CommandInvoker {

    private val undoStack = ArrayDeque<Command>()
    private val redoStack = ArrayDeque<Command>()

    fun run(command: Command) {
        command.execute()
        undoStack.addLast(command)
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isEmpty()) {
            return
        }

        val command = undoStack.removeLast()

        command.undo()

        redoStack.addLast(command)
    }

    fun redo() {
        if (redoStack.isEmpty()) {
            return
        }

        val command = redoStack.removeLast()

        command.execute()

        undoStack.addLast(command)
    }

    fun canUndo() = undoStack.isNotEmpty()

    fun canRedo() = redoStack.isNotEmpty()
}