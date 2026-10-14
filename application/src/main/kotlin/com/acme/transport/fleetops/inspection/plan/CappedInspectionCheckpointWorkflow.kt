package com.acme.transport.fleetops.inspection.plan

import com.acme.transport.fleetops.clock.Clock

/** Moves capped inspection checkpoints through their life cycle and keeps a journal of what it did. */
data class CappedInspectionCheckpointManager(
    var store: CappedInspectionCheckpointStore,
    var validator: CappedInspectionCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped inspection checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(CappedInspectionCheckpointStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped inspection checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedInspectionCheckpointStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newCappedInspectionCheckpointManager(store: CappedInspectionCheckpointStore, clock: Clock): CappedInspectionCheckpointManager {
    return CappedInspectionCheckpointManager(
        store = store,
        validator = defaultCappedInspectionCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
