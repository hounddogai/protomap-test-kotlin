package com.acme.transport.emissions.fuelburn.audit

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fuel burn audits through their life cycle and keeps a journal of what it did. */
data class FuelBurnAuditWorkflow(
    var store: FuelBurnAuditStore,
    var validator: FuelBurnAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelBurnAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fuel burn audit $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnAuditStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnAuditStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fuel burn audit $code not found")
        val result = engine.run(item)
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

fun newFuelBurnAuditWorkflow(store: FuelBurnAuditStore, clock: Clock, flags: Registry): FuelBurnAuditWorkflow {
    return FuelBurnAuditWorkflow(
        store = store,
        validator = defaultFuelBurnAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelBurnAuditEngine(flags),
    )
}
