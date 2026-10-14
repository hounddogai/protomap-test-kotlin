package com.acme.transport.safety.coaching.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves seasonal coaching requests through their life cycle and keeps a journal of what it did. */
data class SeasonalCoachingRequestWorkflow(
    var store: SeasonalCoachingRequestStore,
    var validator: SeasonalCoachingRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalCoachingRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal coaching request $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalCoachingRequestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal coaching request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalCoachingRequestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("seasonal coaching request $code not found")
        val result = engine.price(item)
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

fun newSeasonalCoachingRequestWorkflow(store: SeasonalCoachingRequestStore, clock: Clock, flags: Registry): SeasonalCoachingRequestWorkflow {
    return SeasonalCoachingRequestWorkflow(
        store = store,
        validator = defaultSeasonalCoachingRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalCoachingRequestEngine(flags),
    )
}
