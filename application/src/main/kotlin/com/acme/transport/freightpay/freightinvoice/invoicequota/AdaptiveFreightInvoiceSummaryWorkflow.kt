package com.acme.transport.freightpay.freightinvoice.invoicequota

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves adaptive freight invoice summaries through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveFreightInvoiceSummaryDesk(
    var store: AdaptiveFreightInvoiceSummaryStore,
    var validator: AdaptiveFreightInvoiceSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveFreightInvoiceSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive freight invoice summary $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveFreightInvoiceSummaryStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive freight invoice summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveFreightInvoiceSummaryStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("adaptive freight invoice summary $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newAdaptiveFreightInvoiceSummaryDesk(store: AdaptiveFreightInvoiceSummaryStore, clock: Clock): AdaptiveFreightInvoiceSummaryDesk {
    return AdaptiveFreightInvoiceSummaryDesk(
        store = store,
        validator = defaultAdaptiveFreightInvoiceSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveFreightInvoiceSummaryEngine(),
    )
}
