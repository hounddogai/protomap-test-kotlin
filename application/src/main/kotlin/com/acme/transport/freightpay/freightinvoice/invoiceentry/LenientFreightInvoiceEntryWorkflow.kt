package com.acme.transport.freightpay.freightinvoice.invoiceentry

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves lenient freight invoice entries through their life cycle and keeps a journal of what it did.
 */
data class LenientFreightInvoiceEntryService(
    var store: LenientFreightInvoiceEntryStore,
    var validator: LenientFreightInvoiceEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientFreightInvoiceEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient freight invoice entry $code not found")
        validator.requireValid(item)
        item.moveTo(LenientFreightInvoiceEntryStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient freight invoice entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientFreightInvoiceEntryStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient freight invoice entry $code not found")
        val result = engine.compute(item)
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

fun newLenientFreightInvoiceEntryService(store: LenientFreightInvoiceEntryStore, clock: Clock): LenientFreightInvoiceEntryService {
    return LenientFreightInvoiceEntryService(
        store = store,
        validator = defaultLenientFreightInvoiceEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientFreightInvoiceEntryEngine(),
    )
}
