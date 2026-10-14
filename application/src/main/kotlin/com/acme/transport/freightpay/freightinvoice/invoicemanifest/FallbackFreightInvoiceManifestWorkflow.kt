package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves fallback freight invoice manifests through their life cycle and keeps a journal of what it did.
 */
data class FallbackFreightInvoiceManifestDesk(
    var store: FallbackFreightInvoiceManifestStore,
    var validator: FallbackFreightInvoiceManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback freight invoice manifest $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackFreightInvoiceManifestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback freight invoice manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackFreightInvoiceManifestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newFallbackFreightInvoiceManifestDesk(store: FallbackFreightInvoiceManifestStore, clock: Clock): FallbackFreightInvoiceManifestDesk {
    return FallbackFreightInvoiceManifestDesk(
        store = store,
        validator = defaultFallbackFreightInvoiceManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
