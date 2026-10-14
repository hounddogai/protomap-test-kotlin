package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves weighted route offset templates through their life cycle and keeps a journal of what it did.
 */
data class WeightedRouteOffsetTemplateCoordinator(
    var store: WeightedRouteOffsetTemplateStore,
    var validator: WeightedRouteOffsetTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedRouteOffsetTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted route offset template $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedRouteOffsetTemplateStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted route offset template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedRouteOffsetTemplateStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted route offset template $code not found")
        val result = engine.compute(item)
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

fun newWeightedRouteOffsetTemplateCoordinator(store: WeightedRouteOffsetTemplateStore, clock: Clock, flags: Registry): WeightedRouteOffsetTemplateCoordinator {
    return WeightedRouteOffsetTemplateCoordinator(
        store = store,
        validator = defaultWeightedRouteOffsetTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedRouteOffsetTemplateEngine(flags),
    )
}
