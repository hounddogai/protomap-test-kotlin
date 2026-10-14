package com.acme.transport.linehaul.capacity.milestone

import com.acme.transport.linehaul.clock.Clock

/** Moves lenient capacity reservations through their life cycle and keeps a journal of what it did. */
data class LenientCapacityReservationCoordinator(
    var store: LenientCapacityReservationStore,
    var validator: LenientCapacityReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientCapacityReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient capacity reservation $code not found")
        validator.requireValid(item)
        item.moveTo(LenientCapacityReservationStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient capacity reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientCapacityReservationStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient capacity reservation $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newLenientCapacityReservationCoordinator(store: LenientCapacityReservationStore, clock: Clock): LenientCapacityReservationCoordinator {
    return LenientCapacityReservationCoordinator(
        store = store,
        validator = defaultLenientCapacityReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientCapacityReservationEngine(),
    )
}
