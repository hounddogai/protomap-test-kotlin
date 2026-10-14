package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback accessorial ledgers through their life cycle and keeps a journal of what it did. */
data class FallbackAccessorialLedgerManager(
    var store: FallbackAccessorialLedgerStore,
    var validator: FallbackAccessorialLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackAccessorialLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback accessorial ledger $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackAccessorialLedgerStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback accessorial ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackAccessorialLedgerStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fallback accessorial ledger $code not found")
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

fun newFallbackAccessorialLedgerManager(store: FallbackAccessorialLedgerStore, clock: Clock, flags: Registry): FallbackAccessorialLedgerManager {
    return FallbackAccessorialLedgerManager(
        store = store,
        validator = defaultFallbackAccessorialLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackAccessorialLedgerEngine(flags),
    )
}
