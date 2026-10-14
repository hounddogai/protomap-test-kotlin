package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves redelivery summaries through their life cycle and keeps a journal of what it did. */
data class RedeliverySummaryDesk(
    var store: RedeliverySummaryStore,
    var validator: RedeliverySummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RedeliverySummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("redelivery summary $code not found")
        validator.requireValid(item)
        item.moveTo(RedeliverySummaryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("redelivery summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RedeliverySummaryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("redelivery summary $code not found")
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

fun newRedeliverySummaryDesk(store: RedeliverySummaryStore, clock: Clock, flags: Registry): RedeliverySummaryDesk {
    return RedeliverySummaryDesk(
        store = store,
        validator = defaultRedeliverySummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRedeliverySummaryEngine(flags),
    )
}
