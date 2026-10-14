package com.acme.transport.customs.hold.segment

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves hold ledgers through their life cycle and keeps a journal of what it did. */
data class HoldLedgerService(
    var store: HoldLedgerStore,
    var validator: HoldLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HoldLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("hold ledger $code not found")
        validator.requireValid(item)
        item.moveTo(HoldLedgerStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("hold ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldLedgerStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("hold ledger $code not found")
        val result = engine.evaluate(item)
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

fun newHoldLedgerService(store: HoldLedgerStore, clock: Clock, flags: Registry): HoldLedgerService {
    return HoldLedgerService(
        store = store,
        validator = defaultHoldLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHoldLedgerEngine(flags),
    )
}
