package com.acme.transport.freightpay.freightinvoice.score

import com.acme.transport.freightpay.clock.Clock

/** Moves freight invoice milestones through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceMilestoneManager(
    var store: FreightInvoiceMilestoneStore,
    var validator: FreightInvoiceMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("freight invoice milestone $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceMilestoneStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceMilestoneStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newFreightInvoiceMilestoneManager(store: FreightInvoiceMilestoneStore, clock: Clock): FreightInvoiceMilestoneManager {
    return FreightInvoiceMilestoneManager(
        store = store,
        validator = defaultFreightInvoiceMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
