package com.acme.transport.crewing.availability.threshold

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves weighted availability metrics through their life cycle and keeps a journal of what it did. */
data class WeightedAvailabilityMetricCoordinator(
    var store: WeightedAvailabilityMetricStore,
    var validator: WeightedAvailabilityMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedAvailabilityMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted availability metric $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedAvailabilityMetricStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted availability metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedAvailabilityMetricStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("weighted availability metric $code not found")
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

fun newWeightedAvailabilityMetricCoordinator(store: WeightedAvailabilityMetricStore, clock: Clock, flags: Registry): WeightedAvailabilityMetricCoordinator {
    return WeightedAvailabilityMetricCoordinator(
        store = store,
        validator = defaultWeightedAvailabilityMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedAvailabilityMetricEngine(flags),
    )
}
