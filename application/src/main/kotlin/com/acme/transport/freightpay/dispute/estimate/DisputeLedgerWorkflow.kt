package com.acme.transport.freightpay.dispute.estimate

import com.acme.transport.freightpay.clock.Clock

/** Moves dispute ledgers through their life cycle and keeps a journal of what it did. */
data class DisputeLedgerService(
    var store: DisputeLedgerStore,
    var validator: DisputeLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dispute ledger $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeLedgerStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeLedgerStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newDisputeLedgerService(store: DisputeLedgerStore, clock: Clock): DisputeLedgerService {
    return DisputeLedgerService(
        store = store,
        validator = defaultDisputeLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
