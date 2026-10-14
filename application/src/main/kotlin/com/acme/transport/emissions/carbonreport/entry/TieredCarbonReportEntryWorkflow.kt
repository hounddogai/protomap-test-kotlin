package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.emissions.clock.Clock

/** Moves tiered carbon report entries through their life cycle and keeps a journal of what it did. */
data class TieredCarbonReportEntryCoordinator(
    var store: TieredCarbonReportEntryStore,
    var validator: TieredCarbonReportEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered carbon report entry $code not found")
        validator.requireValid(item)
        item.moveTo(TieredCarbonReportEntryStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered carbon report entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredCarbonReportEntryStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newTieredCarbonReportEntryCoordinator(store: TieredCarbonReportEntryStore, clock: Clock): TieredCarbonReportEntryCoordinator {
    return TieredCarbonReportEntryCoordinator(
        store = store,
        validator = defaultTieredCarbonReportEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
