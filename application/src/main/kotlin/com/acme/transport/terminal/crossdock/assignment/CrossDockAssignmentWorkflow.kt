package com.acme.transport.terminal.crossdock.assignment

import com.acme.transport.terminal.clock.Clock

/** Moves cross dock assignments through their life cycle and keeps a journal of what it did. */
data class CrossDockAssignmentCoordinator(
    var store: CrossDockAssignmentStore,
    var validator: CrossDockAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("cross dock assignment $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockAssignmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockAssignmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            val problems = validator.validate(item)
            if (problems.size == 0) {
                continue
            }
            note("${item.code} needs attention: ${problems.joinToString(", ")}")
            changed += 1
        }
        return changed
    }
}

fun newCrossDockAssignmentCoordinator(store: CrossDockAssignmentStore, clock: Clock): CrossDockAssignmentCoordinator {
    return CrossDockAssignmentCoordinator(
        store = store,
        validator = defaultCrossDockAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
