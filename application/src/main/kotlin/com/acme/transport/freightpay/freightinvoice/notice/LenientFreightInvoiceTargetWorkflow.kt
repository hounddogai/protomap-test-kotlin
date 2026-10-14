package com.acme.transport.freightpay.freightinvoice.notice

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves lenient freight invoice targets through their life cycle and keeps a journal of what it did.
 */
data class LenientFreightInvoiceTargetManager(
    var store: LenientFreightInvoiceTargetStore,
    var validator: LenientFreightInvoiceTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientFreightInvoiceTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient freight invoice target $code not found")
        validator.requireValid(item)
        item.moveTo(LenientFreightInvoiceTargetStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient freight invoice target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientFreightInvoiceTargetStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lenient freight invoice target $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLenientFreightInvoiceTargetManager(store: LenientFreightInvoiceTargetStore, clock: Clock, flags: Registry): LenientFreightInvoiceTargetManager {
    return LenientFreightInvoiceTargetManager(
        store = store,
        validator = defaultLenientFreightInvoiceTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientFreightInvoiceTargetEngine(flags),
    )
}
