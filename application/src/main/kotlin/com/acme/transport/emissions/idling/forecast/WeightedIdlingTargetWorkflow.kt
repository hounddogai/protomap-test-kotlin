package com.acme.transport.emissions.idling.forecast

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves weighted idling targets through their life cycle and keeps a journal of what it did. */
data class WeightedIdlingTargetCoordinator(
    var store: WeightedIdlingTargetStore,
    var validator: WeightedIdlingTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedIdlingTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted idling target $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedIdlingTargetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted idling target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedIdlingTargetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted idling target $code not found")
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

fun newWeightedIdlingTargetCoordinator(store: WeightedIdlingTargetStore, clock: Clock, flags: Registry): WeightedIdlingTargetCoordinator {
    return WeightedIdlingTargetCoordinator(
        store = store,
        validator = defaultWeightedIdlingTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedIdlingTargetEngine(flags),
    )
}
