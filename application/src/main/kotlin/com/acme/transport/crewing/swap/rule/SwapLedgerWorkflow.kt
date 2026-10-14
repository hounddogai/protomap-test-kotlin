package com.acme.transport.crewing.swap.rule

import com.acme.transport.crewing.clock.Clock

/** Moves swap ledgers through their life cycle and keeps a journal of what it did. */
data class SwapLedgerCoordinator(
    var store: SwapLedgerStore,
    var validator: SwapLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("swap ledger $code not found")
        validator.requireValid(item)
        item.moveTo(SwapLedgerStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("swap ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SwapLedgerStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newSwapLedgerCoordinator(store: SwapLedgerStore, clock: Clock): SwapLedgerCoordinator {
    return SwapLedgerCoordinator(
        store = store,
        validator = defaultSwapLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
