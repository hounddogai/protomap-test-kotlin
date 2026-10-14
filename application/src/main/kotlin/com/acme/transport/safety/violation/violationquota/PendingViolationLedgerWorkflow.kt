package com.acme.transport.safety.violation.violationquota

import com.acme.transport.safety.clock.Clock

/** Moves pending violation ledgers through their life cycle and keeps a journal of what it did. */
data class PendingViolationLedgerService(
    var store: PendingViolationLedgerStore,
    var validator: PendingViolationLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending violation ledger $code not found")
        validator.requireValid(item)
        item.moveTo(PendingViolationLedgerStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending violation ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingViolationLedgerStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newPendingViolationLedgerService(store: PendingViolationLedgerStore, clock: Clock): PendingViolationLedgerService {
    return PendingViolationLedgerService(
        store = store,
        validator = defaultPendingViolationLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
