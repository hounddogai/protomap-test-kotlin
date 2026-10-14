package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul ledgers through their life cycle and keeps a journal of what it did. */
data class BackhaulLedgerCoordinator(
    var store: BackhaulLedgerStore,
    var validator: BackhaulLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("backhaul ledger $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulLedgerStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulLedgerStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newBackhaulLedgerCoordinator(store: BackhaulLedgerStore, clock: Clock): BackhaulLedgerCoordinator {
    return BackhaulLedgerCoordinator(
        store = store,
        validator = defaultBackhaulLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
