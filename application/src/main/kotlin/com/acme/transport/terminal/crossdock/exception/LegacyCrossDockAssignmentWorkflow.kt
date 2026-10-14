package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.terminal.clock.Clock

/** Moves legacy cross dock assignments through their life cycle and keeps a journal of what it did. */
data class LegacyCrossDockAssignmentCoordinator(
    var store: LegacyCrossDockAssignmentStore,
    var validator: LegacyCrossDockAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy cross dock assignment $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyCrossDockAssignmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy cross dock assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyCrossDockAssignmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newLegacyCrossDockAssignmentCoordinator(store: LegacyCrossDockAssignmentStore, clock: Clock): LegacyCrossDockAssignmentCoordinator {
    return LegacyCrossDockAssignmentCoordinator(
        store = store,
        validator = defaultLegacyCrossDockAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
