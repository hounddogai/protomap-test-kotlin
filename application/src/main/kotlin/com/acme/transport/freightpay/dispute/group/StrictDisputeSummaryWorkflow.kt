package com.acme.transport.freightpay.dispute.group

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves strict dispute summaries through their life cycle and keeps a journal of what it did. */
data class StrictDisputeSummaryManager(
    var store: StrictDisputeSummaryStore,
    var validator: StrictDisputeSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictDisputeSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict dispute summary $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDisputeSummaryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict dispute summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDisputeSummaryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict dispute summary $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newStrictDisputeSummaryManager(store: StrictDisputeSummaryStore, clock: Clock, flags: Registry): StrictDisputeSummaryManager {
    return StrictDisputeSummaryManager(
        store = store,
        validator = defaultStrictDisputeSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictDisputeSummaryEngine(flags),
    )
}
