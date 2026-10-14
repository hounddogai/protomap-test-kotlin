package com.acme.transport.network.zone.baseline

import com.acme.transport.network.clock.Clock

/** Moves manual zone checkpoints through their life cycle and keeps a journal of what it did. */
data class ManualZoneCheckpointCoordinator(
    var store: ManualZoneCheckpointStore,
    var validator: ManualZoneCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual zone checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(ManualZoneCheckpointStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual zone checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualZoneCheckpointStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newManualZoneCheckpointCoordinator(store: ManualZoneCheckpointStore, clock: Clock): ManualZoneCheckpointCoordinator {
    return ManualZoneCheckpointCoordinator(
        store = store,
        validator = defaultManualZoneCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
