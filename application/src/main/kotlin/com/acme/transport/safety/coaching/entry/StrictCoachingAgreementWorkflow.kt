package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves strict coaching agreements through their life cycle and keeps a journal of what it did. */
data class StrictCoachingAgreementWorkflow(
    var store: StrictCoachingAgreementStore,
    var validator: StrictCoachingAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictCoachingAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict coaching agreement $code not found")
        validator.requireValid(item)
        item.moveTo(StrictCoachingAgreementStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict coaching agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictCoachingAgreementStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("strict coaching agreement $code not found")
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

fun newStrictCoachingAgreementWorkflow(store: StrictCoachingAgreementStore, clock: Clock, flags: Registry): StrictCoachingAgreementWorkflow {
    return StrictCoachingAgreementWorkflow(
        store = store,
        validator = defaultStrictCoachingAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictCoachingAgreementEngine(flags),
    )
}
