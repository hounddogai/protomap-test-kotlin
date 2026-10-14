package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.customs.clock.Clock

/** Moves clearance ledgers through their life cycle and keeps a journal of what it did. */
data class ClearanceLedgerManager(
    var store: ClearanceLedgerStore,
    var validator: ClearanceLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("clearance ledger $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceLedgerStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceLedgerStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newClearanceLedgerManager(store: ClearanceLedgerStore, clock: Clock): ClearanceLedgerManager {
    return ClearanceLedgerManager(
        store = store,
        validator = defaultClearanceLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
