package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves relay ledgers through their life cycle and keeps a journal of what it did. */
data class RelayLedgerDesk(
    var store: RelayLedgerStore,
    var validator: RelayLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("relay ledger $code not found")
        validator.requireValid(item)
        item.moveTo(RelayLedgerStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("relay ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayLedgerStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("relay ledger $code not found")
        val result = engine.compute(item)
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

fun newRelayLedgerDesk(store: RelayLedgerStore, clock: Clock, flags: Registry): RelayLedgerDesk {
    return RelayLedgerDesk(
        store = store,
        validator = defaultRelayLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayLedgerEngine(flags),
    )
}
