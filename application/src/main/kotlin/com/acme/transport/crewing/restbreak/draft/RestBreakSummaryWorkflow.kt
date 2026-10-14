package com.acme.transport.crewing.restbreak.draft

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rest break summaries through their life cycle and keeps a journal of what it did. */
data class RestBreakSummaryWorkflow(
    var store: RestBreakSummaryStore,
    var validator: RestBreakSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RestBreakSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rest break summary $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakSummaryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakSummaryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rest break summary $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newRestBreakSummaryWorkflow(store: RestBreakSummaryStore, clock: Clock, flags: Registry): RestBreakSummaryWorkflow {
    return RestBreakSummaryWorkflow(
        store = store,
        validator = defaultRestBreakSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRestBreakSummaryEngine(flags),
    )
}
