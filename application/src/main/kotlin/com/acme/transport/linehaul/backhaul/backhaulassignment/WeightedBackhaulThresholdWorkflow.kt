package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves weighted backhaul thresholds through their life cycle and keeps a journal of what it did. */
data class WeightedBackhaulThresholdService(
    var store: WeightedBackhaulThresholdStore,
    var validator: WeightedBackhaulThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedBackhaulThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted backhaul threshold $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedBackhaulThresholdStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted backhaul threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedBackhaulThresholdStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("weighted backhaul threshold $code not found")
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

fun newWeightedBackhaulThresholdService(store: WeightedBackhaulThresholdStore, clock: Clock, flags: Registry): WeightedBackhaulThresholdService {
    return WeightedBackhaulThresholdService(
        store = store,
        validator = defaultWeightedBackhaulThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedBackhaulThresholdEngine(flags),
    )
}
