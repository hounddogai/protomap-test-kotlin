package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves scheduled lease return ledgers through their life cycle and keeps a journal of what it did.
 */
data class ScheduledLeaseReturnLedgerCoordinator(
    var store: ScheduledLeaseReturnLedgerStore,
    var validator: ScheduledLeaseReturnLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledLeaseReturnLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled lease return ledger $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledLeaseReturnLedgerStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled lease return ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledLeaseReturnLedgerStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled lease return ledger $code not found")
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

fun newScheduledLeaseReturnLedgerCoordinator(store: ScheduledLeaseReturnLedgerStore, clock: Clock): ScheduledLeaseReturnLedgerCoordinator {
    return ScheduledLeaseReturnLedgerCoordinator(
        store = store,
        validator = defaultScheduledLeaseReturnLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledLeaseReturnLedgerEngine(),
    )
}
