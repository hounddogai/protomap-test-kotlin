package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves strict freight invoice requests through their life cycle and keeps a journal of what it did.
 */
data class StrictFreightInvoiceRequestDesk(
    var store: StrictFreightInvoiceRequestStore,
    var validator: StrictFreightInvoiceRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict freight invoice request $code not found")
        validator.requireValid(item)
        item.moveTo(StrictFreightInvoiceRequestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict freight invoice request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictFreightInvoiceRequestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newStrictFreightInvoiceRequestDesk(store: StrictFreightInvoiceRequestStore, clock: Clock): StrictFreightInvoiceRequestDesk {
    return StrictFreightInvoiceRequestDesk(
        store = store,
        validator = defaultStrictFreightInvoiceRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
