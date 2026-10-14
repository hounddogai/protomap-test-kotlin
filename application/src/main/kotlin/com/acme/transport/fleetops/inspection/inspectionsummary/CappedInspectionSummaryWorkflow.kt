package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.fleetops.clock.Clock

/** Moves capped inspection summaries through their life cycle and keeps a journal of what it did. */
data class CappedInspectionSummaryService(
    var store: CappedInspectionSummaryStore,
    var validator: CappedInspectionSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped inspection summary $code not found")
        validator.requireValid(item)
        item.moveTo(CappedInspectionSummaryStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped inspection summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedInspectionSummaryStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newCappedInspectionSummaryService(store: CappedInspectionSummaryStore, clock: Clock): CappedInspectionSummaryService {
    return CappedInspectionSummaryService(
        store = store,
        validator = defaultCappedInspectionSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
