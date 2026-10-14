package com.acme.transport.network.transittime.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves strict transit time baselines through their life cycle and keeps a journal of what it did. */
data class StrictTransitTimeBaselineWorkflow(
    var store: StrictTransitTimeBaselineStore,
    var validator: StrictTransitTimeBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictTransitTimeBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict transit time baseline $code not found")
        validator.requireValid(item)
        item.moveTo(StrictTransitTimeBaselineStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict transit time baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictTransitTimeBaselineStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("strict transit time baseline $code not found")
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

fun newStrictTransitTimeBaselineWorkflow(store: StrictTransitTimeBaselineStore, clock: Clock, flags: Registry): StrictTransitTimeBaselineWorkflow {
    return StrictTransitTimeBaselineWorkflow(
        store = store,
        validator = defaultStrictTransitTimeBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictTransitTimeBaselineEngine(flags),
    )
}
