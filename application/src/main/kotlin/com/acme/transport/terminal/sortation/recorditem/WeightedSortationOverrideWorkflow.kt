package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves weighted sortation overrides through their life cycle and keeps a journal of what it did. */
data class WeightedSortationOverrideService(
    var store: WeightedSortationOverrideStore,
    var validator: WeightedSortationOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedSortationOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted sortation override $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedSortationOverrideStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted sortation override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedSortationOverrideStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted sortation override $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newWeightedSortationOverrideService(store: WeightedSortationOverrideStore, clock: Clock, flags: Registry): WeightedSortationOverrideService {
    return WeightedSortationOverrideService(
        store = store,
        validator = defaultWeightedSortationOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedSortationOverrideEngine(flags),
    )
}
