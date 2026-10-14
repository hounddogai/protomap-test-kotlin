package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves capacity reservations through their life cycle and keeps a journal of what it did. */
data class CapacityReservationDesk(
    var store: CapacityReservationStore,
    var validator: CapacityReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CapacityReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capacity reservation $code not found")
        validator.requireValid(item)
        item.moveTo(CapacityReservationStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacityReservationStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("capacity reservation $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newCapacityReservationDesk(store: CapacityReservationStore, clock: Clock, flags: Registry): CapacityReservationDesk {
    return CapacityReservationDesk(
        store = store,
        validator = defaultCapacityReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCapacityReservationEngine(flags),
    )
}
