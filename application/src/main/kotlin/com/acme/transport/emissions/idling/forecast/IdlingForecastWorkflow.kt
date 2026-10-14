package com.acme.transport.emissions.idling.forecast

import com.acme.transport.emissions.clock.Clock

/** Moves idling forecasts through their life cycle and keeps a journal of what it did. */
data class IdlingForecastWorkflow(
    var store: IdlingForecastStore,
    var validator: IdlingForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("idling forecast $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingForecastStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("idling forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingForecastStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newIdlingForecastWorkflow(store: IdlingForecastStore, clock: Clock): IdlingForecastWorkflow {
    return IdlingForecastWorkflow(
        store = store,
        validator = defaultIdlingForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
