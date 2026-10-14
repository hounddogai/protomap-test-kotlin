package com.acme.transport.network.cutoff.adjustment

import com.acme.transport.network.clock.Clock

/** Moves legacy cutoff reservations through their life cycle and keeps a journal of what it did. */
data class LegacyCutoffReservationManager(
    var store: LegacyCutoffReservationStore,
    var validator: LegacyCutoffReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyCutoffReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy cutoff reservation $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyCutoffReservationStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy cutoff reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyCutoffReservationStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("legacy cutoff reservation $code not found")
        val result = engine.evaluate(item)
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

fun newLegacyCutoffReservationManager(store: LegacyCutoffReservationStore, clock: Clock): LegacyCutoffReservationManager {
    return LegacyCutoffReservationManager(
        store = store,
        validator = defaultLegacyCutoffReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyCutoffReservationEngine(),
    )
}
