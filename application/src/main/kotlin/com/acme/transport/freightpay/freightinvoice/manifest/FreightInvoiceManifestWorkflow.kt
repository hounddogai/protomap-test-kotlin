package com.acme.transport.freightpay.freightinvoice.manifest

import com.acme.transport.freightpay.clock.Clock

/** Moves freight invoice manifests through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceManifestCoordinator(
    var store: FreightInvoiceManifestStore,
    var validator: FreightInvoiceManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("freight invoice manifest $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceManifestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceManifestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newFreightInvoiceManifestCoordinator(store: FreightInvoiceManifestStore, clock: Clock): FreightInvoiceManifestCoordinator {
    return FreightInvoiceManifestCoordinator(
        store = store,
        validator = defaultFreightInvoiceManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
