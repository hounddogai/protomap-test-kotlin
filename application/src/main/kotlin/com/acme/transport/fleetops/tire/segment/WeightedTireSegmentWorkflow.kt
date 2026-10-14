package com.acme.transport.fleetops.tire.segment

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves weighted tire segments through their life cycle and keeps a journal of what it did. */
data class WeightedTireSegmentDesk(
    var store: WeightedTireSegmentStore,
    var validator: WeightedTireSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedTireSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted tire segment $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedTireSegmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted tire segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedTireSegmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("weighted tire segment $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newWeightedTireSegmentDesk(store: WeightedTireSegmentStore, clock: Clock, flags: Registry): WeightedTireSegmentDesk {
    return WeightedTireSegmentDesk(
        store = store,
        validator = defaultWeightedTireSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedTireSegmentEngine(flags),
    )
}
