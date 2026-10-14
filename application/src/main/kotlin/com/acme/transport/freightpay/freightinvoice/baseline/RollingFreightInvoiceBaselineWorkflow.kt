package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves rolling freight invoice baselines through their life cycle and keeps a journal of what it did.
 */
data class RollingFreightInvoiceBaselineService(
    var store: RollingFreightInvoiceBaselineStore,
    var validator: RollingFreightInvoiceBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingFreightInvoiceBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling freight invoice baseline $code not found")
        validator.requireValid(item)
        item.moveTo(RollingFreightInvoiceBaselineStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling freight invoice baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingFreightInvoiceBaselineStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling freight invoice baseline $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newRollingFreightInvoiceBaselineService(store: RollingFreightInvoiceBaselineStore, clock: Clock): RollingFreightInvoiceBaselineService {
    return RollingFreightInvoiceBaselineService(
        store = store,
        validator = defaultRollingFreightInvoiceBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingFreightInvoiceBaselineEngine(),
    )
}
