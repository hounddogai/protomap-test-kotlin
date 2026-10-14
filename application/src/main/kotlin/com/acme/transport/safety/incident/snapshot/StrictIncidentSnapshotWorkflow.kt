package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves strict incident snapshots through their life cycle and keeps a journal of what it did. */
data class StrictIncidentSnapshotService(
    var store: StrictIncidentSnapshotStore,
    var validator: StrictIncidentSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictIncidentSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict incident snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(StrictIncidentSnapshotStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("strict incident snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictIncidentSnapshotStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("strict incident snapshot $code not found")
        val result = engine.price(item)
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

fun newStrictIncidentSnapshotService(store: StrictIncidentSnapshotStore, clock: Clock, flags: Registry): StrictIncidentSnapshotService {
    return StrictIncidentSnapshotService(
        store = store,
        validator = defaultStrictIncidentSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictIncidentSnapshotEngine(flags),
    )
}
