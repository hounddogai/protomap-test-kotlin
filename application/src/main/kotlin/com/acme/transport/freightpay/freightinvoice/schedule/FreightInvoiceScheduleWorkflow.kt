package com.acme.transport.freightpay.freightinvoice.schedule

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves freight invoice schedules through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceScheduleWorkflow(
    var store: FreightInvoiceScheduleStore,
    var validator: FreightInvoiceScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FreightInvoiceScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("freight invoice schedule $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceScheduleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceScheduleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("freight invoice schedule $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newFreightInvoiceScheduleWorkflow(store: FreightInvoiceScheduleStore, clock: Clock, flags: Registry): FreightInvoiceScheduleWorkflow {
    return FreightInvoiceScheduleWorkflow(
        store = store,
        validator = defaultFreightInvoiceScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFreightInvoiceScheduleEngine(flags),
    )
}
