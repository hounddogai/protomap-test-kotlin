package com.acme.transport.customs.inspection.milestone

import com.acme.transport.customs.clock.Clock

/** Moves inspection checkpoints through their life cycle and keeps a journal of what it did. */
data class InspectionCheckpointDesk(
    var store: InspectionCheckpointStore,
    var validator: InspectionCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("inspection checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionCheckpointStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionCheckpointStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newInspectionCheckpointDesk(store: InspectionCheckpointStore, clock: Clock): InspectionCheckpointDesk {
    return InspectionCheckpointDesk(
        store = store,
        validator = defaultInspectionCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
