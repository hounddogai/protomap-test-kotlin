package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fuel surcharge ledgers through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeLedgerManager(
    var store: FuelSurchargeLedgerStore,
    var validator: FuelSurchargeLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelSurchargeLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fuel surcharge ledger $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeLedgerStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeLedgerStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fuel surcharge ledger $code not found")
        val result = engine.evaluate(item)
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

fun newFuelSurchargeLedgerManager(store: FuelSurchargeLedgerStore, clock: Clock, flags: Registry): FuelSurchargeLedgerManager {
    return FuelSurchargeLedgerManager(
        store = store,
        validator = defaultFuelSurchargeLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelSurchargeLedgerEngine(flags),
    )
}
