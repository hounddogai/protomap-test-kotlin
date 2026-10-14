package com.acme.transport.terminal.dwell.checkpoint

import com.acme.transport.terminal.clock.Clock

/** Moves dwell checkpoints through their life cycle and keeps a journal of what it did. */
data class DwellCheckpointCoordinator(
    var store: DwellCheckpointStore,
    var validator: DwellCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dwell checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(DwellCheckpointStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellCheckpointStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newDwellCheckpointCoordinator(store: DwellCheckpointStore, clock: Clock): DwellCheckpointCoordinator {
    return DwellCheckpointCoordinator(
        store = store,
        validator = defaultDwellCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
