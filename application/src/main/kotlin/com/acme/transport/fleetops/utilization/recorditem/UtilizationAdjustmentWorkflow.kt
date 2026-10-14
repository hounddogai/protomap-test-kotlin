package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves utilization adjustments through their life cycle and keeps a journal of what it did. */
data class UtilizationAdjustmentCoordinator(
    var store: UtilizationAdjustmentStore,
    var validator: UtilizationAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: UtilizationAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("utilization adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationAdjustmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationAdjustmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("utilization adjustment $code not found")
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

fun newUtilizationAdjustmentCoordinator(store: UtilizationAdjustmentStore, clock: Clock, flags: Registry): UtilizationAdjustmentCoordinator {
    return UtilizationAdjustmentCoordinator(
        store = store,
        validator = defaultUtilizationAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultUtilizationAdjustmentEngine(flags),
    )
}
