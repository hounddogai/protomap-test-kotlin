package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves ev charge snapshots through their life cycle and keeps a journal of what it did. */
data class EvChargeSnapshotWorkflow(
    var store: EvChargeSnapshotStore,
    var validator: EvChargeSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EvChargeSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("ev charge snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeSnapshotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeSnapshotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("ev charge snapshot $code not found")
        val result = engine.evaluate(item)
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

fun newEvChargeSnapshotWorkflow(store: EvChargeSnapshotStore, clock: Clock, flags: Registry): EvChargeSnapshotWorkflow {
    return EvChargeSnapshotWorkflow(
        store = store,
        validator = defaultEvChargeSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEvChargeSnapshotEngine(flags),
    )
}
