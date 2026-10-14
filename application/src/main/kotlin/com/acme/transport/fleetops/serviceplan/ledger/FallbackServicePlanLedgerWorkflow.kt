package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback service plan ledgers through their life cycle and keeps a journal of what it did. */
data class FallbackServicePlanLedgerService(
    var store: FallbackServicePlanLedgerStore,
    var validator: FallbackServicePlanLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackServicePlanLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback service plan ledger $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackServicePlanLedgerStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback service plan ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackServicePlanLedgerStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fallback service plan ledger $code not found")
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

fun newFallbackServicePlanLedgerService(store: FallbackServicePlanLedgerStore, clock: Clock, flags: Registry): FallbackServicePlanLedgerService {
    return FallbackServicePlanLedgerService(
        store = store,
        validator = defaultFallbackServicePlanLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackServicePlanLedgerEngine(flags),
    )
}
