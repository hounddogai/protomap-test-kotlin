package com.acme.transport.fleetops.utilization.score

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves utilization metrics through their life cycle and keeps a journal of what it did. */
data class UtilizationMetricCoordinator(
    var store: UtilizationMetricStore,
    var validator: UtilizationMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: UtilizationMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("utilization metric $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationMetricStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationMetricStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("utilization metric $code not found")
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

fun newUtilizationMetricCoordinator(store: UtilizationMetricStore, clock: Clock, flags: Registry): UtilizationMetricCoordinator {
    return UtilizationMetricCoordinator(
        store = store,
        validator = defaultUtilizationMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultUtilizationMetricEngine(flags),
    )
}
