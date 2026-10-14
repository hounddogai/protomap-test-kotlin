package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.freightpay.clock.Clock

/** Moves freight invoice requests through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceRequestCoordinator(
    var store: FreightInvoiceRequestStore,
    var validator: FreightInvoiceRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FreightInvoiceRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("freight invoice request $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceRequestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceRequestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("freight invoice request $code not found")
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

fun newFreightInvoiceRequestCoordinator(store: FreightInvoiceRequestStore, clock: Clock): FreightInvoiceRequestCoordinator {
    return FreightInvoiceRequestCoordinator(
        store = store,
        validator = defaultFreightInvoiceRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFreightInvoiceRequestEngine(),
    )
}
