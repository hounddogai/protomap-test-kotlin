package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tire variances through their life cycle and keeps a journal of what it did. */
data class TireVarianceWorkflow(
    var store: TireVarianceStore,
    var validator: TireVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TireVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tire variance $code not found")
        validator.requireValid(item)
        item.moveTo(TireVarianceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tire variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireVarianceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tire variance $code not found")
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

fun newTireVarianceWorkflow(store: TireVarianceStore, clock: Clock, flags: Registry): TireVarianceWorkflow {
    return TireVarianceWorkflow(
        store = store,
        validator = defaultTireVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTireVarianceEngine(flags),
    )
}
