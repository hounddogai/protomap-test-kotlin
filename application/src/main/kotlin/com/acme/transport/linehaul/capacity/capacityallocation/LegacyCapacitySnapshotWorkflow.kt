package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.linehaul.clock.Clock

/** Moves legacy capacity snapshots through their life cycle and keeps a journal of what it did. */
data class LegacyCapacitySnapshotDesk(
    var store: LegacyCapacitySnapshotStore,
    var validator: LegacyCapacitySnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyCapacitySnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy capacity snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyCapacitySnapshotStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy capacity snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyCapacitySnapshotStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("legacy capacity snapshot $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newLegacyCapacitySnapshotDesk(store: LegacyCapacitySnapshotStore, clock: Clock): LegacyCapacitySnapshotDesk {
    return LegacyCapacitySnapshotDesk(
        store = store,
        validator = defaultLegacyCapacitySnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyCapacitySnapshotEngine(),
    )
}
