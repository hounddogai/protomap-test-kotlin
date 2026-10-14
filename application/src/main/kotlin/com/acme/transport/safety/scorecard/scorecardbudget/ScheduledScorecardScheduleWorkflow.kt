package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.safety.clock.Clock

/** Moves scheduled scorecard schedules through their life cycle and keeps a journal of what it did. */
data class ScheduledScorecardScheduleCoordinator(
    var store: ScheduledScorecardScheduleStore,
    var validator: ScheduledScorecardScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled scorecard schedule $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledScorecardScheduleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled scorecard schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledScorecardScheduleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newScheduledScorecardScheduleCoordinator(store: ScheduledScorecardScheduleStore, clock: Clock): ScheduledScorecardScheduleCoordinator {
    return ScheduledScorecardScheduleCoordinator(
        store = store,
        validator = defaultScheduledScorecardScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
