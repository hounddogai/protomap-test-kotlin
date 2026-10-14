package com.acme.transport.crewing.roster.forecast

import com.acme.transport.crewing.clock.Clock

/** Moves roster ledgers through their life cycle and keeps a journal of what it did. */
data class RosterLedgerManager(
    var store: RosterLedgerStore,
    var validator: RosterLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("roster ledger $code not found")
        validator.requireValid(item)
        item.moveTo(RosterLedgerStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("roster ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RosterLedgerStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newRosterLedgerManager(store: RosterLedgerStore, clock: Clock): RosterLedgerManager {
    return RosterLedgerManager(
        store = store,
        validator = defaultRosterLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
