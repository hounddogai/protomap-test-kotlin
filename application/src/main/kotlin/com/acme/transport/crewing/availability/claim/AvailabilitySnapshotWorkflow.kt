package com.acme.transport.crewing.availability.claim

import com.acme.transport.crewing.clock.Clock

/** Moves availability snapshots through their life cycle and keeps a journal of what it did. */
data class AvailabilitySnapshotService(
    var store: AvailabilitySnapshotStore,
    var validator: AvailabilitySnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("availability snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(AvailabilitySnapshotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("availability snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AvailabilitySnapshotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newAvailabilitySnapshotService(store: AvailabilitySnapshotStore, clock: Clock): AvailabilitySnapshotService {
    return AvailabilitySnapshotService(
        store = store,
        validator = defaultAvailabilitySnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
