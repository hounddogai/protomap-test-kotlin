package com.acme.transport.fleetops.inspection.summary

import com.acme.transport.fleetops.clock.Clock

/** Moves inspection summaries through their life cycle and keeps a journal of what it did. */
data class InspectionSummaryManager(
    var store: InspectionSummaryStore,
    var validator: InspectionSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("inspection summary $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionSummaryStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionSummaryStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newInspectionSummaryManager(store: InspectionSummaryStore, clock: Clock): InspectionSummaryManager {
    return InspectionSummaryManager(
        store = store,
        validator = defaultInspectionSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
