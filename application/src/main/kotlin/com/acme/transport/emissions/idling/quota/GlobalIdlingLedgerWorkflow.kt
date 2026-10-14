package com.acme.transport.emissions.idling.quota

import com.acme.transport.emissions.clock.Clock

/** Moves global idling ledgers through their life cycle and keeps a journal of what it did. */
data class GlobalIdlingLedgerDesk(
    var store: GlobalIdlingLedgerStore,
    var validator: GlobalIdlingLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global idling ledger $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalIdlingLedgerStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global idling ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalIdlingLedgerStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newGlobalIdlingLedgerDesk(store: GlobalIdlingLedgerStore, clock: Clock): GlobalIdlingLedgerDesk {
    return GlobalIdlingLedgerDesk(
        store = store,
        validator = defaultGlobalIdlingLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
