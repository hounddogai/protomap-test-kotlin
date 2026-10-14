package com.acme.transport.lastmile.attempt.attemptplan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves strict attempt plans through their life cycle and keeps a journal of what it did. */
data class StrictAttemptPlanDesk(
    var store: StrictAttemptPlanStore,
    var validator: StrictAttemptPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictAttemptPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict attempt plan $code not found")
        validator.requireValid(item)
        item.moveTo(StrictAttemptPlanStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict attempt plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictAttemptPlanStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict attempt plan $code not found")
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

fun newStrictAttemptPlanDesk(store: StrictAttemptPlanStore, clock: Clock, flags: Registry): StrictAttemptPlanDesk {
    return StrictAttemptPlanDesk(
        store = store,
        validator = defaultStrictAttemptPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictAttemptPlanEngine(flags),
    )
}
