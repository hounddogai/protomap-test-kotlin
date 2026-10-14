package com.acme.transport.network.cutoff.cutoffledger

import com.acme.transport.network.clock.Clock

/** Moves default cutoff ledgers through their life cycle and keeps a journal of what it did. */
data class DefaultCutoffLedgerService(
    var store: DefaultCutoffLedgerStore,
    var validator: DefaultCutoffLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default cutoff ledger $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultCutoffLedgerStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default cutoff ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultCutoffLedgerStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newDefaultCutoffLedgerService(store: DefaultCutoffLedgerStore, clock: Clock): DefaultCutoffLedgerService {
    return DefaultCutoffLedgerService(
        store = store,
        validator = defaultDefaultCutoffLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
