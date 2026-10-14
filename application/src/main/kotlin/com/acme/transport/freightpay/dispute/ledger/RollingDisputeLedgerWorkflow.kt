package com.acme.transport.freightpay.dispute.ledger

import com.acme.transport.freightpay.clock.Clock

/** Moves rolling dispute ledgers through their life cycle and keeps a journal of what it did. */
data class RollingDisputeLedgerDesk(
    var store: RollingDisputeLedgerStore,
    var validator: RollingDisputeLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling dispute ledger $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDisputeLedgerStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling dispute ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDisputeLedgerStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newRollingDisputeLedgerDesk(store: RollingDisputeLedgerStore, clock: Clock): RollingDisputeLedgerDesk {
    return RollingDisputeLedgerDesk(
        store = store,
        validator = defaultRollingDisputeLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
