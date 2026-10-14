package com.acme.transport.fleetops.tire.tier

import com.acme.transport.fleetops.clock.Clock

/** Moves manual tire ledgers through their life cycle and keeps a journal of what it did. */
data class ManualTireLedgerDesk(
    var store: ManualTireLedgerStore,
    var validator: ManualTireLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualTireLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual tire ledger $code not found")
        validator.requireValid(item)
        item.moveTo(ManualTireLedgerStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual tire ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualTireLedgerStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual tire ledger $code not found")
        val result = engine.evaluate(item)
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

fun newManualTireLedgerDesk(store: ManualTireLedgerStore, clock: Clock): ManualTireLedgerDesk {
    return ManualTireLedgerDesk(
        store = store,
        validator = defaultManualTireLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualTireLedgerEngine(),
    )
}
