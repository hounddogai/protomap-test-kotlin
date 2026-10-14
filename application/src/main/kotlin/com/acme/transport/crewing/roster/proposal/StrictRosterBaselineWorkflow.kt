package com.acme.transport.crewing.roster.proposal

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves strict roster baselines through their life cycle and keeps a journal of what it did. */
data class StrictRosterBaselineWorkflow(
    var store: StrictRosterBaselineStore,
    var validator: StrictRosterBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictRosterBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict roster baseline $code not found")
        validator.requireValid(item)
        item.moveTo(StrictRosterBaselineStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict roster baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictRosterBaselineStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict roster baseline $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newStrictRosterBaselineWorkflow(store: StrictRosterBaselineStore, clock: Clock, flags: Registry): StrictRosterBaselineWorkflow {
    return StrictRosterBaselineWorkflow(
        store = store,
        validator = defaultStrictRosterBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictRosterBaselineEngine(flags),
    )
}
