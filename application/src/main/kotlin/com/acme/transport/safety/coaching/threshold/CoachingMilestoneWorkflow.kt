package com.acme.transport.safety.coaching.threshold

import com.acme.transport.safety.clock.Clock

/** Moves coaching milestones through their life cycle and keeps a journal of what it did. */
data class CoachingMilestoneService(
    var store: CoachingMilestoneStore,
    var validator: CoachingMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CoachingMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("coaching milestone $code not found")
        validator.requireValid(item)
        item.moveTo(CoachingMilestoneStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("coaching milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CoachingMilestoneStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("coaching milestone $code not found")
        val result = engine.run(item)
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

fun newCoachingMilestoneService(store: CoachingMilestoneStore, clock: Clock): CoachingMilestoneService {
    return CoachingMilestoneService(
        store = store,
        validator = defaultCoachingMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCoachingMilestoneEngine(),
    )
}
