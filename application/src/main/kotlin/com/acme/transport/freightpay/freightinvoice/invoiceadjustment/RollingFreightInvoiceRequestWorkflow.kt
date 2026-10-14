package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves rolling freight invoice requests through their life cycle and keeps a journal of what it did.
 */
data class RollingFreightInvoiceRequestWorkflow(
    var store: RollingFreightInvoiceRequestStore,
    var validator: RollingFreightInvoiceRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingFreightInvoiceRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling freight invoice request $code not found")
        validator.requireValid(item)
        item.moveTo(RollingFreightInvoiceRequestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling freight invoice request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingFreightInvoiceRequestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling freight invoice request $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newRollingFreightInvoiceRequestWorkflow(store: RollingFreightInvoiceRequestStore, clock: Clock): RollingFreightInvoiceRequestWorkflow {
    return RollingFreightInvoiceRequestWorkflow(
        store = store,
        validator = defaultRollingFreightInvoiceRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingFreightInvoiceRequestEngine(),
    )
}
