package com.acme.transport.safety.coaching.target

import com.acme.transport.safety.clock.Clock

/** Moves coaching targets through their life cycle and keeps a journal of what it did. */
data class CoachingTargetWorkflow(
    var store: CoachingTargetStore,
    var validator: CoachingTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("coaching target $code not found")
        validator.requireValid(item)
        item.moveTo(CoachingTargetStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("coaching target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CoachingTargetStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newCoachingTargetWorkflow(store: CoachingTargetStore, clock: Clock): CoachingTargetWorkflow {
    return CoachingTargetWorkflow(
        store = store,
        validator = defaultCoachingTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
