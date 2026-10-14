package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.linehaul.clock.Clock

/** Moves scheduled trailer exceptions through their life cycle and keeps a journal of what it did. */
data class ScheduledTrailerExceptionManager(
    var store: ScheduledTrailerExceptionStore,
    var validator: ScheduledTrailerExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled trailer exception $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledTrailerExceptionStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled trailer exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledTrailerExceptionStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newScheduledTrailerExceptionManager(store: ScheduledTrailerExceptionStore, clock: Clock): ScheduledTrailerExceptionManager {
    return ScheduledTrailerExceptionManager(
        store = store,
        validator = defaultScheduledTrailerExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
