package com.acme.transport.freightpay.freightinvoice.manifest

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves freight invoice checkpoints through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceCheckpointCoordinator(
    var store: FreightInvoiceCheckpointStore,
    var validator: FreightInvoiceCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FreightInvoiceCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("freight invoice checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceCheckpointStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceCheckpointStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("freight invoice checkpoint $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newFreightInvoiceCheckpointCoordinator(store: FreightInvoiceCheckpointStore, clock: Clock, flags: Registry): FreightInvoiceCheckpointCoordinator {
    return FreightInvoiceCheckpointCoordinator(
        store = store,
        validator = defaultFreightInvoiceCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFreightInvoiceCheckpointEngine(flags),
    )
}
