package com.acme.transport.fleetops.serviceplan.planledger

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default service plan ledgers through their life cycle and keeps a journal of what it did. */
data class DefaultServicePlanLedgerManager(
    var store: DefaultServicePlanLedgerStore,
    var validator: DefaultServicePlanLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultServicePlanLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default service plan ledger $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultServicePlanLedgerStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default service plan ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultServicePlanLedgerStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("default service plan ledger $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newDefaultServicePlanLedgerManager(store: DefaultServicePlanLedgerStore, clock: Clock, flags: Registry): DefaultServicePlanLedgerManager {
    return DefaultServicePlanLedgerManager(
        store = store,
        validator = defaultDefaultServicePlanLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultServicePlanLedgerEngine(flags),
    )
}
