package com.acme.transport.linehaul.tender.summary

import com.acme.transport.linehaul.clock.Clock

/** Moves tender summaries through their life cycle and keeps a journal of what it did. */
data class TenderSummaryManager(
    var store: TenderSummaryStore,
    var validator: TenderSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tender summary $code not found")
        validator.requireValid(item)
        item.moveTo(TenderSummaryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tender summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderSummaryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newTenderSummaryManager(store: TenderSummaryStore, clock: Clock): TenderSummaryManager {
    return TenderSummaryManager(
        store = store,
        validator = defaultTenderSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
