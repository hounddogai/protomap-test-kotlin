package com.acme.transport.customs.hold.summary

import com.acme.transport.customs.clock.Clock

/** Moves hold summaries through their life cycle and keeps a journal of what it did. */
data class HoldSummaryService(
    var store: HoldSummaryStore,
    var validator: HoldSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hold summary $code not found")
        validator.requireValid(item)
        item.moveTo(HoldSummaryStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("hold summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldSummaryStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newHoldSummaryService(store: HoldSummaryStore, clock: Clock): HoldSummaryService {
    return HoldSummaryService(
        store = store,
        validator = defaultHoldSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
