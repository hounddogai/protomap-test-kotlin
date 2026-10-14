package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves tiered freight invoice checkpoints through their life cycle and keeps a journal of what it did.
 */
data class TieredFreightInvoiceCheckpointDesk(
    var store: TieredFreightInvoiceCheckpointStore,
    var validator: TieredFreightInvoiceCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tiered freight invoice checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(TieredFreightInvoiceCheckpointStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered freight invoice checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredFreightInvoiceCheckpointStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newTieredFreightInvoiceCheckpointDesk(store: TieredFreightInvoiceCheckpointStore, clock: Clock): TieredFreightInvoiceCheckpointDesk {
    return TieredFreightInvoiceCheckpointDesk(
        store = store,
        validator = defaultTieredFreightInvoiceCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
