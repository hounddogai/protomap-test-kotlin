package com.acme.transport.network.hub.mapping

import com.acme.transport.network.clock.Clock

/** Moves hub ledgers through their life cycle and keeps a journal of what it did. */
data class HubLedgerService(
    var store: HubLedgerStore,
    var validator: HubLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("hub ledger $code not found")
        validator.requireValid(item)
        item.moveTo(HubLedgerStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("hub ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubLedgerStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newHubLedgerService(store: HubLedgerStore, clock: Clock): HubLedgerService {
    return HubLedgerService(
        store = store,
        validator = defaultHubLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
