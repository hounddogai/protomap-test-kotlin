package com.acme.transport.freightpay.freightinvoice.schedule

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves fallback freight invoice checkpoints through their life cycle and keeps a journal of what it did.
 */
data class FallbackFreightInvoiceCheckpointCoordinator(
    var store: FallbackFreightInvoiceCheckpointStore,
    var validator: FallbackFreightInvoiceCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackFreightInvoiceCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback freight invoice checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackFreightInvoiceCheckpointStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback freight invoice checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackFreightInvoiceCheckpointStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fallback freight invoice checkpoint $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newFallbackFreightInvoiceCheckpointCoordinator(store: FallbackFreightInvoiceCheckpointStore, clock: Clock): FallbackFreightInvoiceCheckpointCoordinator {
    return FallbackFreightInvoiceCheckpointCoordinator(
        store = store,
        validator = defaultFallbackFreightInvoiceCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackFreightInvoiceCheckpointEngine(),
    )
}
