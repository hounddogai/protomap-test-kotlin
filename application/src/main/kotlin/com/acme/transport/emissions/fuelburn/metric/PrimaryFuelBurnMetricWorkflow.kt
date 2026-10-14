package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves primary fuel burn metrics through their life cycle and keeps a journal of what it did. */
data class PrimaryFuelBurnMetricCoordinator(
    var store: PrimaryFuelBurnMetricStore,
    var validator: PrimaryFuelBurnMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryFuelBurnMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary fuel burn metric $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryFuelBurnMetricStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary fuel burn metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryFuelBurnMetricStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("primary fuel burn metric $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newPrimaryFuelBurnMetricCoordinator(store: PrimaryFuelBurnMetricStore, clock: Clock, flags: Registry): PrimaryFuelBurnMetricCoordinator {
    return PrimaryFuelBurnMetricCoordinator(
        store = store,
        validator = defaultPrimaryFuelBurnMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryFuelBurnMetricEngine(flags),
    )
}
