package com.acme.transport.network.node.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves weighted node thresholds through their life cycle and keeps a journal of what it did. */
data class WeightedNodeThresholdDesk(
    var store: WeightedNodeThresholdStore,
    var validator: WeightedNodeThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedNodeThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted node threshold $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedNodeThresholdStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted node threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedNodeThresholdStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("weighted node threshold $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newWeightedNodeThresholdDesk(store: WeightedNodeThresholdStore, clock: Clock, flags: Registry): WeightedNodeThresholdDesk {
    return WeightedNodeThresholdDesk(
        store = store,
        validator = defaultWeightedNodeThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedNodeThresholdEngine(flags),
    )
}
