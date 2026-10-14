package com.acme.transport.terminal.crossdock.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves cross dock ledgers through their life cycle and keeps a journal of what it did. */
data class CrossDockLedgerService(
    var store: CrossDockLedgerStore,
    var validator: CrossDockLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CrossDockLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("cross dock ledger $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockLedgerStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockLedgerStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("cross dock ledger $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newCrossDockLedgerService(store: CrossDockLedgerStore, clock: Clock, flags: Registry): CrossDockLedgerService {
    return CrossDockLedgerService(
        store = store,
        validator = defaultCrossDockLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCrossDockLedgerEngine(flags),
    )
}
