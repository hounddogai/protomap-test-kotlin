package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.linehaul.clock.Clock

/** Moves lenient capacity requests through their life cycle and keeps a journal of what it did. */
data class LenientCapacityRequestCoordinator(
    var store: LenientCapacityRequestStore,
    var validator: LenientCapacityRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientCapacityRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient capacity request $code not found")
        validator.requireValid(item)
        item.moveTo(LenientCapacityRequestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient capacity request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientCapacityRequestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient capacity request $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLenientCapacityRequestCoordinator(store: LenientCapacityRequestStore, clock: Clock): LenientCapacityRequestCoordinator {
    return LenientCapacityRequestCoordinator(
        store = store,
        validator = defaultLenientCapacityRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientCapacityRequestEngine(),
    )
}
