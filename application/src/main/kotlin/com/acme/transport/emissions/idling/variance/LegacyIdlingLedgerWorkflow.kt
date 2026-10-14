package com.acme.transport.emissions.idling.variance

import com.acme.transport.emissions.clock.Clock

/** Moves legacy idling ledgers through their life cycle and keeps a journal of what it did. */
data class LegacyIdlingLedgerWorkflow(
    var store: LegacyIdlingLedgerStore,
    var validator: LegacyIdlingLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy idling ledger $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyIdlingLedgerStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy idling ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyIdlingLedgerStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLegacyIdlingLedgerWorkflow(store: LegacyIdlingLedgerStore, clock: Clock): LegacyIdlingLedgerWorkflow {
    return LegacyIdlingLedgerWorkflow(
        store = store,
        validator = defaultLegacyIdlingLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
