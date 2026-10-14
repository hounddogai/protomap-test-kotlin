package com.acme.transport.freightpay.freightinvoice.summary

import com.acme.transport.freightpay.clock.Clock

/** Moves freight invoice summaries through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceSummaryWorkflow(
    var store: FreightInvoiceSummaryStore,
    var validator: FreightInvoiceSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("freight invoice summary $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceSummaryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceSummaryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newFreightInvoiceSummaryWorkflow(store: FreightInvoiceSummaryStore, clock: Clock): FreightInvoiceSummaryWorkflow {
    return FreightInvoiceSummaryWorkflow(
        store = store,
        validator = defaultFreightInvoiceSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
