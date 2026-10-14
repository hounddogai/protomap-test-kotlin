package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.safety.clock.Clock

/** Moves adaptive scorecard segments through their life cycle and keeps a journal of what it did. */
data class AdaptiveScorecardSegmentDesk(
    var store: AdaptiveScorecardSegmentStore,
    var validator: AdaptiveScorecardSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveScorecardSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive scorecard segment $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveScorecardSegmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive scorecard segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveScorecardSegmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive scorecard segment $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newAdaptiveScorecardSegmentDesk(store: AdaptiveScorecardSegmentStore, clock: Clock): AdaptiveScorecardSegmentDesk {
    return AdaptiveScorecardSegmentDesk(
        store = store,
        validator = defaultAdaptiveScorecardSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveScorecardSegmentEngine(),
    )
}
