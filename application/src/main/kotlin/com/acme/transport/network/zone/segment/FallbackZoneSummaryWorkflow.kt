package com.acme.transport.network.zone.segment

import com.acme.transport.network.clock.Clock

/** Moves fallback zone summaries through their life cycle and keeps a journal of what it did. */
data class FallbackZoneSummaryDesk(
    var store: FallbackZoneSummaryStore,
    var validator: FallbackZoneSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback zone summary $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackZoneSummaryStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback zone summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackZoneSummaryStatus.WITHDRAWN)
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

fun newFallbackZoneSummaryDesk(store: FallbackZoneSummaryStore, clock: Clock): FallbackZoneSummaryDesk {
    return FallbackZoneSummaryDesk(
        store = store,
        validator = defaultFallbackZoneSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
