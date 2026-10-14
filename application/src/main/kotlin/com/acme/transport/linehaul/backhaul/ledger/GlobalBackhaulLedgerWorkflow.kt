package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves global backhaul ledgers through their life cycle and keeps a journal of what it did. */
data class GlobalBackhaulLedgerService(
    var store: GlobalBackhaulLedgerStore,
    var validator: GlobalBackhaulLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalBackhaulLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global backhaul ledger $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalBackhaulLedgerStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global backhaul ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalBackhaulLedgerStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("global backhaul ledger $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newGlobalBackhaulLedgerService(store: GlobalBackhaulLedgerStore, clock: Clock, flags: Registry): GlobalBackhaulLedgerService {
    return GlobalBackhaulLedgerService(
        store = store,
        validator = defaultGlobalBackhaulLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalBackhaulLedgerEngine(flags),
    )
}
