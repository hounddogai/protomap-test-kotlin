package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves legacy freight invoice digests through their life cycle and keeps a journal of what it did.
 */
data class LegacyFreightInvoiceDigestDesk(
    var store: LegacyFreightInvoiceDigestStore,
    var validator: LegacyFreightInvoiceDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy freight invoice digest $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyFreightInvoiceDigestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy freight invoice digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyFreightInvoiceDigestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newLegacyFreightInvoiceDigestDesk(store: LegacyFreightInvoiceDigestStore, clock: Clock): LegacyFreightInvoiceDigestDesk {
    return LegacyFreightInvoiceDigestDesk(
        store = store,
        validator = defaultLegacyFreightInvoiceDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
