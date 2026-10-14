package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.freightpay.clock.Clock

/** Moves freight invoice bundles through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceBundleCoordinator(
    var store: FreightInvoiceBundleStore,
    var validator: FreightInvoiceBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("freight invoice bundle $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceBundleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceBundleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newFreightInvoiceBundleCoordinator(store: FreightInvoiceBundleStore, clock: Clock): FreightInvoiceBundleCoordinator {
    return FreightInvoiceBundleCoordinator(
        store = store,
        validator = defaultFreightInvoiceBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
