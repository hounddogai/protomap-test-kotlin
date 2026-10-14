package com.acme.transport.freightpay.freightinvoice.target

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves global freight invoice digests through their life cycle and keeps a journal of what it did.
 */
data class GlobalFreightInvoiceDigestCoordinator(
    var store: GlobalFreightInvoiceDigestStore,
    var validator: GlobalFreightInvoiceDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global freight invoice digest $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalFreightInvoiceDigestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global freight invoice digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalFreightInvoiceDigestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newGlobalFreightInvoiceDigestCoordinator(store: GlobalFreightInvoiceDigestStore, clock: Clock): GlobalFreightInvoiceDigestCoordinator {
    return GlobalFreightInvoiceDigestCoordinator(
        store = store,
        validator = defaultGlobalFreightInvoiceDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
