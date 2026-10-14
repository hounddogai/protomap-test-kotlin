package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.linehaul.clock.Clock

/** Moves primary capacity snapshots through their life cycle and keeps a journal of what it did. */
data class PrimaryCapacitySnapshotService(
    var store: PrimaryCapacitySnapshotStore,
    var validator: PrimaryCapacitySnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary capacity snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryCapacitySnapshotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary capacity snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryCapacitySnapshotStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newPrimaryCapacitySnapshotService(store: PrimaryCapacitySnapshotStore, clock: Clock): PrimaryCapacitySnapshotService {
    return PrimaryCapacitySnapshotService(
        store = store,
        validator = defaultPrimaryCapacitySnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
