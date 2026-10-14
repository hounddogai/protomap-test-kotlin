package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.emissions.clock.Clock

/**
 * Moves fallback route offset reservations through their life cycle and keeps a journal of what it did.
 */
data class FallbackRouteOffsetReservationService(
    var store: FallbackRouteOffsetReservationStore,
    var validator: FallbackRouteOffsetReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackRouteOffsetReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback route offset reservation $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackRouteOffsetReservationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback route offset reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackRouteOffsetReservationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback route offset reservation $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newFallbackRouteOffsetReservationService(store: FallbackRouteOffsetReservationStore, clock: Clock): FallbackRouteOffsetReservationService {
    return FallbackRouteOffsetReservationService(
        store = store,
        validator = defaultFallbackRouteOffsetReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackRouteOffsetReservationEngine(),
    )
}
