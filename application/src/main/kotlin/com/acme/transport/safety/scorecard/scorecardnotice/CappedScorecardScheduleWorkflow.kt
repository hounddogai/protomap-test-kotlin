package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves capped scorecard schedules through their life cycle and keeps a journal of what it did. */
data class CappedScorecardScheduleDesk(
    var store: CappedScorecardScheduleStore,
    var validator: CappedScorecardScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedScorecardScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped scorecard schedule $code not found")
        validator.requireValid(item)
        item.moveTo(CappedScorecardScheduleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped scorecard schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedScorecardScheduleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped scorecard schedule $code not found")
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

fun newCappedScorecardScheduleDesk(store: CappedScorecardScheduleStore, clock: Clock, flags: Registry): CappedScorecardScheduleDesk {
    return CappedScorecardScheduleDesk(
        store = store,
        validator = defaultCappedScorecardScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedScorecardScheduleEngine(flags),
    )
}
