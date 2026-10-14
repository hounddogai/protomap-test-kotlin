package com.acme.transport.linehaul.trailer.overrideitem

import com.acme.transport.linehaul.clock.Clock

/** Moves scheduled trailer overrides through their life cycle and keeps a journal of what it did. */
data class ScheduledTrailerOverrideCoordinator(
    var store: ScheduledTrailerOverrideStore,
    var validator: ScheduledTrailerOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled trailer override $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledTrailerOverrideStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled trailer override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledTrailerOverrideStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newScheduledTrailerOverrideCoordinator(store: ScheduledTrailerOverrideStore, clock: Clock): ScheduledTrailerOverrideCoordinator {
    return ScheduledTrailerOverrideCoordinator(
        store = store,
        validator = defaultScheduledTrailerOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
