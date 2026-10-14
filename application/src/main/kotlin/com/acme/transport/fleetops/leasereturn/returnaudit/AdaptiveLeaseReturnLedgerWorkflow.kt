package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.fleetops.clock.Clock

/** Moves adaptive lease return ledgers through their life cycle and keeps a journal of what it did. */
data class AdaptiveLeaseReturnLedgerManager(
    var store: AdaptiveLeaseReturnLedgerStore,
    var validator: AdaptiveLeaseReturnLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive lease return ledger $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveLeaseReturnLedgerStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive lease return ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveLeaseReturnLedgerStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newAdaptiveLeaseReturnLedgerManager(store: AdaptiveLeaseReturnLedgerStore, clock: Clock): AdaptiveLeaseReturnLedgerManager {
    return AdaptiveLeaseReturnLedgerManager(
        store = store,
        validator = defaultAdaptiveLeaseReturnLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
