package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.terminal.clock.Clock

/** Moves dock door ledgers through their life cycle and keeps a journal of what it did. */
data class DockDoorLedgerCoordinator(
    var store: DockDoorLedgerStore,
    var validator: DockDoorLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DockDoorLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("dock door ledger $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorLedgerStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorLedgerStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("dock door ledger $code not found")
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

fun newDockDoorLedgerCoordinator(store: DockDoorLedgerStore, clock: Clock): DockDoorLedgerCoordinator {
    return DockDoorLedgerCoordinator(
        store = store,
        validator = defaultDockDoorLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDockDoorLedgerEngine(),
    )
}
