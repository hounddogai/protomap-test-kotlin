package com.acme.transport.network.transittime.proposal

import com.acme.transport.network.clock.Clock

/** Moves seasonal transit time entries through their life cycle and keeps a journal of what it did. */
data class SeasonalTransitTimeEntryWorkflow(
    var store: SeasonalTransitTimeEntryStore,
    var validator: SeasonalTransitTimeEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal transit time entry $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalTransitTimeEntryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal transit time entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalTransitTimeEntryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newSeasonalTransitTimeEntryWorkflow(store: SeasonalTransitTimeEntryStore, clock: Clock): SeasonalTransitTimeEntryWorkflow {
    return SeasonalTransitTimeEntryWorkflow(
        store = store,
        validator = defaultSeasonalTransitTimeEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
