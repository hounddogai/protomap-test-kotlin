package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves coaching windows through their life cycle and keeps a journal of what it did. */
data class CoachingWindowWorkflow(
    var store: CoachingWindowStore,
    var validator: CoachingWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CoachingWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("coaching window $code not found")
        validator.requireValid(item)
        item.moveTo(CoachingWindowStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("coaching window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CoachingWindowStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("coaching window $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newCoachingWindowWorkflow(store: CoachingWindowStore, clock: Clock, flags: Registry): CoachingWindowWorkflow {
    return CoachingWindowWorkflow(
        store = store,
        validator = defaultCoachingWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCoachingWindowEngine(flags),
    )
}
