package com.acme.transport.lastmile.redelivery.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves global redelivery ledgers through their life cycle and keeps a journal of what it did. */
data class GlobalRedeliveryLedgerService(
    var store: GlobalRedeliveryLedgerStore,
    var validator: GlobalRedeliveryLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalRedeliveryLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global redelivery ledger $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalRedeliveryLedgerStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global redelivery ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalRedeliveryLedgerStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("global redelivery ledger $code not found")
        val result = engine.price(item)
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

fun newGlobalRedeliveryLedgerService(store: GlobalRedeliveryLedgerStore, clock: Clock, flags: Registry): GlobalRedeliveryLedgerService {
    return GlobalRedeliveryLedgerService(
        store = store,
        validator = defaultGlobalRedeliveryLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalRedeliveryLedgerEngine(flags),
    )
}
