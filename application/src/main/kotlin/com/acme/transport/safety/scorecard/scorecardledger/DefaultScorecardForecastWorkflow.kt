package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.safety.clock.Clock

/** Moves default scorecard forecasts through their life cycle and keeps a journal of what it did. */
data class DefaultScorecardForecastWorkflow(
    var store: DefaultScorecardForecastStore,
    var validator: DefaultScorecardForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default scorecard forecast $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultScorecardForecastStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default scorecard forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultScorecardForecastStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newDefaultScorecardForecastWorkflow(store: DefaultScorecardForecastStore, clock: Clock): DefaultScorecardForecastWorkflow {
    return DefaultScorecardForecastWorkflow(
        store = store,
        validator = defaultDefaultScorecardForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
