package com.acme.transport.network.hub.ledger

import com.acme.transport.network.clock.Clock

/** Moves pending hub ledgers through their life cycle and keeps a journal of what it did. */
data class PendingHubLedgerWorkflow(
    var store: PendingHubLedgerStore,
    var validator: PendingHubLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending hub ledger $code not found")
        validator.requireValid(item)
        item.moveTo(PendingHubLedgerStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending hub ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingHubLedgerStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newPendingHubLedgerWorkflow(store: PendingHubLedgerStore, clock: Clock): PendingHubLedgerWorkflow {
    return PendingHubLedgerWorkflow(
        store = store,
        validator = defaultPendingHubLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
