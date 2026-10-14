package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves regional idling snapshots through their life cycle and keeps a journal of what it did. */
data class RegionalIdlingSnapshotWorkflow(
    var store: RegionalIdlingSnapshotStore,
    var validator: RegionalIdlingSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalIdlingSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional idling snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalIdlingSnapshotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional idling snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalIdlingSnapshotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional idling snapshot $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newRegionalIdlingSnapshotWorkflow(store: RegionalIdlingSnapshotStore, clock: Clock, flags: Registry): RegionalIdlingSnapshotWorkflow {
    return RegionalIdlingSnapshotWorkflow(
        store = store,
        validator = defaultRegionalIdlingSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalIdlingSnapshotEngine(flags),
    )
}
