package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.safety.clock.Clock

/** Moves scorecard schedules through their life cycle and keeps a journal of what it did. */
data class ScorecardScheduleCoordinator(
    var store: ScorecardScheduleStore,
    var validator: ScorecardScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scorecard schedule $code not found")
        validator.requireValid(item)
        item.moveTo(ScorecardScheduleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scorecard schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScorecardScheduleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newScorecardScheduleCoordinator(store: ScorecardScheduleStore, clock: Clock): ScorecardScheduleCoordinator {
    return ScorecardScheduleCoordinator(
        store = store,
        validator = defaultScorecardScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
