package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves weighted incident snapshots through their life cycle and keeps a journal of what it did. */
data class WeightedIncidentSnapshotManager(
    var store: WeightedIncidentSnapshotStore,
    var validator: WeightedIncidentSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedIncidentSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted incident snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedIncidentSnapshotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted incident snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedIncidentSnapshotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("weighted incident snapshot $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newWeightedIncidentSnapshotManager(store: WeightedIncidentSnapshotStore, clock: Clock, flags: Registry): WeightedIncidentSnapshotManager {
    return WeightedIncidentSnapshotManager(
        store = store,
        validator = defaultWeightedIncidentSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedIncidentSnapshotEngine(flags),
    )
}
