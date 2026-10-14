package com.acme.transport.crewing.swap.entry

import com.acme.transport.crewing.clock.Clock

/** Moves strict swap ledgers through their life cycle and keeps a journal of what it did. */
data class StrictSwapLedgerManager(
    var store: StrictSwapLedgerStore,
    var validator: StrictSwapLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictSwapLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict swap ledger $code not found")
        validator.requireValid(item)
        item.moveTo(StrictSwapLedgerStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict swap ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictSwapLedgerStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("strict swap ledger $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newStrictSwapLedgerManager(store: StrictSwapLedgerStore, clock: Clock): StrictSwapLedgerManager {
    return StrictSwapLedgerManager(
        store = store,
        validator = defaultStrictSwapLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictSwapLedgerEngine(),
    )
}
