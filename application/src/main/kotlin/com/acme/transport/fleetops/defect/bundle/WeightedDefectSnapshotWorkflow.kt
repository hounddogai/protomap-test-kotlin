package com.acme.transport.fleetops.defect.bundle

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves weighted defect snapshots through their life cycle and keeps a journal of what it did. */
data class WeightedDefectSnapshotCoordinator(
    var store: WeightedDefectSnapshotStore,
    var validator: WeightedDefectSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedDefectSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted defect snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedDefectSnapshotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted defect snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedDefectSnapshotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted defect snapshot $code not found")
        val result = engine.compute(item)
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

fun newWeightedDefectSnapshotCoordinator(store: WeightedDefectSnapshotStore, clock: Clock, flags: Registry): WeightedDefectSnapshotCoordinator {
    return WeightedDefectSnapshotCoordinator(
        store = store,
        validator = defaultWeightedDefectSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedDefectSnapshotEngine(flags),
    )
}
