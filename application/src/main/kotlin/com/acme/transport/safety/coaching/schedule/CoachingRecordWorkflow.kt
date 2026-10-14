package com.acme.transport.safety.coaching.schedule

import com.acme.transport.safety.clock.Clock

/** Moves coaching records through their life cycle and keeps a journal of what it did. */
data class CoachingRecordCoordinator(
    var store: CoachingRecordStore,
    var validator: CoachingRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("coaching record $code not found")
        validator.requireValid(item)
        item.moveTo(CoachingRecordStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("coaching record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CoachingRecordStatus.CANCELLED)
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

fun newCoachingRecordCoordinator(store: CoachingRecordStore, clock: Clock): CoachingRecordCoordinator {
    return CoachingRecordCoordinator(
        store = store,
        validator = defaultCoachingRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
