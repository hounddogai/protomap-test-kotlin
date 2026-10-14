package com.acme.transport.linehaul.capacity.slot

import com.acme.transport.linehaul.clock.Clock

/** Moves adaptive capacity snapshots through their life cycle and keeps a journal of what it did. */
data class AdaptiveCapacitySnapshotWorkflow(
    var store: AdaptiveCapacitySnapshotStore,
    var validator: AdaptiveCapacitySnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive capacity snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveCapacitySnapshotStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive capacity snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveCapacitySnapshotStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newAdaptiveCapacitySnapshotWorkflow(store: AdaptiveCapacitySnapshotStore, clock: Clock): AdaptiveCapacitySnapshotWorkflow {
    return AdaptiveCapacitySnapshotWorkflow(
        store = store,
        validator = defaultAdaptiveCapacitySnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
