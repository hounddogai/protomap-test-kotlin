package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/**
 * Moves seasonal trailer yard reservations through their life cycle and keeps a journal of what it did.
 */
data class SeasonalTrailerYardReservationService(
    var store: SeasonalTrailerYardReservationStore,
    var validator: SeasonalTrailerYardReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalTrailerYardReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal trailer yard reservation $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalTrailerYardReservationStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal trailer yard reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalTrailerYardReservationStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal trailer yard reservation $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newSeasonalTrailerYardReservationService(store: SeasonalTrailerYardReservationStore, clock: Clock, flags: Registry): SeasonalTrailerYardReservationService {
    return SeasonalTrailerYardReservationService(
        store = store,
        validator = defaultSeasonalTrailerYardReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalTrailerYardReservationEngine(flags),
    )
}
