package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.terminal.clock.Clock

/** Moves sortation assignments through their life cycle and keeps a journal of what it did. */
data class SortationAssignmentDesk(
    var store: SortationAssignmentStore,
    var validator: SortationAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("sortation assignment $code not found")
        validator.requireValid(item)
        item.moveTo(SortationAssignmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationAssignmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newSortationAssignmentDesk(store: SortationAssignmentStore, clock: Clock): SortationAssignmentDesk {
    return SortationAssignmentDesk(
        store = store,
        validator = defaultSortationAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
