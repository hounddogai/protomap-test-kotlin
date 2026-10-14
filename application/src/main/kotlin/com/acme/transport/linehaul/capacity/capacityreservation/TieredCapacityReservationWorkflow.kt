package com.acme.transport.linehaul.capacity.capacityreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves tiered capacity reservations through their life cycle and keeps a journal of what it did. */
data class TieredCapacityReservationWorkflow(
    var store: TieredCapacityReservationStore,
    var validator: TieredCapacityReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredCapacityReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered capacity reservation $code not found")
        validator.requireValid(item)
        item.moveTo(TieredCapacityReservationStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered capacity reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredCapacityReservationStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tiered capacity reservation $code not found")
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

fun newTieredCapacityReservationWorkflow(store: TieredCapacityReservationStore, clock: Clock, flags: Registry): TieredCapacityReservationWorkflow {
    return TieredCapacityReservationWorkflow(
        store = store,
        validator = defaultTieredCapacityReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredCapacityReservationEngine(flags),
    )
}
