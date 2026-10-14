package com.acme.transport.freightpay.freightinvoice.adjustment

import com.acme.transport.freightpay.clock.Clock

/** Moves freight invoice adjustments through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceAdjustmentService(
    var store: FreightInvoiceAdjustmentStore,
    var validator: FreightInvoiceAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FreightInvoiceAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("freight invoice adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceAdjustmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceAdjustmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("freight invoice adjustment $code not found")
        val result = engine.evaluate(item)
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

fun newFreightInvoiceAdjustmentService(store: FreightInvoiceAdjustmentStore, clock: Clock): FreightInvoiceAdjustmentService {
    return FreightInvoiceAdjustmentService(
        store = store,
        validator = defaultFreightInvoiceAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFreightInvoiceAdjustmentEngine(),
    )
}
