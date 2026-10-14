package com.acme.transport.network.hub.draft

import com.acme.transport.network.clock.Clock

/** Moves global hub ledgers through their life cycle and keeps a journal of what it did. */
data class GlobalHubLedgerService(
    var store: GlobalHubLedgerStore,
    var validator: GlobalHubLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalHubLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global hub ledger $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalHubLedgerStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global hub ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalHubLedgerStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global hub ledger $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newGlobalHubLedgerService(store: GlobalHubLedgerStore, clock: Clock): GlobalHubLedgerService {
    return GlobalHubLedgerService(
        store = store,
        validator = defaultGlobalHubLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalHubLedgerEngine(),
    )
}
