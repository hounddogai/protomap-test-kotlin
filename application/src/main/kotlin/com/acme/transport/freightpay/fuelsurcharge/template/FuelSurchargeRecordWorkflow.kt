package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fuel surcharge records through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeRecordDesk(
    var store: FuelSurchargeRecordStore,
    var validator: FuelSurchargeRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelSurchargeRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fuel surcharge record $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeRecordStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeRecordStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fuel surcharge record $code not found")
        val result = engine.compute(item)
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

fun newFuelSurchargeRecordDesk(store: FuelSurchargeRecordStore, clock: Clock, flags: Registry): FuelSurchargeRecordDesk {
    return FuelSurchargeRecordDesk(
        store = store,
        validator = defaultFuelSurchargeRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelSurchargeRecordEngine(flags),
    )
}
