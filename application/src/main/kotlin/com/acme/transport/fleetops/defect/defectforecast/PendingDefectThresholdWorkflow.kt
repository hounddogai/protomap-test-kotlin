package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves pending defect thresholds through their life cycle and keeps a journal of what it did. */
data class PendingDefectThresholdCoordinator(
    var store: PendingDefectThresholdStore,
    var validator: PendingDefectThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingDefectThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending defect threshold $code not found")
        validator.requireValid(item)
        item.moveTo(PendingDefectThresholdStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending defect threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingDefectThresholdStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("pending defect threshold $code not found")
        val result = engine.run(item)
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

fun newPendingDefectThresholdCoordinator(store: PendingDefectThresholdStore, clock: Clock, flags: Registry): PendingDefectThresholdCoordinator {
    return PendingDefectThresholdCoordinator(
        store = store,
        validator = defaultPendingDefectThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingDefectThresholdEngine(flags),
    )
}
