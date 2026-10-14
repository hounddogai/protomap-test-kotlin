package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves adaptive idling segments through their life cycle and keeps a journal of what it did. */
data class AdaptiveIdlingSegmentManager(
    var store: AdaptiveIdlingSegmentStore,
    var validator: AdaptiveIdlingSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveIdlingSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive idling segment $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveIdlingSegmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive idling segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveIdlingSegmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("adaptive idling segment $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newAdaptiveIdlingSegmentManager(store: AdaptiveIdlingSegmentStore, clock: Clock, flags: Registry): AdaptiveIdlingSegmentManager {
    return AdaptiveIdlingSegmentManager(
        store = store,
        validator = defaultAdaptiveIdlingSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveIdlingSegmentEngine(flags),
    )
}
