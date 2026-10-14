package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.freightpay.clock.Clock

/** Moves freight invoice windows through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceWindowService(
    var store: FreightInvoiceWindowStore,
    var validator: FreightInvoiceWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("freight invoice window $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceWindowStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceWindowStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newFreightInvoiceWindowService(store: FreightInvoiceWindowStore, clock: Clock): FreightInvoiceWindowService {
    return FreightInvoiceWindowService(
        store = store,
        validator = defaultFreightInvoiceWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
