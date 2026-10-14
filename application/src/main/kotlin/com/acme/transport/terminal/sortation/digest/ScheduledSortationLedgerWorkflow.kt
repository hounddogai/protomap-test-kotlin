package com.acme.transport.terminal.sortation.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves scheduled sortation ledgers through their life cycle and keeps a journal of what it did. */
data class ScheduledSortationLedgerService(
    var store: ScheduledSortationLedgerStore,
    var validator: ScheduledSortationLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledSortationLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled sortation ledger $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledSortationLedgerStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled sortation ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledSortationLedgerStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scheduled sortation ledger $code not found")
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

fun newScheduledSortationLedgerService(store: ScheduledSortationLedgerStore, clock: Clock, flags: Registry): ScheduledSortationLedgerService {
    return ScheduledSortationLedgerService(
        store = store,
        validator = defaultScheduledSortationLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledSortationLedgerEngine(flags),
    )
}
