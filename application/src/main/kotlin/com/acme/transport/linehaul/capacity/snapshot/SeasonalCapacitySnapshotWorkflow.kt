package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves seasonal capacity snapshots through their life cycle and keeps a journal of what it did. */
data class SeasonalCapacitySnapshotService(
    var store: SeasonalCapacitySnapshotStore,
    var validator: SeasonalCapacitySnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalCapacitySnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal capacity snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalCapacitySnapshotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal capacity snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalCapacitySnapshotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("seasonal capacity snapshot $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newSeasonalCapacitySnapshotService(store: SeasonalCapacitySnapshotStore, clock: Clock, flags: Registry): SeasonalCapacitySnapshotService {
    return SeasonalCapacitySnapshotService(
        store = store,
        validator = defaultSeasonalCapacitySnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalCapacitySnapshotEngine(flags),
    )
}
