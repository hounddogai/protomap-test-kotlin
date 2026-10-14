package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves weighted capacity drafts through their life cycle and keeps a journal of what it did. */
data class WeightedCapacityDraftCoordinator(
    var store: WeightedCapacityDraftStore,
    var validator: WeightedCapacityDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedCapacityDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted capacity draft $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedCapacityDraftStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted capacity draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedCapacityDraftStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("weighted capacity draft $code not found")
        val result = engine.run(item)
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

fun newWeightedCapacityDraftCoordinator(store: WeightedCapacityDraftStore, clock: Clock, flags: Registry): WeightedCapacityDraftCoordinator {
    return WeightedCapacityDraftCoordinator(
        store = store,
        validator = defaultWeightedCapacityDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedCapacityDraftEngine(flags),
    )
}
