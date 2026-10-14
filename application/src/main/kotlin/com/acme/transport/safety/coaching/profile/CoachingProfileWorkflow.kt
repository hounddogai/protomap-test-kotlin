package com.acme.transport.safety.coaching.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves coaching profiles through their life cycle and keeps a journal of what it did. */
data class CoachingProfileWorkflow(
    var store: CoachingProfileStore,
    var validator: CoachingProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CoachingProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("coaching profile $code not found")
        validator.requireValid(item)
        item.moveTo(CoachingProfileStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("coaching profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CoachingProfileStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("coaching profile $code not found")
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

fun newCoachingProfileWorkflow(store: CoachingProfileStore, clock: Clock, flags: Registry): CoachingProfileWorkflow {
    return CoachingProfileWorkflow(
        store = store,
        validator = defaultCoachingProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCoachingProfileEngine(flags),
    )
}
