package com.acme.transport.freightpay.freightinvoice.budget

import com.acme.transport.freightpay.clock.Clock

/** Moves freight invoice forecasts through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceForecastService(
    var store: FreightInvoiceForecastStore,
    var validator: FreightInvoiceForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("freight invoice forecast $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceForecastStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceForecastStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newFreightInvoiceForecastService(store: FreightInvoiceForecastStore, clock: Clock): FreightInvoiceForecastService {
    return FreightInvoiceForecastService(
        store = store,
        validator = defaultFreightInvoiceForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
