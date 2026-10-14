package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.lastmile.clock.Clock

/** Moves scheduled parcel summaries through their life cycle and keeps a journal of what it did. */
data class ScheduledParcelSummaryService(
    var store: ScheduledParcelSummaryStore,
    var validator: ScheduledParcelSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled parcel summary $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledParcelSummaryStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled parcel summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledParcelSummaryStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newScheduledParcelSummaryService(store: ScheduledParcelSummaryStore, clock: Clock): ScheduledParcelSummaryService {
    return ScheduledParcelSummaryService(
        store = store,
        validator = defaultScheduledParcelSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
