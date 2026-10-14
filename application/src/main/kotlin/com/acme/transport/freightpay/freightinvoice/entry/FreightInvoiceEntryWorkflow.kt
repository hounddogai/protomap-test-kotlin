package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves freight invoice entries through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceEntryDesk(
    var store: FreightInvoiceEntryStore,
    var validator: FreightInvoiceEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FreightInvoiceEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("freight invoice entry $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceEntryStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceEntryStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("freight invoice entry $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newFreightInvoiceEntryDesk(store: FreightInvoiceEntryStore, clock: Clock, flags: Registry): FreightInvoiceEntryDesk {
    return FreightInvoiceEntryDesk(
        store = store,
        validator = defaultFreightInvoiceEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFreightInvoiceEntryEngine(flags),
    )
}
