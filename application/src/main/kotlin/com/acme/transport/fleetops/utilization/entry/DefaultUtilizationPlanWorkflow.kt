package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default utilization plans through their life cycle and keeps a journal of what it did. */
data class DefaultUtilizationPlanManager(
    var store: DefaultUtilizationPlanStore,
    var validator: DefaultUtilizationPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultUtilizationPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default utilization plan $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultUtilizationPlanStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default utilization plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultUtilizationPlanStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("default utilization plan $code not found")
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

fun newDefaultUtilizationPlanManager(store: DefaultUtilizationPlanStore, clock: Clock, flags: Registry): DefaultUtilizationPlanManager {
    return DefaultUtilizationPlanManager(
        store = store,
        validator = defaultDefaultUtilizationPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultUtilizationPlanEngine(flags),
    )
}
