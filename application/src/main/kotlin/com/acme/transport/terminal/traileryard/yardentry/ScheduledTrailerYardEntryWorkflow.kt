package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/**
 * Moves scheduled trailer yard entries through their life cycle and keeps a journal of what it did.
 */
data class ScheduledTrailerYardEntryService(
    var store: ScheduledTrailerYardEntryStore,
    var validator: ScheduledTrailerYardEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledTrailerYardEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled trailer yard entry $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledTrailerYardEntryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled trailer yard entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledTrailerYardEntryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled trailer yard entry $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newScheduledTrailerYardEntryService(store: ScheduledTrailerYardEntryStore, clock: Clock, flags: Registry): ScheduledTrailerYardEntryService {
    return ScheduledTrailerYardEntryService(
        store = store,
        validator = defaultScheduledTrailerYardEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledTrailerYardEntryEngine(flags),
    )
}
