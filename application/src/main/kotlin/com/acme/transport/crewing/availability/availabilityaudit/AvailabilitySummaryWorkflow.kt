package com.acme.transport.crewing.availability.availabilityaudit

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves availability summaries through their life cycle and keeps a journal of what it did. */
data class AvailabilitySummaryManager(
    var store: AvailabilitySummaryStore,
    var validator: AvailabilitySummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AvailabilitySummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("availability summary $code not found")
        validator.requireValid(item)
        item.moveTo(AvailabilitySummaryStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("availability summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AvailabilitySummaryStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("availability summary $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newAvailabilitySummaryManager(store: AvailabilitySummaryStore, clock: Clock, flags: Registry): AvailabilitySummaryManager {
    return AvailabilitySummaryManager(
        store = store,
        validator = defaultAvailabilitySummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAvailabilitySummaryEngine(flags),
    )
}
