package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.freightpay.clock.Clock

/** Moves legacy freight invoice rules through their life cycle and keeps a journal of what it did. */
data class LegacyFreightInvoiceRuleManager(
    var store: LegacyFreightInvoiceRuleStore,
    var validator: LegacyFreightInvoiceRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy freight invoice rule $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyFreightInvoiceRuleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy freight invoice rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyFreightInvoiceRuleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLegacyFreightInvoiceRuleManager(store: LegacyFreightInvoiceRuleStore, clock: Clock): LegacyFreightInvoiceRuleManager {
    return LegacyFreightInvoiceRuleManager(
        store = store,
        validator = defaultLegacyFreightInvoiceRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
