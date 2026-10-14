package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual rate card summaries through their life cycle and keeps a journal of what it did. */
data class ManualRateCardSummaryWorkflow(
    var store: ManualRateCardSummaryStore,
    var validator: ManualRateCardSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualRateCardSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual rate card summary $code not found")
        validator.requireValid(item)
        item.moveTo(ManualRateCardSummaryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual rate card summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualRateCardSummaryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual rate card summary $code not found")
        val result = engine.price(item)
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

fun newManualRateCardSummaryWorkflow(store: ManualRateCardSummaryStore, clock: Clock, flags: Registry): ManualRateCardSummaryWorkflow {
    return ManualRateCardSummaryWorkflow(
        store = store,
        validator = defaultManualRateCardSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualRateCardSummaryEngine(flags),
    )
}
