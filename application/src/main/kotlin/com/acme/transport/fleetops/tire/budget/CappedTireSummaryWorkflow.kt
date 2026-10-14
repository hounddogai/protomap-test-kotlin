package com.acme.transport.fleetops.tire.budget

import com.acme.transport.fleetops.clock.Clock

/** Moves capped tire summaries through their life cycle and keeps a journal of what it did. */
data class CappedTireSummaryManager(
    var store: CappedTireSummaryStore,
    var validator: CappedTireSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped tire summary $code not found")
        validator.requireValid(item)
        item.moveTo(CappedTireSummaryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped tire summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedTireSummaryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newCappedTireSummaryManager(store: CappedTireSummaryStore, clock: Clock): CappedTireSummaryManager {
    return CappedTireSummaryManager(
        store = store,
        validator = defaultCappedTireSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
