package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves capacity ledgers through their life cycle and keeps a journal of what it did. */
data class CapacityLedgerService(
    var store: CapacityLedgerStore,
    var validator: CapacityLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CapacityLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capacity ledger $code not found")
        validator.requireValid(item)
        item.moveTo(CapacityLedgerStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacityLedgerStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capacity ledger $code not found")
        val result = engine.price(item)
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

fun newCapacityLedgerService(store: CapacityLedgerStore, clock: Clock, flags: Registry): CapacityLedgerService {
    return CapacityLedgerService(
        store = store,
        validator = defaultCapacityLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCapacityLedgerEngine(flags),
    )
}
