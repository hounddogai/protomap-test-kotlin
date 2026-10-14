package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves regional freight invoice summaries through their life cycle and keeps a journal of what it did.
 */
data class RegionalFreightInvoiceSummaryWorkflow(
    var store: RegionalFreightInvoiceSummaryStore,
    var validator: RegionalFreightInvoiceSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalFreightInvoiceSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional freight invoice summary $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalFreightInvoiceSummaryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional freight invoice summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalFreightInvoiceSummaryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional freight invoice summary $code not found")
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

fun newRegionalFreightInvoiceSummaryWorkflow(store: RegionalFreightInvoiceSummaryStore, clock: Clock, flags: Registry): RegionalFreightInvoiceSummaryWorkflow {
    return RegionalFreightInvoiceSummaryWorkflow(
        store = store,
        validator = defaultRegionalFreightInvoiceSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalFreightInvoiceSummaryEngine(flags),
    )
}
