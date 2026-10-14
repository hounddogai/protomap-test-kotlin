package com.acme.transport.crewing.roster.rostercycle

import com.acme.transport.crewing.clock.Clock

/** Moves scheduled roster overrides through their life cycle and keeps a journal of what it did. */
data class ScheduledRosterOverrideCoordinator(
    var store: ScheduledRosterOverrideStore,
    var validator: ScheduledRosterOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled roster override $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRosterOverrideStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled roster override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRosterOverrideStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newScheduledRosterOverrideCoordinator(store: ScheduledRosterOverrideStore, clock: Clock): ScheduledRosterOverrideCoordinator {
    return ScheduledRosterOverrideCoordinator(
        store = store,
        validator = defaultScheduledRosterOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
