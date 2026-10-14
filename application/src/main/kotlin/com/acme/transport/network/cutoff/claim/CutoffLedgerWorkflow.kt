package com.acme.transport.network.cutoff.claim

import com.acme.transport.network.clock.Clock

/** Moves cutoff ledgers through their life cycle and keeps a journal of what it did. */
data class CutoffLedgerManager(
    var store: CutoffLedgerStore,
    var validator: CutoffLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("cutoff ledger $code not found")
        validator.requireValid(item)
        item.moveTo(CutoffLedgerStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("cutoff ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CutoffLedgerStatus.FAILED)
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

fun newCutoffLedgerManager(store: CutoffLedgerStore, clock: Clock): CutoffLedgerManager {
    return CutoffLedgerManager(
        store = store,
        validator = defaultCutoffLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
