package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.freightpay.clock.Clock

/** Moves freight invoice metrics through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceMetricWorkflow(
    var store: FreightInvoiceMetricStore,
    var validator: FreightInvoiceMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FreightInvoiceMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("freight invoice metric $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceMetricStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceMetricStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("freight invoice metric $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newFreightInvoiceMetricWorkflow(store: FreightInvoiceMetricStore, clock: Clock): FreightInvoiceMetricWorkflow {
    return FreightInvoiceMetricWorkflow(
        store = store,
        validator = defaultFreightInvoiceMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFreightInvoiceMetricEngine(),
    )
}
