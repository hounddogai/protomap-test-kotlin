package com.acme.transport.crewing.swap.score

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves swap reservations through their life cycle and keeps a journal of what it did. */
data class SwapReservationCoordinator(
    var store: SwapReservationStore,
    var validator: SwapReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SwapReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("swap reservation $code not found")
        validator.requireValid(item)
        item.moveTo(SwapReservationStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("swap reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SwapReservationStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("swap reservation $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newSwapReservationCoordinator(store: SwapReservationStore, clock: Clock, flags: Registry): SwapReservationCoordinator {
    return SwapReservationCoordinator(
        store = store,
        validator = defaultSwapReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSwapReservationEngine(flags),
    )
}
