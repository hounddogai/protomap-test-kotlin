package com.acme.transport.network.transittime.allocation

import com.acme.transport.network.clock.Clock

/** Moves default transit time groups through their life cycle and keeps a journal of what it did. */
data class DefaultTransitTimeGroupDesk(
    var store: DefaultTransitTimeGroupStore,
    var validator: DefaultTransitTimeGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default transit time group $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultTransitTimeGroupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default transit time group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultTransitTimeGroupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newDefaultTransitTimeGroupDesk(store: DefaultTransitTimeGroupStore, clock: Clock): DefaultTransitTimeGroupDesk {
    return DefaultTransitTimeGroupDesk(
        store = store,
        validator = defaultDefaultTransitTimeGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
