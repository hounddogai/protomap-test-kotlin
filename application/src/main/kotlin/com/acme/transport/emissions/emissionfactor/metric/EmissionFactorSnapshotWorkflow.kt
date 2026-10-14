package com.acme.transport.emissions.emissionfactor.metric

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves emission factor snapshots through their life cycle and keeps a journal of what it did. */
data class EmissionFactorSnapshotManager(
    var store: EmissionFactorSnapshotStore,
    var validator: EmissionFactorSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EmissionFactorSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("emission factor snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorSnapshotStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorSnapshotStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("emission factor snapshot $code not found")
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

fun newEmissionFactorSnapshotManager(store: EmissionFactorSnapshotStore, clock: Clock, flags: Registry): EmissionFactorSnapshotManager {
    return EmissionFactorSnapshotManager(
        store = store,
        validator = defaultEmissionFactorSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEmissionFactorSnapshotEngine(flags),
    )
}
