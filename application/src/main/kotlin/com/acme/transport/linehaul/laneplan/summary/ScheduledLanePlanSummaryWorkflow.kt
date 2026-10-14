package com.acme.transport.linehaul.laneplan.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves scheduled lane plan summaries through their life cycle and keeps a journal of what it did. */
data class ScheduledLanePlanSummaryWorkflow(
    var store: ScheduledLanePlanSummaryStore,
    var validator: ScheduledLanePlanSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledLanePlanSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled lane plan summary $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledLanePlanSummaryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled lane plan summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledLanePlanSummaryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled lane plan summary $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newScheduledLanePlanSummaryWorkflow(store: ScheduledLanePlanSummaryStore, clock: Clock, flags: Registry): ScheduledLanePlanSummaryWorkflow {
    return ScheduledLanePlanSummaryWorkflow(
        store = store,
        validator = defaultScheduledLanePlanSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledLanePlanSummaryEngine(flags),
    )
}
