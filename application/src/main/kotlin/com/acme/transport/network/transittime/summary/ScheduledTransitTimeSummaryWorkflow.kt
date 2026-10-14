package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/**
 * Moves scheduled transit time summaries through their life cycle and keeps a journal of what it did.
 */
data class ScheduledTransitTimeSummaryWorkflow(
    var store: ScheduledTransitTimeSummaryStore,
    var validator: ScheduledTransitTimeSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledTransitTimeSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled transit time summary $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledTransitTimeSummaryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled transit time summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledTransitTimeSummaryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled transit time summary $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newScheduledTransitTimeSummaryWorkflow(store: ScheduledTransitTimeSummaryStore, clock: Clock, flags: Registry): ScheduledTransitTimeSummaryWorkflow {
    return ScheduledTransitTimeSummaryWorkflow(
        store = store,
        validator = defaultScheduledTransitTimeSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledTransitTimeSummaryEngine(flags),
    )
}
